use jni::objects::JClass;
use jni::sys::jstring;
use jni::JNIEnv;

use super::create_jstring_safe;
use crate::platform::binder;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getForegroundAppNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let pkg = binder::get_foreground_app()
        .map(|f| f.package_name)
        .unwrap_or_default();
    create_jstring_safe(&env, &pkg)
}
