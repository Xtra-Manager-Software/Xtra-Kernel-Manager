pub mod backward_compat;
pub mod binder_bridge;
pub mod drm_bridge;
pub mod fps_bridge;
pub mod gpu_bridge;
pub mod logging_bridge;
pub mod media_bridge;
pub mod module_bridge;
pub mod network_bridge;
pub mod npu_bridge;
pub mod sensor_bridge;
pub mod system_bridge;
pub mod telemetry_bridge;
pub mod tuning_bridge;

use jni::EnvUnowned;
use jni::objects::JString;
use jni::sys::jstring;

#[inline]
pub fn create_jstring_safe(unowned: &mut EnvUnowned, s: &str) -> jstring {
    let outcome: jni::EnvOutcome<'_, jstring, jni::errors::Error> = unowned.with_env(|env| {
        let js = env.new_string(s).unwrap_or_else(|_| env.new_string("").unwrap());
        Ok(js.into_raw())
    });
    match outcome.into_outcome() {
        jni::Outcome::Ok(raw) => raw,
        _ => std::ptr::null_mut(),
    }
}

#[inline]
pub fn jstring_to_string(unowned: &mut EnvUnowned, jstr: &JString) -> Option<String> {
    let outcome: jni::EnvOutcome<'_, Option<String>, jni::errors::Error> = unowned.with_env(|env| {
        let s = jstr.try_to_string(env).ok();
        Ok(s)
    });
    match outcome.into_outcome() {
        jni::Outcome::Ok(res) => res,
        _ => None,
    }
}
