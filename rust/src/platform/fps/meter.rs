use super::sysfs::SysfsFpsReader;
use super::types::{FpsReading, FpsSource};
use parking_lot::Mutex;
use std::collections::VecDeque;
use std::path::Path;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::mpsc::{channel, Receiver, Sender};
use std::sync::Arc;
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

const SHORT_WINDOW: usize = 30;
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
            frametimes: VecDeque::with_capacity(SHORT_WINDOW),
            last_frame_instant: Instant::now()
                .checked_sub(EBPF_TIMEOUT)
                .unwrap_or_else(Instant::now),
            attached_pid: None,
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

    /// Set a dedicated target package to monitor.
    /// When set, eBPF probe is ONLY attached when this app is in foreground.
    /// When app switches or exits, probe is automatically detached/unloaded.
    pub fn set_target_package(&mut self, pkg: Option<String>) {
        let changed = self.target_package != pkg;
        self.target_package = pkg;
        if changed && self.target_package.is_none() && self.whitelist.is_empty() {
            self.detach_current();
        }
    }

    pub fn get_target_package(&self) -> Option<String> {
        self.target_package.clone()
    }

    /// Set a list of target package names.
    pub fn set_target_whitelist(&mut self, list: Vec<String>) {
        self.whitelist = list;
        if self.target_package.is_none() && self.whitelist.is_empty() {
            self.detach_current();
        }
    }

    pub fn get_target_whitelist(&self) -> Vec<String> {
        self.whitelist.clone()
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
            if let Some(fg) = crate::platform::binder::get_foreground_app() {
                let matches_target = match (&self.target_package, &self.whitelist) {
                    (Some(target), _) if fg.package_name == *target => true,
                    (_, list) if !list.is_empty() => list.iter().any(|p| p == &fg.package_name),
                    _ => false,
                };

                if matches_target {
                    if let Some(pid) = fg.pid {
                        let current_attached = self.shared.lock().attached_pid;
                        if current_attached != Some(pid) {
                            self.attach_pid(pid);
                        }
                    }
                } else {
                    // Switched away from target app (e.g. went home or switched app):
                    // Detach immediately to unload uprobe and protect app & system!
                    if self.shared.lock().attached_pid.is_some() {
                        self.detach_current();
                    }
                }
            } else if self.shared.lock().attached_pid.is_some() {
                self.detach_current();
            }
        } else {
            // No target rule active: verify if manual attached PID is still alive
            let attached = self.shared.lock().attached_pid;
            if let Some(pid) = attached {
                if !Path::new(&format!("/proc/{}", pid)).exists() {
                    self.detach_current();
                }
            }
        }

        // 1. Try reading calculated FPS from eBPF ring buffer
        if let Some(fps) = self.calculate_ebpf_fps() {
            return Some(FpsReading {
                fps,
                source: FpsSource::Ebpf,
            });
        }

        // 2. Fall back to sysfs reader (DRM CRTC / framebuffer / Mali)
        self.sysfs.read()
    }

    fn calculate_ebpf_fps(&self) -> Option<f64> {
        let guard = self.shared.lock();
        match (!guard.frametimes.is_empty(), guard.last_frame_instant.elapsed()) {
            (true, elapsed) if elapsed < EBPF_TIMEOUT => {
                let total: Duration = guard.frametimes.iter().sum();
                let avg_secs = total.as_secs_f64() / guard.frametimes.len() as f64;
                if avg_secs <= 0.0 {
                    return None;
                }
                let raw_fps = 1.0 / avg_secs;
                if (0.1..=500.0).contains(&raw_fps) {
                    Some((raw_fps * 10.0).round() / 10.0)
                } else {
                    None
                }
            }
            _ => None,
        }
    }
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

        if attached_pid.is_none() {
            continue;
        }

        // Check if attached process died
        if let Some(pid) = attached_pid {
            if last_health_check.elapsed() >= Duration::from_millis(500) {
                last_health_check = Instant::now();
                if !Path::new(&format!("/proc/{}", pid)).exists() {
                    handle_detach(&mut probe, &mut attached_pid, &shared);
                    continue;
                }
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
                    while guard.frametimes.len() > SHORT_WINDOW {
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

    // Detach and drop old probe if present
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
        // p is dropped here, which triggers program.unload() and removes the uprobe!
    } else {
        *probe = None;
        *attached_pid = None;
    }
    let mut guard = shared.lock();
    guard.attached_pid = None;
    guard.frametimes.clear();
}
