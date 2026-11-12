use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::jstring;

use crate::bridge::create_jstring_safe;
use crate::platform::system;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getSystemInfoNative(
    mut env: EnvUnowned,
    _class: JClass,
) -> jstring {
    let info = system::scan_system_subsystem();
    let json = serde_json::to_string(&info).unwrap_or_else(|_| "{}".to_string());
    create_jstring_safe(&mut env, &json)
}
