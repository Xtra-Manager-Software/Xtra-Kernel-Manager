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

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_runFstrimNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::run_fstrim() {
        Ok(true) => jni::sys::JNI_TRUE,
        _ => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getStorageLifeTimeNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let json = match binder::get_storage_life_time() {
        Ok(health) => serde_json::to_string(&health).unwrap_or_else(|_| "{}".to_string()),
        Err(_) => "{}".to_string(),
    };
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_checkRecentKernelCrashesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let json = match binder::check_recent_kernel_crashes() {
        Ok(entries) => serde_json::to_string(&entries).unwrap_or_else(|_| "[]".to_string()),
        Err(_) => "[]".to_string(),
    };
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_rebootSystemNative(
    mut env: JNIEnv,
    _class: JClass,
    target: JString,
) -> jni::sys::jboolean {
    let target_str = jstring_to_string(&mut env, &target).unwrap_or_default();
    match binder::reboot_system(&target_str) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isPowerSaveModeNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::is_power_save_mode().unwrap_or(false) {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setPowerSaveModeNative(
    _env: JNIEnv,
    _class: JClass,
    enabled: jni::sys::jboolean,
) -> jni::sys::jboolean {
    let val = enabled != jni::sys::JNI_FALSE;
    match binder::set_power_save_mode_enabled(val) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setPowerModeNative(
    _env: JNIEnv,
    _class: JClass,
    mode: jint,
    enabled: jni::sys::jboolean,
) -> jni::sys::jboolean {
    let m = match mode {
        0 => binder::PowerMode::DoubleTapToWake,
        1 => binder::PowerMode::LowPower,
        2 => binder::PowerMode::SustainedPerformance,
        3 => binder::PowerMode::FixedPerformance,
        4 => binder::PowerMode::Vr,
        5 => binder::PowerMode::DeviceIdle,
        6 => binder::PowerMode::MultiWindow,
        7 => binder::PowerMode::ExpensiveRendering,
        8 => binder::PowerMode::Interactive,
        9 => binder::PowerMode::Game,
        10 => binder::PowerMode::AudioStreamingLowLatency,
        11 => binder::PowerMode::CameraStreamingSecure,
        12 => binder::PowerMode::GameLoading,
        _ => return jni::sys::JNI_FALSE,
    };
    let val = enabled != jni::sys::JNI_FALSE;
    match binder::set_power_mode(m, val) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isPowerModeSupportedNative(
    _env: JNIEnv,
    _class: JClass,
    mode: jint,
) -> jni::sys::jboolean {
    let m = match mode {
        0 => binder::PowerMode::DoubleTapToWake,
        1 => binder::PowerMode::LowPower,
        2 => binder::PowerMode::SustainedPerformance,
        3 => binder::PowerMode::FixedPerformance,
        4 => binder::PowerMode::Vr,
        5 => binder::PowerMode::DeviceIdle,
        6 => binder::PowerMode::MultiWindow,
        7 => binder::PowerMode::ExpensiveRendering,
        8 => binder::PowerMode::Interactive,
        9 => binder::PowerMode::Game,
        10 => binder::PowerMode::AudioStreamingLowLatency,
        11 => binder::PowerMode::CameraStreamingSecure,
        12 => binder::PowerMode::GameLoading,
        _ => return jni::sys::JNI_FALSE,
    };
    match binder::is_power_mode_supported(m) {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setPowerBoostNative(
    _env: JNIEnv,
    _class: JClass,
    boost: jint,
    duration_ms: jint,
) -> jni::sys::jboolean {
    let b = match boost {
        0 => binder::PowerBoost::Interaction,
        1 => binder::PowerBoost::DisplayUpdateImmediate,
        2 => binder::PowerBoost::MlAccelerator,
        3 => binder::PowerBoost::AudioLaunch,
        4 => binder::PowerBoost::CameraLaunch,
        5 => binder::PowerBoost::CameraShot,
        _ => return jni::sys::JNI_FALSE,
    };
    match binder::set_power_boost(b, duration_ms) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isWifiLowLatencyActiveNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match binder::is_wifi_low_latency_active() {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setWifiLowLatencyLockNative(
    _env: JNIEnv,
    _class: JClass,
    enabled: jni::sys::jboolean,
) -> jni::sys::jboolean {
    let val = enabled != jni::sys::JNI_FALSE;
    match binder::set_wifi_low_latency_lock(val) {
        Ok(res) if res => jni::sys::JNI_TRUE,
        _ => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getActiveDisplayRefreshRateNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    binder::get_active_display_refresh_rate() as jfloat
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_applyRefreshRateNative(
    _env: JNIEnv,
    _class: JClass,
    rate: jfloat,
) -> jni::sys::jboolean {
    match binder::apply_refresh_rate(rate as u32) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}
