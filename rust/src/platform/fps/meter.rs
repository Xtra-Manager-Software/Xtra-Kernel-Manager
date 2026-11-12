use parking_lot::Mutex;
use std::collections::VecDeque;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::mpsc::{Sender, channel};
use std::thread::{self, JoinHandle};
use std::time::Instant;

use super::diagnose::diagnose_drop_cause;
use super::stats::calculate_ebpf_stats;
use super::sysfs::SysfsFpsReader;
use super::types::FpsReading;
use super::worker::{EBPF_TIMEOUT, SharedFrames, WINDOW_SIZE, WorkerCmd, run_ebpf_worker};

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

    /// Clear FPS buffer and caches.
    pub fn clear(&mut self) {
        let mut guard = self.shared.lock();
        guard.frametimes.clear();
        self.sysfs.clear();
        self.target_package = None;
        self.whitelist.clear();
    }

    /// Handle newly focused app from system event / polling.
    pub fn on_app_focused(&mut self, package_name: &str) {
        if self.matches_target(package_name) {
            if let Some(Some(pid)) = crate::platform::binder::get_foreground_app()
                .filter(|a| a.package_name == package_name)
                .map(|a| a.pid)
            {
                self.attach_pid(pid);
            }
        } else if self.target_package.is_some() || !self.whitelist.is_empty() {
            self.detach_current();
        }
    }

    /// Read the current FPS metric.
    pub fn read(&mut self) -> Option<FpsReading> {
        let is_attached = self.shared.lock().attached_pid.is_some();
        if !is_attached && (self.target_package.is_some() || !self.whitelist.is_empty()) {
            let fg_opt = crate::platform::binder::get_foreground_app();
            if let Some(fg) = fg_opt {
                self.on_app_focused(&fg.package_name);
            }
        }

        // 1. Try reading calculated FPS and telemetry from eBPF ring buffer
        if let Some(reading) = calculate_ebpf_stats(&self.shared) {
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
}

impl Drop for FpsMeter {
    fn drop(&mut self) {
        self.worker_running.store(false, Ordering::Release);
        if let Some(tx) = self.cmd_tx.take() {
            let _ = tx.send(WorkerCmd::Stop);
        }
    }
}
