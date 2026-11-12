use anyhow::Result;
#[cfg(target_os = "android")]
use std::sync::Mutex;

#[cfg(target_os = "android")]
#[derive(Debug, Default, Clone)]
struct DisplayStateSnapshot {
    last_applied_hz: Option<u32>,
    saved_min_hz: Option<f32>,
    saved_peak_hz: Option<f32>,
    snapshot_taken: bool,
}

#[cfg(target_os = "android")]
static DISPLAY_STATE: Mutex<DisplayStateSnapshot> = Mutex::new(DisplayStateSnapshot {
    last_applied_hz: None,
    saved_min_hz: None,
    saved_peak_hz: None,
    snapshot_taken: false,
});

#[cfg(target_os = "android")]
static DISPLAY_HZ_CACHE: Mutex<Option<(f64, std::time::Instant)>> = Mutex::new(None);

/// Query current active display refresh rate in Hz.
pub fn get_active_display_refresh_rate() -> f64 {
    #[cfg(target_os = "android")]
    {
        if let Ok(guard) = DISPLAY_HZ_CACHE.lock() {
            if let Some((hz, instant)) = *guard {
                if instant.elapsed() < std::time::Duration::from_millis(500) {
                    return hz;
                }
            }
        }

        let hz = match get_user_preferred_display_mode() {
            Ok(Some(h)) if h > 0.0 => h as f64,
            _ => 120.0,
        };

        if let Ok(mut guard) = DISPLAY_HZ_CACHE.lock() {
            *guard = Some((hz, std::time::Instant::now()));
        }
        hz
    }

    #[cfg(not(target_os = "android"))]
    {
        120.0
    }
}

/// Query user preferred display mode via Native Binder (code 47).
pub fn get_user_preferred_display_mode() -> Result<Option<f32>> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return Ok(None);
        }
        let display_svc = match rsbinder::hub::check_service("display") {
            Some(s) => s,
            None => return Ok(None),
        };
        let proxy = match display_svc.as_proxy() {
            Some(p) => p,
            None => return Ok(None),
        };
        let mut data = proxy.prepare_transact(true)?;
        data.write(&0i32)?; // displayId = 0
        let reply_opt = proxy.submit_transact(47, &data, 0)?;
        if let Some(mut rep) = reply_opt {
            let ex = rep.read_i32()?;
            if ex != 0 {
                return Ok(None);
            }
            let is_null = rep.read_i32()?;
            if is_null == 0 {
                return Ok(None);
            }
            rep.set_data_position(rep.data_position() + 24);
            let hz = rep.read_f32()?;
            return Ok(Some(hz));
        }
        Ok(None)
    }

    #[cfg(not(target_os = "android"))]
    {
        Ok(Some(120.0))
    }
}

/// Query supported refresh rates (e.g. [60, 90, 120]).
pub fn query_binder_supported_rates() -> Vec<u32> {
    vec![60, 90, 120]
}

/// Apply display refresh rate override (in Hz).
/// Passing `0` restores user's original display preferences.
pub fn apply_refresh_rate(rate_hz: u32) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        let mut state = DISPLAY_STATE
            .lock()
            .map_err(|_| anyhow::anyhow!("DisplayState lock poisoned"))?;

        if state.last_applied_hz == Some(rate_hz) {
            return Ok(());
        }

        if let Ok(mut guard) = DISPLAY_HZ_CACHE.lock() {
            match rate_hz {
                0 => *guard = None,
                hz => *guard = Some((hz as f64, std::time::Instant::now())),
            }
        }

        // Direct Native Binder RPC to DisplayManagerService (code 45)
        let _ = set_binder_display_mode(rate_hz as f32);

        // Sync with settings for framework compatibility
        if rate_hz == 0 {
            if let Some(peak) = state.saved_peak_hz {
                let _ = std::process::Command::new("cmd")
                    .args([
                        "settings",
                        "put",
                        "system",
                        "peak_refresh_rate",
                        &peak.to_string(),
                    ])
                    .status();
            }
            if let Some(min) = state.saved_min_hz {
                let _ = std::process::Command::new("cmd")
                    .args([
                        "settings",
                        "put",
                        "system",
                        "min_refresh_rate",
                        &min.to_string(),
                    ])
                    .status();
            }
        } else {
            if !state.snapshot_taken {
                state.saved_peak_hz = read_settings_float("peak_refresh_rate");
                state.saved_min_hz = read_settings_float("min_refresh_rate");
                state.snapshot_taken = true;
            }

            let hz_str = (rate_hz as f32).to_string();
            let _ = std::process::Command::new("cmd")
                .args(["settings", "put", "system", "peak_refresh_rate", &hz_str])
                .status();
            let _ = std::process::Command::new("cmd")
                .args(["settings", "put", "system", "min_refresh_rate", &hz_str])
                .status();
        }

        state.last_applied_hz = Some(rate_hz);
    }

    #[cfg(not(target_os = "android"))]
    {
        let _ = rate_hz;
    }

    Ok(())
}

