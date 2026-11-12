use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::{jboolean, jfloat};

use crate::platform::binder;

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getActiveDisplayRefreshRateNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jfloat {
    binder::get_active_display_refresh_rate() as jfloat
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_applyRefreshRateNative(
    _env: EnvUnowned,
    _class: JClass,
    rate: jfloat,
) -> jboolean {
    binder::apply_refresh_rate(rate as u32).is_ok()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_isHighTouchPollingRateSupportedNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::is_high_touch_polling_rate_supported()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_getHighTouchPollingRateEnabledNative(
    _env: EnvUnowned,
    _class: JClass,
) -> jboolean {
    binder::get_high_touch_polling_rate_enabled().unwrap_or(false)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_id_xms_xtrakernelmanager_domain_native_NativeLib_setHighTouchPollingRateEnabledNative(
    _env: EnvUnowned,
    _class: JClass,
    enabled: jboolean,
) -> jboolean {
    binder::set_high_touch_polling_rate_enabled(enabled).unwrap_or(false)
}
