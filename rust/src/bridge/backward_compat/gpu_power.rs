use jni::EnvUnowned;
use jni::objects::{JClass, JString};
use jni::sys::{jint, jstring};

use crate::bridge::{create_jstring_safe, jstring_to_string};
use crate::platform::{common, gpu, thermal};

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuFreqNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    gpu::read_gpu_freq_mhz()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuBusyNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    gpu::read_gpu_busy_pct()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_resetGpuStatsNative(
    _env: EnvUnowned,
    _class: JClass,
) {
    gpu::reset_gpu_busy_stats();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuVendorNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let (vendor, _) = gpu::get_gpu_info();
    create_jstring_safe(&mut env, &vendor.to_string())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuModelNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let (_, model) = gpu::get_gpu_info();
    create_jstring_safe(&mut env, model)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuAvailableFrequenciesNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let freqs = gpu::get_available_frequencies();
    let json = serde_json::to_string(&freqs).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuAvailablePoliciesNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&mut env, "[]")
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuDriverInfoNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let (_, model) = gpu::get_gpu_info();
    create_jstring_safe(&mut env, model)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readThermalZonesNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let summary = thermal::scan_thermal_zones();
    let json = serde_json::to_string(&summary.zones).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSystemPropertyNative(
    mut env: EnvUnowned,
    _class: JClass,
    key: JString,
) -> jstring {
    let prop_key = jstring_to_string(&mut env, &key).unwrap_or_default();
    let val = common::get_system_property(&prop_key).unwrap_or_default();
    create_jstring_safe(&mut env, &val)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readWakeupCountNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    common::read_sysfs_i64("/sys/power/wakeup_count").unwrap_or(0) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readSuspendCountNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    common::read_sysfs_i64("/sys/kernel/debug/suspend_stats/success").unwrap_or(0) as jint
}
