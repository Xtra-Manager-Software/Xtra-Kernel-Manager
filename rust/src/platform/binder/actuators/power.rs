use anyhow::Result;

pub const POWER_SERVICE: &str = "power";

pub fn is_power_save_mode() -> Result<bool> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return Ok(false);
        }
        let svc = match rsbinder::hub::check_service(POWER_SERVICE) {
            Some(s) => s,
            None => return Ok(false),
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => return Ok(false),
        };

        // TRANSACTION_isPowerSaveMode (usually 14 or 15)
        let data = proxy.prepare_transact(true)?;
        let reply_opt = proxy.submit_transact(15, &data, 0)?;
        if let Some(mut rep) = reply_opt {
            let ex = rep.read_i32()?;
            if ex == 0 {
                return Ok(rep.read_bool().unwrap_or(false));
            }
        }
        Ok(false)
    }

    #[cfg(not(target_os = "android"))]
    {
        Ok(false)
    }
}

pub fn set_power_save_mode_enabled(enable: bool) -> Result<bool> {
    #[cfg(target_os = "android")]
    {
        // 1. Direct system command
        let _ = std::process::Command::new("cmd")
            .args(["power", "set-mode", if enable { "1" } else { "0" }])
            .status();

        // 2. Direct Binder IPC
        if crate::platform::binder::ensure_initialized() {
            if let Some(svc) = rsbinder::hub::check_service(POWER_SERVICE) {
                if let Some(proxy) = svc.as_proxy() {
                    let mut data = proxy.prepare_transact(true)?;
                    data.write(&enable)?;
                    let _ = proxy.submit_transact(17, &data, 0);
                }
            }
        }
        Ok(enable)
    }

    #[cfg(not(target_os = "android"))]
    {
        Ok(enable)
    }
}
