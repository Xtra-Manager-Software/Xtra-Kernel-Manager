#[allow(unused_imports)]
use anyhow::{Context, Result, bail};
#[cfg(target_os = "android")]
use std::fs;
#[cfg(target_os = "android")]
use std::path::Path;
use std::sync::atomic::{AtomicBool, Ordering};

pub const HIGH_TOUCH_SERVICE: &str = "vendor.lineage.touch.IHighTouchPollingRate/default";
pub const HIGH_TOUCH_DESCRIPTOR: &str = "vendor.lineage.touch.IHighTouchPollingRate";

// Transaction codes for IHighTouchPollingRate AIDL
pub const TRANSACTION_GET_ENABLED: u32 = 1;
pub const TRANSACTION_SET_ENABLED: u32 = 2;

// OEM touch game mode nodes: (path, performance_value, balance_value)
#[allow(dead_code)]
const TOUCH_PATHS: &[(&str, &str, &str)] = &[
    // Xiaomi / POCO (bump_sample_rate)
    (
        "/sys/devices/virtual/touch/touch_dev/bump_sample_rate",
        "1",
        "0",
    ),
    ("/sys/class/touch/touch_dev/bump_sample_rate", "1", "0"),
    // OnePlus / OPPO / Realme (OPlus touchpanel)
    ("/proc/touchpanel/game_switch_enable", "1", "0"),
    ("/proc/touchpanel/oplus_tp_limit_enable", "0", "1"),
    ("/proc/touchpanel/oppo_tp_limit_enable", "0", "1"),
    ("/proc/touchpanel/oplus_tp_direction", "1", "0"),
    ("/proc/touchpanel/oppo_tp_direction", "1", "0"),
    // Black Shark
    ("/sys/devices/virtual/touch/touch_dev/game_mode", "1", "0"),
    // ASUS ROG Phone
    ("/sys/class/sensors/g_sensor/game_mode", "1", "0"),
];

static ACTIVE_TOUCH_POLLING: AtomicBool = AtomicBool::new(false);

#[cfg(target_os = "android")]
fn query_hal_touch_enabled() -> Result<bool> {
    if !crate::platform::binder::ensure_initialized() {
        bail!("Binder not initialized");
    }
    let svc =
        rsbinder::hub::check_service(HIGH_TOUCH_SERVICE).context("HighTouch service not found")?;
    let proxy = svc.as_proxy().context("Failed to get proxy")?;
    let data = proxy.prepare_transact(true)?;
    let mut reply = proxy
        .submit_transact(TRANSACTION_GET_ENABLED, &data, 0)?
        .context("Empty reply")?;

    match reply.read_i32()? {
        0 => Ok(reply.read_bool()?),
        code => bail!("HAL status non-zero: {code}"),
    }
}

#[cfg(target_os = "android")]
fn send_hal_touch_enabled(enabled: bool) -> Result<()> {
    if !crate::platform::binder::ensure_initialized() {
        bail!("Binder not initialized");
    }
    let svc =
        rsbinder::hub::check_service(HIGH_TOUCH_SERVICE).context("HighTouch service not found")?;
    let proxy = svc.as_proxy().context("Failed to get proxy")?;
    let mut data = proxy.prepare_transact(true)?;
    data.write(&enabled)?;
    let mut reply = proxy
        .submit_transact(TRANSACTION_SET_ENABLED, &data, 0)?
        .context("Empty reply")?;

    match reply.read_i32()? {
        0 => Ok(()),
        code => bail!("HAL set_enabled error code: {code}"),
    }
}

/// Check if High Touch Polling Rate is supported via LineageOS/AOSP Touch HAL or OEM sysfs nodes.
pub fn is_high_touch_polling_rate_supported() -> bool {
    #[cfg(target_os = "android")]
    {
        if crate::platform::binder::ensure_initialized()
            && rsbinder::hub::check_service(HIGH_TOUCH_SERVICE).is_some()
        {
            return true;
        }
        TOUCH_PATHS
            .iter()
            .any(|(path, _, _)| Path::new(path).exists())
    }
    #[cfg(not(target_os = "android"))]
    {
        true
    }
}

/// Query whether high touch polling rate (e.g. 240Hz/480Hz) is currently active.
pub fn get_high_touch_polling_rate_enabled() -> Result<bool> {
    #[cfg(target_os = "android")]
    {
        // Many OEM kernel drivers (e.g. Xiaomi touch_dev) are write-only and return -EINVAL on read.
        // First try reading Binder HAL; if unsupported or failing, return cached state.
        if let Ok(val) = query_hal_touch_enabled() {
            ACTIVE_TOUCH_POLLING.store(val, Ordering::Relaxed);
            return Ok(val);
        }
        Ok(ACTIVE_TOUCH_POLLING.load(Ordering::Relaxed))
    }
    #[cfg(not(target_os = "android"))]
    {
        Ok(ACTIVE_TOUCH_POLLING.load(Ordering::Relaxed))
    }
}

/// Set high touch polling rate state via Native Binder AIDL and OEM sysfs fallback.
pub fn set_high_touch_polling_rate_enabled(enabled: bool) -> Result<bool> {
    ACTIVE_TOUCH_POLLING.store(enabled, Ordering::Relaxed);

    #[cfg(target_os = "android")]
    {
        let mut applied = send_hal_touch_enabled(enabled).is_ok();

        // Also write to OEM sysfs nodes for complete hardware coverage
        for &(path, perf_val, bal_val) in TOUCH_PATHS {
            if Path::new(path).exists() {
                let v = if enabled { perf_val } else { bal_val };
                if fs::write(path, v).is_ok() {
                    applied = true;
                }
            }
        }

        Ok(applied)
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = enabled;
        Ok(true)
    }
}
