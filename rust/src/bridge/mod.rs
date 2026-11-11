pub mod backward_compat;
pub mod binder_bridge;
pub mod telemetry_bridge;
pub mod tuning_bridge;

use jni::objects::JString;
use jni::sys::jstring;
use jni::JNIEnv;

#[inline]
pub fn create_jstring_safe(env: &JNIEnv, s: &str) -> jstring {
    env.new_string(s)
        .unwrap_or_else(|_| env.new_string("").unwrap())
        .into_raw()
}

#[inline]
pub fn jstring_to_string(env: &mut JNIEnv, jstr: &JString) -> Option<String> {
    env.get_string(jstr).ok().map(|s| s.into())
}
