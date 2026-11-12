use super::sysfs::SysfsFpsReader;
use super::types::{FpsReading, FpsSource, FrameDropCause};
use parking_lot::Mutex;
use std::collections::VecDeque;
use std::path::Path;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::mpsc::{Receiver, Sender, channel};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

const WINDOW_SIZE: usize = 60;
const EBPF_TIMEOUT: Duration = Duration::from_millis(1500);

enum WorkerCmd {
    Attach(i32),
    Detach,
    Stop,
}

struct SharedFrames {
    frametimes: VecDeque<Duration>,
    last_frame_instant: Instant,
    attached_pid: Option<i32>,
    target_fps: Option<f64>,
}

pub struct FpsMeter {
    cmd_tx: Option<Sender<WorkerCmd>>,
    shared: Arc<Mutex<SharedFrames>>,
    worker_running: Arc<AtomicBool>,
    _worker_handle: Option<JoinHandle<()>>,
    sysfs: SysfsFpsReader,
    target_package: Option<String>,
    whitelist: Vec<String>,
}

impl Default for FpsMeter {
    fn default() -> Self {
        Self::new()
    }
}

impl FpsMeter {
    pub fn new() -> Self {
        let shared = Arc::new(Mutex::new(SharedFrames {
            frametimes: VecDeque::with_capacity(WINDOW_SIZE),
            last_frame_instant: Instant::now()
                .checked_sub(EBPF_TIMEOUT)
                .unwrap_or_else(Instant::now),
            attached_pid: None,
            target_fps: None,
        }));

        let worker_running = Arc::new(AtomicBool::new(true));
        let (cmd_tx, cmd_rx) = channel::<WorkerCmd>();

        let shared_clone = Arc::clone(&shared);
        let running_clone = Arc::clone(&worker_running);

        let handle = thread::Builder::new()
            .name("xkm-fps-ebpf".to_string())
            .spawn(move || {
                run_ebpf_worker(cmd_rx, shared_clone, running_clone);
            })
            .ok();

        Self {
            cmd_tx: Some(cmd_tx),
            shared,
            worker_running,
            _worker_handle: handle,
            sysfs: SysfsFpsReader::new(),
            target_package: None,
            whitelist: Vec::new(),
        }
    }

    /// Set target FPS (e.g. 60.0, 90.0, 120.0).
    pub fn set_target_fps(&mut self, target: Option<f64>) {
        self.shared.lock().target_fps = target;
    }

    pub fn get_target_fps(&self) -> Option<f64> {
        self.shared.lock().target_fps
    }

    /// Set a dedicated target package to monitor.
    pub fn set_target_package(&mut self, pkg: Option<String>) {
        let changed = self.target_package != pkg;
        self.target_package = pkg;
        if changed && self.target_package.is_none() && self.whitelist.is_empty() {
            self.detach_current();
        }
    }

    pub fn get_target_package(&self) -> Option<&str> {
        self.target_package.as_deref()
    }

    /// Set a list of target package names.
    pub fn set_target_whitelist(&mut self, list: Vec<String>) {
        self.whitelist = list;
        if self.target_package.is_none() && self.whitelist.is_empty() {
            self.detach_current();
        }
    }

    pub fn get_target_whitelist(&self) -> &[String] {
        &self.whitelist
    }

    #[inline]
    fn matches_target(&self, pkg: &str) -> bool {
        match (&self.target_package, &self.whitelist) {
            (Some(target), _) if pkg == target => true,
            (_, list) if !list.is_empty() => list.iter().any(|p| p == pkg),
            _ => false,
        }
    }

    /// Check if eBPF uprobe is currently attached to a live process.
    pub fn is_ebpf_attached(&self) -> bool {
        self.shared.lock().attached_pid.is_some()
    }

    /// Get current attached PID if any.
    pub fn get_attached_pid(&self) -> Option<i32> {
        self.shared.lock().attached_pid
    }

