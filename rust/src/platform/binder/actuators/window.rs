use anyhow::Result;

pub const WINDOW_SERVICE: &str = "window";

pub const TRANSACTION_FREEZE_DISPLAY_ROTATION: u32 = 59;
pub const TRANSACTION_THAW_DISPLAY_ROTATION: u32 = 60;
pub const TRANSACTION_IS_DISPLAY_ROTATION_FROZEN: u32 = 61;

pub fn freeze_display_rotation(display_id: i32, rotation: i32) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            anyhow::bail!("Binder ProcessState not initialized");
        }
        let svc = match rsbinder::hub::check_service(WINDOW_SERVICE) {
            Some(s) => s,
            None => anyhow::bail!("window service not found"),
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => anyhow::bail!("window service is not a proxy"),
        };

        let mut data = proxy.prepare_transact(true)?;
        data.write(&display_id)?;
        data.write(&rotation)?;
        data.write(&"id.xms.xtrakernelmanager".to_string())?;

        let reply_opt = proxy.submit_transact(TRANSACTION_FREEZE_DISPLAY_ROTATION, &data, 0)?;
        if let Some(mut rep) = reply_opt {
            let ex = rep.read_i32()?;
            if ex != 0 {
                anyhow::bail!("freezeDisplayRotation exception: {}", ex);
            }
        }
        Ok(())
    }

    #[cfg(not(target_os = "android"))]
    {
        let _ = (display_id, rotation);
        Ok(())
    }
}

pub fn thaw_display_rotation(display_id: i32) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return Ok(());
        }
        let svc = match rsbinder::hub::check_service(WINDOW_SERVICE) {
            Some(s) => s,
            None => return Ok(()),
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => return Ok(()),
        };

        let mut data = proxy.prepare_transact(true)?;
        data.write(&display_id)?;
        data.write(&"id.xms.xtrakernelmanager".to_string())?;
        let _ = proxy.submit_transact(TRANSACTION_THAW_DISPLAY_ROTATION, &data, 0);
        Ok(())
    }

    #[cfg(not(target_os = "android"))]
    {
        let _ = display_id;
        Ok(())
    }
}

pub fn is_display_rotation_frozen(display_id: i32) -> bool {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return false;
        }
        let svc = match rsbinder::hub::check_service(WINDOW_SERVICE) {
            Some(s) => s,
            None => return false,
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => return false,
        };
        let Ok(mut data) = proxy.prepare_transact(true) else {
            return false;
        };
        if data.write(&display_id).is_err() {
            return false;
        }
        let Ok(Some(mut reply)) = proxy.submit_transact(TRANSACTION_IS_DISPLAY_ROTATION_FROZEN, &data, 0) else {
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
        let _ = display_id;
        false
    }
}
