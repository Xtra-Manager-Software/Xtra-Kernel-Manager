use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring};

use super::{create_jstring_safe, jstring_to_string};
use crate::platform::modules;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_listLoadedModulesNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let mods = modules::list_loaded_modules();
    let json = serde_json::to_string(&mods).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_loadKernelModuleNative(
    mut env: JNIEnv,
    _class: JClass,
    path: JString,
    params: JString,
) -> jboolean {
    let path_str = jstring_to_string(&mut env, &path).unwrap_or_default();
    let params_str = jstring_to_string(&mut env, &params).unwrap_or_default();

    if path_str.is_empty() {
        return jni::sys::JNI_FALSE;
    }

    match modules::load_kernel_module(&path_str, &params_str) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_unloadKernelModuleNative(
    mut env: JNIEnv,
    _class: JClass,
    name: JString,
) -> jboolean {
    let name_str = jstring_to_string(&mut env, &name).unwrap_or_default();
    if name_str.is_empty() {
        return jni::sys::JNI_FALSE;
    }

    match modules::unload_kernel_module(&name_str) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getModuleParametersNative(
    mut env: JNIEnv,
    _class: JClass,
    name: JString,
) -> jstring {
    let name_str = jstring_to_string(&mut env, &name).unwrap_or_default();
    if name_str.is_empty() {
        return create_jstring_safe(&env, "{}");
    }

    let params = modules::get_module_parameters(&name_str);
    let json = serde_json::to_string(&params).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setModuleParameterNative(
    mut env: JNIEnv,
    _class: JClass,
    module: JString,
    param: JString,
    value: JString,
) -> jboolean {
    let mod_str = jstring_to_string(&mut env, &module).unwrap_or_default();
    let param_str = jstring_to_string(&mut env, &param).unwrap_or_default();
    let val_str = jstring_to_string(&mut env, &value).unwrap_or_default();

    if mod_str.is_empty() || param_str.is_empty() {
        return jni::sys::JNI_FALSE;
    }

    match modules::set_module_parameter(&mod_str, &param_str, &val_str) {
        Ok(_) => jni::sys::JNI_TRUE,
        Err(_) => jni::sys::JNI_FALSE,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_listKernelDriversNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let drivers = modules::list_kernel_drivers();
    let json = serde_json::to_string(&drivers).unwrap_or_else(|_| "[]".to_string());
    create_jstring_safe(&env, &json)
}