    /// Manually attach to a specific PID.
    pub fn attach_pid(&mut self, pid: i32) -> bool {
        if pid <= 0 {
            return false;
        }

        {
            let guard = self.shared.lock();
            if guard.attached_pid == Some(pid) {
                return true;
            }
        }

        if let Some(ref tx) = self.cmd_tx {
            let _ = tx.send(WorkerCmd::Attach(pid));
            true
        } else {
            false
        }
    }

    /// Detach current probe and completely unload eBPF uprobe from kernel.
    pub fn detach_current(&mut self) -> bool {
        {
            let mut guard = self.shared.lock();
            guard.attached_pid = None;
            guard.frametimes.clear();
        }
        if let Some(ref tx) = self.cmd_tx {
            let _ = tx.send(WorkerCmd::Detach);
            true
        } else {
            false
        }
    }

    pub fn clear(&mut self) {
        self.target_package = None;
        self.whitelist.clear();
        self.detach_current();
        self.sysfs.clear();
    }

    pub fn read(&mut self) -> Option<FpsReading> {
        let has_target_rule = self.target_package.is_some() || !self.whitelist.is_empty();

        if has_target_rule {
            match crate::platform::binder::get_foreground_app() {
                Some(fg) if self.matches_target(&fg.package_name) => match fg.pid {
                    Some(pid) if self.shared.lock().attached_pid != Some(pid) => {
                        self.attach_pid(pid);
                    }
                    _ => {}
                },
                _ => {
                    if self.shared.lock().attached_pid.is_some() {
                        self.detach_current();
                    }
                }
            }
        } else {
            let attached_pid = self.shared.lock().attached_pid;
            match attached_pid {
                Some(pid) if !Path::new(&format!("/proc/{}", pid)).exists() => {
                    self.detach_current();
                }
                _ => {}
            }
        }

        // 1. Try reading calculated FPS and telemetry from eBPF ring buffer
        if let Some(reading) = self.calculate_ebpf_stats() {
            return Some(reading);
        }

        // 2. Fall back to sysfs reader (DRM CRTC / framebuffer / Mali)
        if let Some(mut r) = self.sysfs.read() {
            if r.fps > 0.0 && r.fps < 45.0 {
                r.drop_cause = diagnose_drop_cause();
            }
            return Some(r);
        }

        None
    }

