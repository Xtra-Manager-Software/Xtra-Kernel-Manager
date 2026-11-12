use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jint, jstring};

use crate::bridge::create_jstring_safe;
use crate::logging::{self, LevelFilter};

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getNativeLogsNative(
    env: JNIEnv,
    _class: JClass,
    max_count: jint,
) -> jstring {
    let count = if max_count <= 0 {
        100
    } else {
        max_count as usize
    };
    let json = logging::get_recent_logs_json(count);
    create_jstring_safe(&env, &json)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setNativeLogLevelNative(
    _env: JNIEnv,
    _class: JClass,
    level: jint,
) {
    let lvl = LevelFilter::from_u8(level.clamp(0, 5) as u8);
    logging::set_max_level(lvl);
}
