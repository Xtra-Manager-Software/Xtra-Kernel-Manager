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
    create_jstring_safe(&env, &cpu::get_cpu_model())
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

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readFpsNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    match crate::platform::fps::read_fps() {
        Some(reading) => reading.fps as jfloat,
        None => -1.0,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readFpsInfoNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let json = match crate::platform::fps::read_fps() {
        Some(reading) => serde_json::to_string(&reading).unwrap_or_else(|_| "".to_string()),
        None => "".to_string(),
    };
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_attachFpsPidNative(
    _env: JNIEnv,
    _class: JClass,
    pid: jint,
) -> jni::sys::jboolean {
    if crate::platform::fps::attach_fps_pid(pid) {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_clearFpsNative(
    _env: JNIEnv,
    _class: JClass,
) {
    crate::platform::fps::clear_fps();
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_detachFpsNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    if crate::platform::fps::detach_fps_pid() {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setFpsTargetPackageNative(
    mut env: JNIEnv,
    _class: JClass,
    pkg: jni::objects::JString,
) {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.trim().is_empty() {
        crate::platform::fps::set_target_package(None);
    } else {
        crate::platform::fps::set_target_package(Some(pkg_str));
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_clearFpsTargetPackageNative(
    _env: JNIEnv,
    _class: JClass,
) {
    crate::platform::fps::set_target_package(None);
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isFpsEbpfAttachedNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    if crate::platform::fps::is_ebpf_attached() {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getFpsAttachedPidNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    crate::platform::fps::get_attached_pid().unwrap_or(-1) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setFpsTargetFpsNative(
    _env: JNIEnv,
    _class: JClass,
    target: jfloat,
) {
    if target > 0.0 {
        crate::platform::fps::set_target_fps(Some(target as f64));
    } else {
        crate::platform::fps::set_target_fps(None);
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getFpsTargetFpsNative(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    crate::platform::fps::get_target_fps().unwrap_or(0.0) as jfloat
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getFpsDropCauseNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let reading = crate::platform::fps::read_fps();
    let cause_str = match reading {
        Some(r) => r.drop_cause.as_str(),
        None => "None",
    };
    create_jstring_safe(&env, cause_str)
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
    match crate::platform::binder::kill_background_processes(&pkg_str, None) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_killAllBackgroundProcessesNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    match crate::platform::binder::kill_all_background_processes() {
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
    match crate::platform::binder::force_stop_package(&pkg_str, None) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
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
        5 => crate::platform::binder::MemoryTrimLevel::RunningModerate,
        10 => crate::platform::binder::MemoryTrimLevel::RunningLow,
        15 => crate::platform::binder::MemoryTrimLevel::RunningCritical,
        40 => crate::platform::binder::MemoryTrimLevel::Background,
        60 => crate::platform::binder::MemoryTrimLevel::Moderate,
        80 => crate::platform::binder::MemoryTrimLevel::Complete,
        _ => crate::platform::binder::MemoryTrimLevel::RunningCritical,
    };
    match crate::platform::binder::set_process_memory_trim_level(&proc_str, uid as i32, trim_lvl) {
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
    let b = crate::platform::binder::StandbyBucket::from_i32(bucket as i32);
    match crate::platform::binder::set_app_standby_bucket(&pkg_str, b, None) {
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
    crate::platform::binder::get_app_standby_bucket(&pkg_str, None)
        .map(|b| b as i32 as jint)
        .unwrap_or(10)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getRunningProcessesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let list = crate::platform::binder::get_running_process_list().unwrap_or_default();
    let json = serde_json::to_string(&list).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isHighTouchPollingRateSupportedNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    if crate::platform::binder::is_high_touch_polling_rate_supported() {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getHighTouchPollingRateEnabledNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    if crate::platform::binder::get_high_touch_polling_rate_enabled().unwrap_or(false) {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setHighTouchPollingRateEnabledNative(
    _env: JNIEnv,
    _class: JClass,
    enabled: jni::sys::jboolean,
) -> jni::sys::jboolean {
    let val = enabled != jni::sys::JNI_FALSE;
    match crate::platform::binder::set_high_touch_polling_rate_enabled(val) {
        Ok(s) if s => jni::sys::JNI_TRUE,
        _ => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalStatusNative(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    crate::platform::binder::get_thermal_status()
        .map(|s| s as i32 as jint)
        .unwrap_or(0)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getThermalHeadroomNative(
    _env: JNIEnv,
    _class: JClass,
    forecast_seconds: jint,
) -> jfloat {
    crate::platform::binder::get_thermal_headroom(forecast_seconds as i32).unwrap_or(f32::NAN)
        as jfloat
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isChargingControlSupportedNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    if crate::platform::binder::is_charging_control_supported() {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getChargingEnabledNative(
    _env: JNIEnv,
    _class: JClass,
) -> jni::sys::jboolean {
    if crate::platform::binder::get_charging_enabled().unwrap_or(true) {
        jni::sys::JNI_TRUE
    } else {
        jni::sys::JNI_FALSE
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setChargingEnabledNative(
    _env: JNIEnv,
    _class: JClass,
    enabled: jni::sys::jboolean,
) -> jni::sys::jboolean {
    let val = enabled != jni::sys::JNI_FALSE;
    match crate::platform::binder::set_charging_enabled(val) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}