    fn calculate_ebpf_stats(&self) -> Option<FpsReading> {
        let guard = self.shared.lock();
        if guard.frametimes.is_empty() || guard.last_frame_instant.elapsed() >= EBPF_TIMEOUT {
            return None;
        }

        let mut ms_list: Vec<f64> = guard
            .frametimes
            .iter()
            .map(|d| d.as_secs_f64() * 1000.0)
            .filter(|&ms| ms > 1.0 && ms < 1000.0)
            .collect();

        if ms_list.is_empty() {
            return None;
        }

        let valid_count = ms_list.len();
        let current_frametime_ms = (ms_list[0] * 100.0).round() / 100.0;
        let current_fps = if current_frametime_ms > 0.0 {
            let f = 1000.0 / current_frametime_ms;
            (f * 10.0).round() / 10.0
        } else {
            0.0
        };

        let sum_ms: f64 = ms_list.iter().sum();
        let avg_ms = sum_ms / valid_count as f64;
        let avg_fps = if avg_ms > 0.0 {
            let f = 1000.0 / avg_ms;
            (f * 10.0).round() / 10.0
        } else {
            0.0
        };

        // Standard Deviation and Coefficient of Variation (cv)
        let variance = ms_list
            .iter()
            .map(|&ms| {
                let diff = ms - avg_ms;
                diff * diff
            })
            .sum::<f64>()
            / valid_count as f64;
        let std_dev_ms = (variance.sqrt() * 100.0).round() / 100.0;
        let cv = if avg_ms > 0.0 {
            let raw_cv = std_dev_ms / avg_ms;
            (raw_cv * 1000.0).round() / 1000.0
        } else {
            0.0
        };

        // Determine target FPS: user configured or auto-detected (60, 90, 120, 144)
        let target_fps = if let Some(t) = guard.target_fps {
            t
        } else if avg_fps > 105.0 {
            120.0
        } else if avg_fps > 75.0 {
            90.0
        } else {
            60.0
        };

        // Jank threshold: frame took > 1.35x expected deadline
        let deadline_ms = 1000.0 / target_fps;
        let jank_threshold = deadline_ms * 1.35;
        let jank_count = ms_list.iter().filter(|&&ms| ms >= jank_threshold).count() as u32;
        let jank_percent = ((jank_count as f64 / valid_count as f64) * 100.0 * 10.0).round() / 10.0;

        // 1% Low and 0.1% Low calculation
        // Sort ms_list ascending: index near end = slowest frames (lowest FPS)
        ms_list.sort_by(|a, b| a.partial_cmp(b).unwrap_or(std::cmp::Ordering::Equal));
        let p99_idx = (((valid_count - 1) as f64 * 0.99).floor() as usize).min(valid_count - 1);
        let p99_ms = ms_list[p99_idx];
        let low_1pct_fps = if p99_ms > 0.0 {
            (1000.0 / p99_ms * 10.0).round() / 10.0
        } else {
            0.0
        };

        let p999_idx = (((valid_count - 1) as f64 * 0.999).floor() as usize).min(valid_count - 1);
        let p999_ms = ms_list[p999_idx];
        let low_01pct_fps = if p999_ms > 0.0 {
            (1000.0 / p999_ms * 10.0).round() / 10.0
        } else {
            0.0
        };

        // Drop cause diagnosis if jank detected or high CV
        let drop_cause = if jank_count > 0 || cv >= 0.10 {
            diagnose_drop_cause()
        } else {
            FrameDropCause::Smooth
        };

        drop(guard);

        Some(FpsReading {
            fps: current_fps,
            source: FpsSource::Ebpf,
            frametime_ms: current_frametime_ms,
            avg_fps,
            low_1pct_fps,
            low_01pct_fps,
            std_dev_ms,
            cv,
            jank_count,
            jank_percent,
            drop_cause,
        })
    }
}

/// Diagnoses root cause of a frame drop / jank by examining real-time SoC telemetry.
pub fn diagnose_drop_cause() -> FrameDropCause {
    // 1. Thermal throttling check
    let cpu_temp = crate::platform::thermal::read_cpu_temperature();
    if cpu_temp >= 75.0 {
        return FrameDropCause::ThermalThrottling;
    }

    // 2. GPU bottleneck check
    let gpu_load = crate::platform::gpu::read_gpu_busy_pct();
    if gpu_load >= 85 {
        return FrameDropCause::GpuBound;
    }

    // 3. CPU bottleneck check
    let cpu_load = crate::platform::cpu::read_cpu_load();
    if cpu_load >= 85.0 {
        return FrameDropCause::CpuBound;
    }

    // 4. Memory pressure check (Available RAM < 350 MB)
    let (mem, _) = crate::platform::memory::read_memory_and_swap();
    if mem.available_kb > 0 && mem.available_kb < 350 * 1024 {
        return FrameDropCause::MemoryPressure;
    }

    // 5. Hardware has headroom, but frame stuttered -> App/Render stall
    FrameDropCause::AppStall
}

impl Drop for FpsMeter {
    fn drop(&mut self) {
        self.worker_running.store(false, Ordering::Release);
        if let Some(tx) = self.cmd_tx.take() {
            let _ = tx.send(WorkerCmd::Stop);
        }
    }
}

