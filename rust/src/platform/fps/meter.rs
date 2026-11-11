use super::sysfs::SysfsFpsReader;
use super::types::{FpsReading, FpsSource};
use parking_lot::Mutex;
use std::collections::VecDeque;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::mpsc::{Receiver, Sender, channel};
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
        }
    }

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

    pub fn detach_current(&mut self) -> bool {
        if let Some(ref tx) = self.cmd_tx {
            let _ = tx.send(WorkerCmd::Detach);
            true
        } else {
            false
        }
    }

    pub fn clear(&mut self) {
        {
            let mut guard = self.shared.lock();
            guard.frametimes.clear();
            guard.attached_pid = None;
            guard.last_frame_instant = Instant::now()
                .checked_sub(EBPF_TIMEOUT)
                .unwrap_or_else(Instant::now);
        }
        if let Some(ref tx) = self.cmd_tx {
            let _ = tx.send(WorkerCmd::Detach);
        }
        self.sysfs.clear();
    }

    pub fn read(&mut self) -> Option<FpsReading> {
        // Auto-detect and attach to foreground app PID if changed
        if let Some(fg) = crate::platform::binder::get_foreground_app() {
            if let Some(pid) = fg.pid {
                let current_attached = self.shared.lock().attached_pid;
                if current_attached != Some(pid) {
                    self.attach_pid(pid);
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
    let mut probe: Option<kala_ebpf::FrameProbe> = kala_ebpf::FrameProbe::new().ok();
    let mut attached_pid: Option<i32> = None;

    while running.load(Ordering::Acquire) {
        // When not attached, block on command
        if attached_pid.is_none() {
            match cmd_rx.recv() {
                Ok(WorkerCmd::Attach(pid)) => {
                    handle_attach(&mut probe, pid, &mut attached_pid, &shared);
                }
                Ok(WorkerCmd::Detach) => {
                    handle_detach(&mut probe, &mut attached_pid, &shared);
                }
                Ok(WorkerCmd::Stop) | Err(_) => return,
            }
            continue;
        }

        // When attached, drain commands first
        while let Ok(cmd) = cmd_rx.try_recv() {
            match cmd {
                WorkerCmd::Attach(pid) => {
                    handle_attach(&mut probe, pid, &mut attached_pid, &shared);
                }
                WorkerCmd::Detach => {
                    handle_detach(&mut probe, &mut attached_pid, &shared);
                }
                WorkerCmd::Stop => return,
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
            // No probe available, sleep to avoid busy-spinning
            thread::sleep(Duration::from_millis(100));
        }
    }
}

fn handle_attach(
    probe: &mut Option<kala_ebpf::FrameProbe>,
    pid: i32,
    attached_pid: &mut Option<i32>,
    shared: &Arc<Mutex<SharedFrames>>,
) {
    if *attached_pid == Some(pid) {
        return;
    }

    if probe.is_none() {
        *probe = kala_ebpf::FrameProbe::new().ok();
    }

    if let Some(p) = probe.as_mut() {
        if let Some(old_pid) = attached_pid.take() {
            let _ = p.detach(old_pid);
        }

        if p.attach(pid).is_ok() {
            *attached_pid = Some(pid);
            let mut guard = shared.lock();
            guard.attached_pid = Some(pid);
            guard.frametimes.clear();
            guard.last_frame_instant = Instant::now();
        }
    }
}

fn handle_detach(
    probe: &mut Option<kala_ebpf::FrameProbe>,
    attached_pid: &mut Option<i32>,
    shared: &Arc<Mutex<SharedFrames>>,
) {
    if let (Some(p), Some(pid)) = (probe.as_mut(), attached_pid.take()) {
        let _ = p.detach(pid);
    }
    let mut guard = shared.lock();
    guard.attached_pid = None;
    guard.frametimes.clear();
}
