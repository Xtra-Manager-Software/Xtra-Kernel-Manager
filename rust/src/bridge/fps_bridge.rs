use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jfloat, jint, jstring};

use super::{create_jstring_safe, jstring_to_string};

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
        Some(reading) => serde_json::to_string(&reading).unwrap_or_default(),
        None => String::new(),
    };
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_attachFpsPidNative(
    _env: JNIEnv,
    _class: JClass,
    pid: jint,
) -> jni::sys::jboolean {
    match crate::platform::fps::attach_fps_pid(pid) {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
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
    match crate::platform::fps::detach_fps_pid() {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setFpsTargetPackageNative(
    mut env: JNIEnv,
    _class: JClass,
    pkg: JString,
) {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    match pkg_str.trim() {
        "" => crate::platform::fps::set_target_package(None),
        s => crate::platform::fps::set_target_package(Some(s.to_string())),
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
    match crate::platform::fps::is_ebpf_attached() {
        true => jni::sys::JNI_TRUE,
        false => jni::sys::JNI_FALSE,
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
    match target {
        t if t > 0.0 => crate::platform::fps::set_target_fps(Some(t as f64)),
        _ => crate::platform::fps::set_target_fps(None),
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
