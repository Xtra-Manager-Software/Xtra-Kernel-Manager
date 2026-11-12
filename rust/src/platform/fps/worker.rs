use parking_lot::Mutex;
use std::collections::VecDeque;
use std::path::Path;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::mpsc::Receiver;
use std::time::{Duration, Instant};

pub const WINDOW_SIZE: usize = 60;
pub const EBPF_TIMEOUT: Duration = Duration::from_millis(1500);

pub enum WorkerCmd {
    Attach(i32),
    Detach,
    Stop,
}

pub struct SharedFrames {
    pub frametimes: VecDeque<Duration>,
    pub last_frame_instant: Instant,
    pub attached_pid: Option<i32>,
    pub target_fps: Option<f64>,
}

pub fn run_ebpf_worker(
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
