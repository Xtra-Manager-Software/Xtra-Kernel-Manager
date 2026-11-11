pub mod meter;
pub mod sysfs;
pub mod types;

pub use meter::FpsMeter;
pub use sysfs::{FPS_SYSFS_PATHS, SysfsFpsReader, parse_sysfs_fps};
pub use types::{FpsReading, FpsSource};

use once_cell::sync::Lazy;
use parking_lot::Mutex;

static GLOBAL_FPS_METER: Lazy<Mutex<FpsMeter>> = Lazy::new(|| Mutex::new(FpsMeter::new()));

/// Read current FPS from eBPF uprobe on /system/lib64/libgui.so with sysfs fallback.
pub fn read_fps() -> Option<FpsReading> {
    GLOBAL_FPS_METER.lock().read()
}

/// Attach FPS probe to a specific target PID.
pub fn attach_fps_pid(pid: i32) -> bool {
    GLOBAL_FPS_METER.lock().attach_pid(pid)
}

/// Detach current target PID and unload eBPF uprobe.
pub fn detach_fps_pid() -> bool {
    GLOBAL_FPS_METER.lock().detach_current()
}

/// Clear FPS buffer and reset caches.
pub fn clear_fps() {
    GLOBAL_FPS_METER.lock().clear();
}

/// Set a single target package to monitor.
pub fn set_target_package(pkg: Option<String>) {
    GLOBAL_FPS_METER.lock().set_target_package(pkg);
}

/// Get current target package name.
pub fn get_target_package() -> Option<String> {
    GLOBAL_FPS_METER.lock().get_target_package()
}

/// Set target package whitelist.
pub fn set_target_whitelist(list: Vec<String>) {
    GLOBAL_FPS_METER.lock().set_target_whitelist(list);
}

/// Get target package whitelist.
pub fn get_target_whitelist() -> Vec<String> {
    GLOBAL_FPS_METER.lock().get_target_whitelist()
}

/// Check if eBPF uprobe is currently attached to a live process.
pub fn is_ebpf_attached() -> bool {
    GLOBAL_FPS_METER.lock().is_ebpf_attached()
}

/// Get current attached PID.
pub fn get_attached_pid() -> Option<i32> {
    GLOBAL_FPS_METER.lock().get_attached_pid()
}
