use anyhow::Result;

pub const VIBRATOR_SERVICE: &str = "android.hardware.vibrator.IVibrator/default";

pub fn get_vibrator_capabilities() -> Result<u32> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            anyhow::bail!("Binder ProcessState not initialized");
        }
        let svc = match rsbinder::hub::check_service(VIBRATOR_SERVICE) {
            Some(s) => s,
            None => anyhow::bail!("Vibrator HAL not found"),
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => anyhow::bail!("Vibrator HAL is not a proxy"),
        };
        let data = proxy.prepare_transact(true)?;
        let reply_opt = proxy.submit_transact(1, &data, 0)?;
        if let Some(mut rep) = reply_opt {
            let ex = rep.read_i32()?;
            if ex != 0 {
                anyhow::bail!("getCapabilities exception: {}", ex);
            }
            let caps = rep.read_i32()?;
            return Ok(caps as u32);
        }
        anyhow::bail!("No reply received from Vibrator HAL");
    }

    #[cfg(not(target_os = "android"))]
    {
        Ok(0x27)
    }
}

pub fn silence_vibrator() -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            return Ok(());
        }
        let svc = match rsbinder::hub::check_service(VIBRATOR_SERVICE) {
            Some(s) => s,
            None => return Ok(()),
        };
        let proxy = match svc.as_proxy() {
            Some(p) => p,
            None => return Ok(()),
        };
        // TRANSACTION_off = 2
        let data = proxy.prepare_transact(true)?;
        let _ = proxy.submit_transact(2, &data, 0);
    }
    Ok(())
}
