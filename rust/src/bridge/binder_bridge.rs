use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jfloat, jint, jstring};

use super::{create_jstring_safe, jstring_to_string};
use crate::platform::binder;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getForegroundAppNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let pkg = binder::get_foreground_app()
        .map(|f| f.package_name)
        .unwrap_or_default();
    create_jstring_safe(&env, &pkg)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_killBackgroundProcessesNative(
    mut env: JNIEnv,
    _class: JClass,
    pkg: JString,
) -> jni::sys::jboolean {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.is_empty() {
        return jni::sys::JNI_FALSE;
    }
    match binder::kill_background_processes(&pkg_str, None) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_killAllBackgroundProcessesNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::kill_all_background_processes() {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_forceStopPackageNative(
    mut env: JNIEnv,
    _class: JClass,
    pkg: JString,
) -> jni::sys::jboolean {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.is_empty() {
        return jni::sys::JNI_FALSE;
    }
    match binder::force_stop_package(&pkg_str, None) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_killPidsNative(
    _env: JNIEnv,
    _class: JClass,
    pid: jint,
) -> jni::sys::jboolean {
    if pid <= 0 {
        return jni::sys::JNI_FALSE;
    }
    match binder::kill_pids(&[pid], "NativeTaskKill") {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isProcessFrozenNative(
    _env: JNIEnv,
    _class: JClass,
    pid: jint,
) -> jni::sys::jboolean {
    if pid <= 0 {
        return jni::sys::JNI_FALSE;
    }
    match binder::is_process_frozen(pid) {
        Ok(true) => jni::sys::JNI_TRUE,
        _ => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_trimProcessMemoryNative(
    mut env: JNIEnv,
    _class: JClass,
    process: JString,
    uid: jint,
    level: jint,
) -> jni::sys::jboolean {
    let proc_str = jstring_to_string(&mut env, &process).unwrap_or_default();
    if proc_str.is_empty() {
        return jni::sys::JNI_FALSE;
    }
    let trim_lvl = match level {
        5 => binder::MemoryTrimLevel::RunningModerate,
        10 => binder::MemoryTrimLevel::RunningLow,
        15 => binder::MemoryTrimLevel::RunningCritical,
        40 => binder::MemoryTrimLevel::Background,
        60 => binder::MemoryTrimLevel::Moderate,
        80 => binder::MemoryTrimLevel::Complete,
        _ => binder::MemoryTrimLevel::RunningCritical,
    };
    match binder::set_process_memory_trim_level(&proc_str, uid, trim_lvl) {
        Ok(success) if success => jni::sys::JNI_TRUE,
        _ => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setAppStandbyBucketNative(
    mut env: JNIEnv,
    _class: JClass,
    pkg: JString,
    bucket: jint,
) -> jni::sys::jboolean {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.is_empty() {
        return jni::sys::JNI_FALSE;
    }
    let b = binder::StandbyBucket::from_i32(bucket);
    match binder::set_app_standby_bucket(&pkg_str, b, None) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getAppStandbyBucketNative(
    mut env: JNIEnv,
    _class: JClass,
    pkg: JString,
) -> jint {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.is_empty() {
        return 10;
    }
    binder::get_app_standby_bucket(&pkg_str, None)
        .map(|b| b as i32 as jint)
        .unwrap_or(10)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getRunningProcessesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let list = binder::get_running_process_list().unwrap_or_default();
    let json = serde_json::to_string(&list).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isHighTouchPollingRateSupportedNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::is_high_touch_polling_rate_supported() {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getHighTouchPollingRateEnabledNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::get_high_touch_polling_rate_enabled().unwrap_or(false) {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setHighTouchPollingRateEnabledNative(
    _env: JNIEnv,
    _class: JClass,
    enabled: jni::sys::jboolean,
) -> jni::sys::jboolean {
    let val = enabled != jni::sys::JNI_FALSE;
    match binder::set_high_touch_polling_rate_enabled(val) {
        Ok(s) if s => jni::sys::JNI_TRUE,
        _ => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalStatusNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    binder::get_thermal_status()
        .map(|s| s as i32 as jint)
        .unwrap_or(0)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalHeadroomNative(
    _env: JNIEnv,
    _class: JClass,
    forecast_seconds: jint,
) -> jfloat {
    binder::get_thermal_headroom(forecast_seconds).unwrap_or(f32::NAN) as jfloat
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingControlSupportedNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::is_charging_control_supported() {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getChargingEnabledNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::get_charging_enabled().unwrap_or(true) {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setChargingEnabledNative(
    _env: JNIEnv,
    _class: JClass,
    enabled: jni::sys::jboolean,
) -> jni::sys::jboolean {
    let val = enabled != jni::sys::JNI_FALSE;
    match binder::set_charging_enabled(val) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}
