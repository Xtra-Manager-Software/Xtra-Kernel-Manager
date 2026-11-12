use jni::EnvUnowned;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring};

use crate::bridge::{create_jstring_safe, jstring_to_string};
use crate::platform::binder;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_runFstrimNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::run_fstrim().unwrap_or(false)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getStorageLifeTimeNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let json = match binder::get_storage_life_time() {
        Ok(health) => serde_json::to_string(&health).unwrap_or_else(|_| "{}".to_string()),
        Err(_) => "{}".to_string(),
    };
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_checkRecentKernelCrashesNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let json = match binder::check_recent_kernel_crashes() {
        Ok(entries) => serde_json::to_string(&entries).unwrap_or_else(|_| "[]".to_string()),
        Err(_) => "[]".to_string(),
    };
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_rebootSystemNative(
    mut env: EnvUnowned,
    _class: JClass,
    target: JString,
) -> jboolean {
    let target_str = jstring_to_string(&mut env, &target).unwrap_or_default();
    binder::reboot_system(&target_str).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isWifiLowLatencyActiveNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::is_wifi_low_latency_active()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setWifiLowLatencyLockNative(
    _env: EnvUnowned,
    _class: JClass,
    enabled: jboolean,
) -> jboolean {
    binder::set_wifi_low_latency_lock(enabled).unwrap_or(false)
}