#[cfg(target_os = "android")]
fn set_binder_display_mode(hz: f32) -> Result<()> {
    if !crate::platform::binder::ensure_initialized() {
        anyhow::bail!("Binder ProcessState not initialized");
    }
    let display_svc = match rsbinder::hub::check_service("display") {
        Some(s) => s,
        None => anyhow::bail!("display service not found"),
    };
    let proxy = match display_svc.as_proxy() {
        Some(p) => p,
        None => anyhow::bail!("display service is not a proxy"),
    };

    let mut data = proxy.prepare_transact(true)?;
    data.write(&0i32)?; // displayId = 0 (primary display)

    if hz <= 0.0 {
        data.write(&0i32)?; // null mode
    } else {
        data.write(&1i32)?;
        data.write(&-1i32)?; // modeId = -1
        data.write(&-1i32)?; // physicalWidth = -1
        data.write(&-1i32)?; // physicalHeight = -1
        data.write(&0i32)?; // flags = 0
        data.write(&-1i32)?; // width = -1
        data.write(&-1i32)?; // height = -1
        data.write(&hz)?; // refreshRate = hz
        data.write(&hz)?; // vsyncRate = hz
        data.write(&0i32)?; // alternativeRefreshRates len = 0
        data.write(&0i32)?; // supportedHdrTypes len = 0
    }

    data.write(&false)?; // storeMode = false
    let reply = proxy.submit_transact(45, &data, 0)?;
    if let Some(mut rep) = reply {
        let ex = rep.read_i32()?;
        if ex != 0 {
            anyhow::bail!("setUserPreferredDisplayMode exception {}", ex);
        }
    }
    Ok(())
}

/// Set screen brightness level (0.0 to 1.0, or 0 to 255 scaled).
pub fn set_screen_brightness(level: f32) -> Result<()> {
    let clamped = level.clamp(0.0, 1.0);
    let int_val = (clamped * 255.0).round() as u32;

    #[cfg(target_os = "android")]
    {
        // 1. Direct system settings write
        let _ = std::process::Command::new("settings")
            .args(["put", "system", "screen_brightness", &int_val.to_string()])
            .status();

        // 2. Try direct DisplayManager IPC
        if crate::platform::binder::ensure_initialized() {
            if let Some(svc) = rsbinder::hub::check_service("display") {
                if let Some(proxy) = svc.as_proxy() {
                    // TRANSACTION_setTemporaryScreenBrightness = 48 (or 49)
                    if let Ok(mut data) = proxy.prepare_transact(true) {
                        let _ = data.write(&0i32); // displayId = 0
                        let _ = data.write(&clamped);
                        let _ = proxy.submit_transact(48, &data, 0);
                    }
                }
            }
        }
    }

    #[cfg(not(target_os = "android"))]
    {
        let _ = int_val;
    }

    Ok(())
}

/// Get current screen brightness (0.0 to 1.0).
pub fn get_screen_brightness() -> Option<f32> {
    #[cfg(target_os = "android")]
    {
        read_settings_float("screen_brightness").map(|v| v / 255.0)
    }

    #[cfg(not(target_os = "android"))]
    {
        Some(0.8)
    }
}

#[cfg(target_os = "android")]
fn read_settings_float(key: &str) -> Option<f32> {
    let out = std::process::Command::new("settings")
        .args(["get", "system", key])
        .output()
        .ok()?;
    let text = String::from_utf8_lossy(&out.stdout).trim().to_string();
    text.parse::<f32>().ok()
}
