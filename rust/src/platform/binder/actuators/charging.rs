#[allow(unused_imports)]
use anyhow::{Context, Result, bail};
#[allow(unused_imports)]
use std::sync::atomic::{AtomicBool, Ordering};

pub const CHARGING_CONTROL_SERVICE: &str = "vendor.lineage.health.IChargingControl/default";
pub const CHARGING_CONTROL_DESCRIPTOR: &str = "vendor.lineage.health.IChargingControl";

// Transaction codes for IChargingControl AIDL
// FIRST_CALL_TRANSACTION + 0 = getChargingEnabled
// FIRST_CALL_TRANSACTION + 1 = setChargingEnabled
// FIRST_CALL_TRANSACTION + 2 = setChargingDeadline
// FIRST_CALL_TRANSACTION + 3 = setChargingLimit
// FIRST_CALL_TRANSACTION + 4 = getChargingLimit
pub const TRANSACTION_GET_CHARGING_ENABLED: u32 = 1;
pub const TRANSACTION_SET_CHARGING_ENABLED: u32 = 2;
pub const TRANSACTION_SET_CHARGING_LIMIT: u32 = 4;
pub const TRANSACTION_GET_CHARGING_LIMIT: u32 = 5;

#[cfg(not(target_os = "android"))]
static MOCK_CHARGING_ENABLED: AtomicBool = AtomicBool::new(true);

/// Check if the hardware supports hardware-level Charging Control (Bypass charging / limit).
pub fn is_charging_control_supported() -> bool {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return false;
        }
        rsbinder::hub::check_service(CHARGING_CONTROL_SERVICE).is_some()
    }
    #[cfg(not(target_os = "android"))]
    {
        true
    }
}

/// Query whether charging current is actively entering the battery.
/// If false, device is in Bypass Charging / Idle Mode (powered directly from AC).
pub fn get_charging_enabled() -> Result<bool> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(CHARGING_CONTROL_SERVICE)
            .context("ChargingControl service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let data = proxy.prepare_transact(true)?;

        let reply = proxy.submit_transact(TRANSACTION_GET_CHARGING_ENABLED, &data, 0)?;
        match reply {
            Some(mut r) => match r.read_i32() {
                Ok(0) => Ok(r.read_bool().unwrap_or(true)),
                _ => bail!("Failed to read charging state"),
            },
            None => bail!("Failed to read charging state"),
        }
    }
    #[cfg(not(target_os = "android"))]
    {
        Ok(MOCK_CHARGING_ENABLED.load(Ordering::Relaxed))
    }
}

/// Set charging enabled. Setting this to false enables Bypass Charging / Idle Mode!
pub fn set_charging_enabled(enabled: bool) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(CHARGING_CONTROL_SERVICE)
            .context("ChargingControl service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&enabled)?;

        let reply = proxy.submit_transact(TRANSACTION_SET_CHARGING_ENABLED, &data, 0)?;
        if let Some(mut r) = reply {
            if let Ok(ex) = r.read_i32() {
                if ex != 0 {
                    bail!("Service returned exception {}", ex);
                }
            }
        }
        Ok(())
    }
    #[cfg(not(target_os = "android"))]
    {
        MOCK_CHARGING_ENABLED.store(enabled, Ordering::Relaxed);
        Ok(())
    }
}
