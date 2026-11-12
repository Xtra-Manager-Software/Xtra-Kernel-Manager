use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::{jboolean, jfloat, jint};

use crate::platform::binder::{self, PowerBoost, PowerMode};

fn parse_power_mode(mode: jint) -> Option<PowerMode> {
    match mode {
        0 => Some(PowerMode::DoubleTapToWake),
        1 => Some(PowerMode::LowPower),
        2 => Some(PowerMode::SustainedPerformance),
        3 => Some(PowerMode::FixedPerformance),
        4 => Some(PowerMode::Vr),
        5 => Some(PowerMode::DeviceIdle),
        6 => Some(PowerMode::MultiWindow),
        7 => Some(PowerMode::ExpensiveRendering),
        8 => Some(PowerMode::Interactive),
        9 => Some(PowerMode::Game),
        10 => Some(PowerMode::AudioStreamingLowLatency),
        11 => Some(PowerMode::CameraStreamingSecure),
        12 => Some(PowerMode::GameLoading),
        _ => None,
    }
}

fn parse_power_boost(boost: jint) -> Option<PowerBoost> {
    match boost {
        0 => Some(PowerBoost::Interaction),
        1 => Some(PowerBoost::DisplayUpdateImmediate),
        2 => Some(PowerBoost::MlAccelerator),
        3 => Some(PowerBoost::AudioLaunch),
        4 => Some(PowerBoost::CameraLaunch),
        5 => Some(PowerBoost::CameraShot),
        _ => None,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isPowerSaveModeNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::is_power_save_mode().unwrap_or(false)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setPowerSaveModeNative(
    _env: EnvUnowned,
    _class: JClass,
    enabled: jboolean,
) -> jboolean {
    binder::set_power_save_mode_enabled(enabled).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setPowerModeNative(
    _env: EnvUnowned,
    _class: JClass,
    mode: jint,
    enabled: jboolean,
) -> jboolean {
    let Some(m) = parse_power_mode(mode) else {
        return false;
    };
    binder::set_power_mode(m, enabled).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isPowerModeSupportedNative(
    _env: EnvUnowned,
    _class: JClass,
    mode: jint,
) -> jboolean {
    let Some(m) = parse_power_mode(mode) else {
        return false;
    };
    binder::is_power_mode_supported(m)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setPowerBoostNative(
    _env: EnvUnowned,
    _class: JClass,
    boost: jint,
    duration_ms: jint,
) -> jboolean {
    let Some(b) = parse_power_boost(boost) else {
        return false;
    };
    binder::set_power_boost(b, duration_ms).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalStatusNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    binder::get_thermal_status().map(|s| s as jint).unwrap_or(-1)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalHeadroomNative(
    _env: EnvUnowned,
    _class: JClass,
    forecast_seconds: jint,
) -> jfloat {
    binder::get_thermal_headroom(forecast_seconds).unwrap_or(-1.0)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingControlSupportedNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::is_charging_control_supported()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getChargingEnabledNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::get_charging_enabled().unwrap_or(true)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setChargingEnabledNative(
    _env: EnvUnowned,
    _class: JClass,
    enabled: jboolean,
) -> jboolean {
    binder::set_charging_enabled(enabled).is_ok()
}
