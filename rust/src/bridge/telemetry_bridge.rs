use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::jstring;

use super::create_jstring_safe;
use crate::telemetry::collect_snapshot;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getTelemetrySnapshotNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let snapshot = collect_snapshot();
    let json = serde_json::to_string(&snapshot).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(&mut env, &json)
}
