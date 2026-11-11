use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong};
use jni::JNIEnv;

use super::jstring_to_string;
use crate::platform::{cpu, memory};
use crate::preset;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_applyPresetNative(
    mut env: JNIEnv,
    _class: JClass,
    preset_name: JString,
) -> jboolean {
    if let Some(name) = jstring_to_string(&mut env, &preset_name) {
        let result = preset::apply_preset(&name);
        (result.is_ok()) as jboolean
    } else {
        0
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_restoreDefaultsNative(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    (preset::restore_defaults().is_ok()) as jboolean
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_lockCpuFrequencyNative(
    _env: JNIEnv,
    _class: JClass,
    cluster: jint,
    freq_khz: jlong,
) -> jboolean {
    (cpu::lock_cluster_frequency(cluster, freq_khz as u64).is_ok()) as jboolean
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_unlockCpuFrequencyNative(
    _env: JNIEnv,
    _class: JClass,
    cluster: jint,
) -> jboolean {
    (cpu::unlock_cluster_frequency(cluster).is_ok()) as jboolean
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setGovernorNative(
    mut env: JNIEnv,
    _class: JClass,
    cluster: jint,
    governor: JString,
) -> jboolean {
    if let Some(gov) = jstring_to_string(&mut env, &governor) {
        (cpu::set_governor_for_cluster(cluster, &gov).is_ok()) as jboolean
    } else {
        0
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setSwappinessNative(
    _env: JNIEnv,
    _class: JClass,
    swappiness: jint,
) -> jboolean {
    (memory::set_swappiness(swappiness).is_ok()) as jboolean
}
