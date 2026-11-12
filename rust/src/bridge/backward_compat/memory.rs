use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::{jfloat, jint, jlong, jstring};

use crate::bridge::create_jstring_safe;
use crate::platform::memory;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readMemInfoNative(
    mut env: EnvUnowned,
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
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramSizeNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jlong {
    memory::read_zram_stats().disksize
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressionRatioNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    memory::read_zram_stats().compression_ratio
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramCompressedSizeNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jlong {
    memory::read_zram_stats().compr_data_size
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramOrigDataSizeNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jlong {
    memory::read_zram_stats().orig_data_size
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getZramAlgorithmNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&mut env, &memory::get_current_zram_algorithm())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getAvailableZramAlgorithmsNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let algos = memory::get_available_zram_algorithms();
    let json = serde_json::to_string(&algos).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSwappinessNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jint {
    memory::get_swappiness()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getMemoryPressureNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    let (mem, _) = memory::read_memory_and_swap();
    if mem.total_kb > 0 {
        let used = mem.total_kb.saturating_sub(mem.available_kb);
        ((used as f32 / mem.total_kb as f32) * 100.0).clamp(0.0, 100.0)
    } else {
        0.0
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readZramDeviceStatsNative(
    mut env: EnvUnowned,
    _class: JClass,
    _device: jint,
) -> jstring {
    let stats = memory::read_zram_stats();
    let json = serde_json::to_string(&stats).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(&mut env, &json)
}