fn run_ebpf_worker(
    cmd_rx: Receiver<WorkerCmd>,
    shared: Arc<Mutex<SharedFrames>>,
    running: Arc<AtomicBool>,
) {
    let mut probe: Option<kala_ebpf::FrameProbe> = None;
    let mut attached_pid: Option<i32> = None;
    let mut last_health_check = Instant::now();

    while running.load(Ordering::Acquire) {
        // When not attached, block on command — 0% CPU consumption
        if attached_pid.is_none() {
            match cmd_rx.recv() {
                Ok(WorkerCmd::Attach(pid)) => {
                    handle_attach(&mut probe, pid, &mut attached_pid, &shared);
                }
                Ok(WorkerCmd::Detach) => {
                    handle_detach(&mut probe, &mut attached_pid, &shared);
                }
                Ok(WorkerCmd::Stop) | Err(_) => {
                    handle_detach(&mut probe, &mut attached_pid, &shared);
                    return;
                }
            }
            continue;
        }

        // When attached, drain all pending commands first
        while let Ok(cmd) = cmd_rx.try_recv() {
            match cmd {
                WorkerCmd::Attach(pid) => {
                    handle_attach(&mut probe, pid, &mut attached_pid, &shared);
                }
                WorkerCmd::Detach => {
                    handle_detach(&mut probe, &mut attached_pid, &shared);
                }
                WorkerCmd::Stop => {
                    handle_detach(&mut probe, &mut attached_pid, &shared);
                    return;
                }
            }
        }

        let Some(pid) = attached_pid else {
            continue;
        };

        // Check if attached process died
        if last_health_check.elapsed() >= Duration::from_millis(500) {
            last_health_check = Instant::now();
            if !Path::new(&format!("/proc/{}", pid)).exists() {
                handle_detach(&mut probe, &mut attached_pid, &shared);
                continue;
            }
        }

        // Pump frame events with 16ms deadline (~1 frame at 60Hz)
        if let Some(p) = probe.as_mut() {
            if let Some((_pid, dt)) = p.recv_with_deadline(Duration::from_millis(16)) {
                // Filter out sub-3ms bursts or >500ms stalls
                if dt >= Duration::from_millis(3) && dt < Duration::from_millis(500) {
                    let mut guard = shared.lock();
                    guard.frametimes.push_front(dt);
                    guard.last_frame_instant = Instant::now();
                    while guard.frametimes.len() > WINDOW_SIZE {
                        guard.frametimes.pop_back();
                    }
                }
            }
        } else {
            handle_detach(&mut probe, &mut attached_pid, &shared);
        }
    }

    // Clean up probe on exit
    handle_detach(&mut probe, &mut attached_pid, &shared);
}

fn handle_attach(
    probe: &mut Option<kala_ebpf::FrameProbe>,
    pid: i32,
    attached_pid: &mut Option<i32>,
    shared: &Arc<Mutex<SharedFrames>>,
) {
    if *attached_pid == Some(pid) && probe.is_some() {
        return;
    }

    if let (Some(mut p), Some(old_pid)) = (probe.take(), attached_pid.take()) {
        let _ = p.detach(old_pid);
    }

    let mut new_probe = match kala_ebpf::FrameProbe::new() {
        Ok(p) => p,
        Err(_) => {
            let mut guard = shared.lock();
            guard.attached_pid = None;
            return;
        }
    };

    if new_probe.attach(pid).is_ok() {
        *attached_pid = Some(pid);
        *probe = Some(new_probe);
        let mut guard = shared.lock();
        guard.attached_pid = Some(pid);
        guard.frametimes.clear();
        guard.last_frame_instant = Instant::now();
    } else {
        let mut guard = shared.lock();
        guard.attached_pid = None;
    }
}

fn handle_detach(
    probe: &mut Option<kala_ebpf::FrameProbe>,
    attached_pid: &mut Option<i32>,
    shared: &Arc<Mutex<SharedFrames>>,
) {
    if let (Some(mut p), Some(pid)) = (probe.take(), attached_pid.take()) {
        let _ = p.detach(pid);
    } else {
        *probe = None;
        *attached_pid = None;
    }
    let mut guard = shared.lock();
    guard.attached_pid = None;
    guard.frametimes.clear();
}
