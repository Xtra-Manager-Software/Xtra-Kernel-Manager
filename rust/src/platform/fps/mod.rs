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

/// Detach current target PID.
pub fn detach_fps_pid() -> bool {
    GLOBAL_FPS_METER.lock().detach_current()
}

/// Clear FPS buffer and reset caches.
pub fn clear_fps() {
    GLOBAL_FPS_METER.lock().clear();
}
