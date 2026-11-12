pub mod bridge;
pub mod logging;
pub mod platform;
pub mod preset;
pub mod telemetry;

use jni::sys::jint;
use tracing::info;

#[unsafe(no_mangle)]
pub extern "system" fn JNI_OnLoad(
    _vm: *mut jni::sys::JavaVM,
    _reserved: *mut std::ffi::c_void,
) -> jint {
    // Initialize native logging system (logcat sink on Android, stderr on host)
    logging::init();
    info!("XKM native library initialized via JNI_OnLoad");

    // Clean up any stale bind-mounts left behind from previous runs or crashes
    platform::cpu::cleanup_stale_mounts();

    jni::sys::JNI_VERSION_1_6
}
