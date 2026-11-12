#[allow(unused_imports)]
use anyhow::{Context, Result, bail};
use serde::{Deserialize, Serialize};

pub const ACTIVITY_SERVICE: &str = "activity";
pub const ACTIVITY_DESCRIPTOR: &str = "android.app.IActivityManager";

pub const USAGE_STATS_SERVICE: &str = "usagestats";
pub const USAGE_STATS_DESCRIPTOR: &str = "android.app.usage.IUsageStatsManager";

// Transaction codes for android.app.IActivityManager
pub const TRANSACTION_FORCE_STOP_PACKAGE: u32 = 91;
pub const TRANSACTION_KILL_PIDS: u32 = 93;
pub const TRANSACTION_KILL_BACKGROUND_PROCESSES: u32 = 110;
pub const TRANSACTION_KILL_ALL_BACKGROUND_PROCESSES: u32 = 135;
pub const TRANSACTION_SET_PROCESS_MEMORY_TRIM_LEVEL: u32 = 180;
pub const TRANSACTION_IS_PROCESS_FROZEN: u32 = 253;

// Transaction codes for android.app.usage.IUsageStatsManager
pub const TRANSACTION_GET_APP_STANDBY_BUCKET: u32 = 14;
pub const TRANSACTION_SET_APP_STANDBY_BUCKET: u32 = 15;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[repr(i32)]
pub enum StandbyBucket {
    Active = 10,
    WorkingSet = 20,
    Frequent = 30,
    Rare = 40,
    Restricted = 45,
    Never = 50,
}

impl StandbyBucket {
    pub fn from_i32(val: i32) -> Self {
        match val {
            10 => Self::Active,
            20 => Self::WorkingSet,
            30 => Self::Frequent,
            40 => Self::Rare,
            45 => Self::Restricted,
            50 => Self::Never,
            _ => Self::Active,
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[repr(i32)]
pub enum MemoryTrimLevel {
    RunningModerate = 5,
    RunningLow = 10,
    RunningCritical = 15,
    Background = 40,
    Moderate = 60,
    Complete = 80,
}

pub use crate::platform::process::{ProcessItem, get_running_process_list};

/// Kill all background processes belonging to the specified package.
pub fn kill_background_processes(package_name: &str, user_id: Option<i32>) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(ACTIVITY_SERVICE)
            .context("ActivityManager service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&package_name.to_string())?;
        data.write(&(user_id.unwrap_or(0)))?;

        let _ = proxy.submit_transact(TRANSACTION_KILL_BACKGROUND_PROCESSES, &data, 0)?;
        Ok(())
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = (package_name, user_id);
        Ok(())
    }
}

/// Kill all background processes in the system that are safe to terminate (cached apps).
pub fn kill_all_background_processes() -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(ACTIVITY_SERVICE)
            .context("ActivityManager service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let data = proxy.prepare_transact(true)?;

        let _ = proxy.submit_transact(TRANSACTION_KILL_ALL_BACKGROUND_PROCESSES, &data, 0)?;
        Ok(())
    }
    #[cfg(not(target_os = "android"))]
    {
        Ok(())
    }
}

/// Completely force stop a package across its activities, services, and alarms.
pub fn force_stop_package(package_name: &str, user_id: Option<i32>) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(ACTIVITY_SERVICE)
            .context("ActivityManager service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&package_name.to_string())?;
        data.write(&(user_id.unwrap_or(0)))?;

        let _ = proxy.submit_transact(TRANSACTION_FORCE_STOP_PACKAGE, &data, 0)?;
        Ok(())
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = (package_name, user_id);
        Ok(())
    }
}

/// Terminate multiple PIDs directly via ActivityManager.
pub fn kill_pids(pids: &[i32], reason: &str) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(ACTIVITY_SERVICE)
            .context("ActivityManager service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        let pids_vec: Vec<i32> = pids.to_vec();
        data.write(&pids_vec)?;
        data.write(&reason.to_string())?;
        data.write(&false)?; // secure flag

        let _ = proxy.submit_transact(TRANSACTION_KILL_PIDS, &data, 0)?;
        Ok(())
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = (pids, reason);
        Ok(())
    }
}

/// Check if a process is currently frozen by the Android cgroup freezer / v2 freezer.
pub fn is_process_frozen(pid: i32) -> Result<bool> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(ACTIVITY_SERVICE)
            .context("ActivityManager service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&pid)?;

        let reply = proxy.submit_transact(TRANSACTION_IS_PROCESS_FROZEN, &data, 0)?;
        if let Some(mut r) = reply {
            if let Ok(0) = r.read_i32() {
                return Ok(r.read_bool().unwrap_or(false));
            }
        }
        Ok(false)
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = pid;
        Ok(false)
    }
}

/// Request an application to immediately trim memory (caches, bitmaps, unused resources).
pub fn set_process_memory_trim_level(
    process: &str,
    uid: i32,
    level: MemoryTrimLevel,
) -> Result<bool> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(ACTIVITY_SERVICE)
            .context("ActivityManager service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&process.to_string())?;
        data.write(&uid)?;
        data.write(&(level as i32))?;

        let reply = proxy.submit_transact(TRANSACTION_SET_PROCESS_MEMORY_TRIM_LEVEL, &data, 0)?;
        match reply {
            Some(mut r) => match r.read_i32() {
                Ok(0) => Ok(r.read_bool().unwrap_or(true)),
                _ => Ok(true),
            },
            None => Ok(true),
        }
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = (process, uid, level);
        Ok(true)
    }
}

/// Get the App Standby Bucket for a specific package.
pub fn get_app_standby_bucket(package_name: &str, user_id: Option<i32>) -> Result<StandbyBucket> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(USAGE_STATS_SERVICE)
            .context("UsageStats service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&package_name.to_string())?;
        data.write(&"android".to_string())?;
        data.write(&(user_id.unwrap_or(0)))?;

        let reply = proxy.submit_transact(TRANSACTION_GET_APP_STANDBY_BUCKET, &data, 0)?;
        match reply {
            Some(mut r) => match r.read_i32() {
                Ok(0) => {
                    let bucket_val = r.read_i32().unwrap_or(10);
                    Ok(StandbyBucket::from_i32(bucket_val))
                }
                _ => Ok(StandbyBucket::Active),
            },
            None => Ok(StandbyBucket::Active),
        }
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = (package_name, user_id);
        Ok(StandbyBucket::Active)
    }
}

/// Set the App Standby Bucket for a package (e.g. Active for games, Restricted for heavy background apps).
pub fn set_app_standby_bucket(
    package_name: &str,
    bucket: StandbyBucket,
    user_id: Option<i32>,
) -> Result<()> {
    #[cfg(target_os = "android")]
    {
        if !crate::platform::binder::ensure_initialized() {
            bail!("Binder ProcessState not initialized");
        }
        let svc = rsbinder::hub::check_service(USAGE_STATS_SERVICE)
            .context("UsageStats service not found")?;
        let proxy = svc.as_proxy().context("Failed to get proxy")?;
        let mut data = proxy.prepare_transact(true)?;
        data.write(&package_name.to_string())?;
        data.write(&(bucket as i32))?;
        data.write(&(user_id.unwrap_or(0)))?;

        let _ = proxy.submit_transact(TRANSACTION_SET_APP_STANDBY_BUCKET, &data, 0)?;
        Ok(())
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = (package_name, bucket, user_id);
        Ok(())
    }
}
