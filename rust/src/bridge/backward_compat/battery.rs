use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::{jfloat, jint, jstring};

use crate::bridge::create_jstring_safe;
use crate::platform::battery;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryLevelNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().level_pct
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCurrentNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().current_ma
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readDrainRateNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().current_ma
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryTempNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    (battery::read_battery_snapshot().temp_c * 10.0) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryVoltageNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    (battery::read_battery_snapshot().voltage_v * 1000.0) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    if battery::read_battery_snapshot().is_charging {
        1
    } else {
        0
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryHealthNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&mut env, &battery::read_battery_snapshot().health)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCycleCountNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().cycle_count.unwrap_or(-1)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCapacityLevelNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    battery::read_battery_snapshot().level_pct as f32
}
