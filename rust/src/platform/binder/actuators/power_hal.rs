use anyhow::Result;
use serde::{Deserialize, Serialize};
use std::sync::atomic::{AtomicU32, Ordering};

pub const POWER_HAL_SERVICE: &str = "android.hardware.power.IPower/default";

static ACTIVE_POWER_MODES: AtomicU32 = AtomicU32::new(0);

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[repr(i32)]
pub enum PowerMode {
    DoubleTapToWake = 0,
    LowPower = 1,
    SustainedPerformance = 2,
    FixedPerformance = 3,
    Vr = 4,
    DeviceIdle = 5,
    MultiWindow = 6,
    ExpensiveRendering = 7,
    Interactive = 8,
    Game = 9,
    AudioStreamingLowLatency = 10,
    CameraStreamingSecure = 11,
    GameLoading = 12,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[repr(i32)]
pub enum PowerBoost {
    Interaction = 0,
    DisplayUpdateImmediate = 1,
    MlAccelerator = 2,
    AudioLaunch = 3,
    CameraLaunch = 4,
    CameraShot = 5,
}

pub fn is_power_mode_supported(mode: PowerMode) -> bool {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return false;
        }
        let svc = match rsbinder::hub::check_service(POWER_HAL_SERVICE) {
            Some(s) => s,
            None => return false,
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => return false,
        };
        // TRANSACTION_isModeSupported = 2
        let Ok(mut data) = proxy.prepare_transact(true) else {
            return false;
        };
        if data.write(&(mode as i32)).is_err() {
            return false;
        }
        let Ok(Some(mut reply)) = proxy.submit_transact(2, &data, 0) else {
            return false;
        };
        if let Ok(ex) = reply.read_i32() {
            if ex != 0 {
                return false;
            }
        }
        reply.read_bool().unwrap_or(false)
    }

    #[cfg(not(target_os = "android"))]
    {
        matches!(
            mode,
            PowerMode::SustainedPerformance
                | PowerMode::FixedPerformance
                | PowerMode::ExpensiveRendering
                | PowerMode::Game
        )
    }
}

pub fn set_power_mode(mode: PowerMode, enable: bool) -> Result<()> {
    let bit = 1u32 << (mode as u32);
    let prev = ACTIVE_POWER_MODES.load(Ordering::Relaxed);
    let is_active = (prev & bit) != 0;
    if is_active == enable {
        return Ok(());
    }

    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            anyhow::bail!("Binder ProcessState not initialized");
        }
        let svc = match rsbinder::hub::check_service(POWER_HAL_SERVICE) {
            Some(s) => s,
            None => anyhow::bail!("PowerHAL AIDL service not found"),
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => anyhow::bail!("PowerHAL AIDL service is not a proxy"),
        };
        // TRANSACTION_setMode = 1
        let mut data = proxy.prepare_transact(true)?;
        data.write(&(mode as i32))?;
        data.write(&enable)?;

        let reply_opt = proxy.submit_transact(1, &data, 0)?;
        if let Some(mut rep) = reply_opt {
            let ex = rep.read_i32()?;
            if ex != 0 {
                anyhow::bail!("setMode exception: {}", ex);
            }
        }
    }

    if enable {
        ACTIVE_POWER_MODES.fetch_or(bit, Ordering::Relaxed);
    } else {
        ACTIVE_POWER_MODES.fetch_and(!bit, Ordering::Relaxed);
    }

    Ok(())
}

pub fn set_power_boost(boost: PowerBoost, duration_ms: i32) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return Ok(());
        }
        let svc = match rsbinder::hub::check_service(POWER_HAL_SERVICE) {
            Some(s) => s,
            None => return Ok(()),
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => return Ok(()),
        };
        // TRANSACTION_setBoost = 3
        let mut data = proxy.prepare_transact(true)?;
        data.write(&(boost as i32))?;
        data.write(&duration_ms)?;
        let _ = proxy.submit_transact(3, &data, 0);
    }

    #[cfg(not(target_os = "android"))]
    {
        let _ = (boost, duration_ms);
    }

    Ok(())
}
