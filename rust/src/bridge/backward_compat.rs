use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jfloat, jint, jlong, jstring};

use super::{create_jstring_safe, jstring_to_string};
use crate::platform::{battery, common, cpu, gpu, memory, thermal};

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_detectCpuClustersNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let clusters = cpu::detect_cpu_clusters();
    let json = serde_json::to_string(&clusters).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreDataNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let cores = cpu::read_core_data();
    let json = serde_json::to_string(&cores).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuLoadNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    cpu::read_cpu_load()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuTemperatureNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    thermal::read_cpu_temperature()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreTemperatureNative(
    _env: JNIEnv,
    _class: JClass,
    _core: jint,
) -> jfloat {
    thermal::read_cpu_temperature()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getCpuModelNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, cpu::get_cpu_model())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuFreqNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    gpu::read_gpu_freq_mhz()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readGpuBusyNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    gpu::read_gpu_busy_pct()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_resetGpuStatsNative(
    _env: JNIEnv,
    _class: JClass,
) {
    gpu::reset_gpu_busy_stats();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryLevelNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().level_pct
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCurrentNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().current_ma
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readDrainRateNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().current_ma
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryTempNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    (battery::read_battery_snapshot().temp_c * 10.0) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryVoltageNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    (battery::read_battery_snapshot().voltage_v * 1000.0) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    match battery::read_battery_snapshot().is_charging {
        true => 1,
        false => 0,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryHealthNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, &battery::read_battery_snapshot().health)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCycleCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    battery::read_battery_snapshot().cycle_count.unwrap_or(-1)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readBatteryCapacityLevelNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    battery::read_battery_snapshot().level_pct as f32
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readMemInfoNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let (mem, swap) = memory::read_memory_and_swap();
    let zram = memory::read_zram_stats();
    let swappiness = memory::get_swappiness();

    #[derive(serde::Serialize)]
    struct FullMemInfo {
        memory: memory::MemoryInfo,
        swap: memory::SwapInfo,
        zram: memory::ZramStats,
        swappiness: i32,
    }

    let full = FullMemInfo {
        memory: mem,
        swap,
        zram,
        swappiness,
    };

    let json = serde_json::to_string(&full).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramSizeNative(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    memory::read_zram_stats().disksize
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressionRatioNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    memory::read_zram_stats().compression_ratio
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressedSizeNative(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    memory::read_zram_stats().compr_data_size
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramOrigDataSizeNative(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    memory::read_zram_stats().orig_data_size
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramAlgorithmNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, &memory::get_current_zram_algorithm())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getAvailableZramAlgorithmsNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let algos = memory::get_available_zram_algorithms();
    let json = serde_json::to_string(&algos).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSwappinessNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    memory::get_swappiness()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getMemoryPressureNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    let (mem, _) = memory::read_memory_and_swap();
    match mem.total_kb > 0 {
        true => {
            let used = mem.total_kb.saturating_sub(mem.available_kb);
            ((used as f32 / mem.total_kb as f32) * 100.0).clamp(0.0, 100.0)
        }
        false => 0.0,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readThermalZonesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let summary = thermal::scan_thermal_zones();
    let json = serde_json::to_string(&summary.zones).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSystemPropertyNative(
    mut env: JNIEnv,
    _class: JClass,
    key: JString,
) -> jstring {
    let prop_key = jstring_to_string(&mut env, &key).unwrap_or_default();
    let val = common::get_system_property(&prop_key).unwrap_or_default();
    create_jstring_safe(&env, &val)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuVendorNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let (vendor, _) = gpu::get_gpu_info();
    create_jstring_safe(&env, &vendor.to_string())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuModelNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let (_, model) = gpu::get_gpu_info();
    create_jstring_safe(&env, model)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuAvailableFrequenciesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let freqs = gpu::get_available_frequencies();
    let json = serde_json::to_string(&freqs).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuAvailablePoliciesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&env, "[]")
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getGpuDriverInfoNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let (_, model) = gpu::get_gpu_info();
    create_jstring_safe(&env, model)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readWakeupCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    common::read_sysfs_i64("/sys/power/wakeup_count").unwrap_or(0) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readSuspendCountNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    common::read_sysfs_i64("/sys/kernel/debug/suspend_stats/success").unwrap_or(0) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramDeviceStatsNative(
    env: JNIEnv,
    _class: JClass,
    _device: jint,
) -> jstring {
    let stats = memory::read_zram_stats();
    let json = serde_json::to_string(&stats).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(&env, &json)
}
