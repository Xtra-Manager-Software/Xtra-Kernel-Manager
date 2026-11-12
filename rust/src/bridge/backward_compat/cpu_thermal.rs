use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::{jfloat, jint, jstring};

use crate::bridge::create_jstring_safe;
use crate::platform::{cpu, thermal};

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_detectCpuClustersNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let clusters = cpu::detect_cpu_clusters();
    let json = serde_json::to_string(&clusters).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreDataNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let cores = cpu::read_core_data();
    let json = serde_json::to_string(&cores).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&mut env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuLoadNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    cpu::read_cpu_load()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCpuTemperatureNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    thermal::read_cpu_temperature()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_readCoreTemperatureNative(
    _env: EnvUnowned,
    _class: JClass,
    _core: jint,
) -> jfloat {
    thermal::read_cpu_temperature()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getCpuModelNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    create_jstring_safe(&mut env, cpu::get_cpu_model())
}
