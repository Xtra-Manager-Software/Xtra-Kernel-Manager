use jni::EnvUnowned;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jstring};

use crate::bridge::{create_jstring_safe, jstring_to_string};
use crate::platform::binder;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getForegroundAppNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let pkg = binder::get_foreground_app()
        .map(|f| f.package_name)
        .unwrap_or_default();
    create_jstring_safe(&mut env, &pkg)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_killBackgroundProcessesNative(
    mut env: EnvUnowned,
    _class: JClass,
    pkg: JString,
) -> jboolean {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.is_empty() {
        return false;
    }
    binder::kill_background_processes(&pkg_str, None).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_killAllBackgroundProcessesNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::kill_all_background_processes().is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_forceStopPackageNative(
    mut env: EnvUnowned,
    _class: JClass,
    pkg: JString,
) -> jboolean {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.is_empty() {
        return false;
    }
    binder::force_stop_package(&pkg_str, None).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_killPidsNative(
    _env: EnvUnowned,
    _class: JClass,
    pid: jint,
) -> jboolean {
    if pid <= 0 {
        return false;
    }
    binder::kill_pids(&[pid], "NativeTaskKill").is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isProcessFrozenNative(
    _env: EnvUnowned,
    _class: JClass,
    pid: jint,
) -> jboolean {
    if pid <= 0 {
        return false;
    }
    binder::is_process_frozen(pid).unwrap_or(false)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_trimProcessMemoryNative(
    mut env: EnvUnowned,
    _class: JClass,
    process: JString,
    uid: jint,
    level: jint,
) -> jboolean {
    let proc_str = jstring_to_string(&mut env, &process).unwrap_or_default();
    if proc_str.is_empty() {
        return false;
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
    binder::set_process_memory_trim_level(&proc_str, uid, trim_lvl).unwrap_or(false)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setAppStandbyBucketNative(
    mut env: EnvUnowned,
    _class: JClass,
    pkg: JString,
    bucket: jint,
) -> jboolean {
    let pkg_str = jstring_to_string(&mut env, &pkg).unwrap_or_default();
    if pkg_str.is_empty() {
        return false;
    }
    let b = binder::StandbyBucket::from_i32(bucket);
    binder::set_app_standby_bucket(&pkg_str, b, None).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getAppStandbyBucketNative(
    mut env: EnvUnowned,
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
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let list = binder::get_running_process_list().unwrap_or_default();
    let json = serde_json::to_string(&list).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&mut env, &json)
}
