#[allow(unused_imports)]
use anyhow::{Context, Result, bail};
use serde::{Deserialize, Serialize};

pub const THERMAL_SERVICE: &str = "thermalservice";
pub const THERMAL_DESCRIPTOR: &str = "android.os.IThermalService";

// Transaction codes for android.os.IThermalService
pub const TRANSACTION_GET_THERMAL_HEADROOM: u32 = 8;
pub const TRANSACTION_GET_CURRENT_THERMAL_STATUS: u32 = 9;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[repr(i32)]
pub enum ThermalStatus {
    None = 0,
    Light = 1,
    Moderate = 2,
    Severe = 3,
    Critical = 4,
    Emergency = 5,
    Shutdown = 6,
}

impl ThermalStatus {
    pub fn from_i32(val: i32) -> Self {
        match val {
            0 => Self::None,
            1 => Self::Light,
            2 => Self::Moderate,
            3 => Self::Severe,
            4 => Self::Critical,
            5 => Self::Emergency,
            6 => Self::Shutdown,
            _ => Self::None,
        }
    }

    pub fn is_throttling(&self) -> bool {
        matches!(
            self,
            Self::Severe | Self::Critical | Self::Emergency | Self::Shutdown
        )
    }
}

/// Query current system-wide thermal status from IThermalService.
pub fn get_thermal_status() -> Result<ThermalStatus> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc =
            rsbinder::hub::check_service(THERMAL_SERVICE).context("IThermalService not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let data = proxy.prepare_transact(true)?;

        let reply = proxy.submit_transact(TRANSACTION_GET_CURRENT_THERMAL_STATUS, &data, 0)?;
        if let Some(mut r) = reply {
            if let Ok(0) = r.read_i32() {
                let status_val = r.read_i32().unwrap_or(0);
                return Ok(ThermalStatus::from_i32(status_val));
            }
        }
        bail!("Failed to read thermal status")
    }
    #[cfg(not(target_os = "android"))]
    {
        Ok(ThermalStatus::None)
    }
}

/// Query predicted thermal headroom for next forecast_seconds (0 to 60s).
/// Returns a ratio where:
/// - < 1.0 = Safe (no throttling)
/// - 1.0 = Severe throttling threshold reached
/// - > 1.0 = In active throttling
pub fn get_thermal_headroom(forecast_seconds: i32) -> Result<f32> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc =
            rsbinder::hub::check_service(THERMAL_SERVICE).context("IThermalService not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&forecast_seconds)?;

        let reply = proxy.submit_transact(TRANSACTION_GET_THERMAL_HEADROOM, &data, 0)?;
        if let Some(mut r) = reply {
            if let Ok(0) = r.read_i32() {
                let headroom = r.read_f32().unwrap_or(f32::NAN);
                return Ok(headroom);
            }
        }
        Ok(f32::NAN)
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = forecast_seconds;
        Ok(0.5f32)
    }
}
