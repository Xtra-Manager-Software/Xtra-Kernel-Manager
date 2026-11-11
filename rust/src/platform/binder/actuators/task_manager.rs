#[allow(unused_imports)]
use anyhow::{Context, Result, bail};
use serde::{Deserialize, Serialize};
use std::fs;

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

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ProcessItem {
    pub pid: i32,
    pub uid: u32,
    pub process_name: String,
    pub package_name: String,
    pub rss_kb: u64,
    pub state: char,
    pub cgroup: String,
}

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
        if let Some(mut r) = reply {
            if let Ok(0) = r.read_i32() {
                return Ok(r.read_bool().unwrap_or(true));
            }
        }
        Ok(true)
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
        if let Some(mut r) = reply {
            if let Ok(0) = r.read_i32() {
                let bucket_val = r.read_i32().unwrap_or(10);
                return Ok(StandbyBucket::from_i32(bucket_val));
            }
        }
        Ok(StandbyBucket::Active)
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

/// Scan running processes natively without spawning subshells or companion APKs.
pub fn get_running_process_list() -> Result<Vec<ProcessItem>> {
    let mut list = Vec::with_capacity(128);
    let proc_dir = fs::read_dir("/proc").context("Cannot read /proc")?;

    for entry in proc_dir.flatten() {
        let file_name = entry.file_name();
        let name_str = match file_name.to_str() {
            Some(s) => s,
            None => continue,
        };

        let pid: i32 = match name_str.parse() {
            Ok(p) => p,
            Err(_) => continue,
        };

        if pid <= 100 {
            continue; // Skip kernel threads
        }

        let pid_path = format!("/proc/{}", pid);
        let cmdline_path = format!("{}/cmdline", pid_path);
        let status_path = format!("{}/status", pid_path);
        let cgroup_path = format!("{}/cgroup", pid_path);

        // 1. Process / Package Name from cmdline
        let cmdline_bytes = fs::read(&cmdline_path).unwrap_or_default();
        let first_null = cmdline_bytes
            .iter()
            .position(|&b| b == 0)
            .unwrap_or(cmdline_bytes.len());
        let raw_name = std::str::from_utf8(&cmdline_bytes[..first_null]).unwrap_or("");
        if raw_name.is_empty() {
            continue;
        }

        let process_name = raw_name.to_string();
        let package_name = raw_name.split(':').next().unwrap_or(raw_name).to_string();

        // 2. UID, State, RSS from status
        let mut uid = 0u32;
        let mut rss_kb = 0u64;
        let mut state = 'S';

        if let Ok(status_str) = fs::read_to_string(&status_path) {
            for line in status_str.lines() {
                if let Some(rest) = line.strip_prefix("Uid:") {
                    if let Some(first_uid) = rest.split_whitespace().next() {
                        uid = first_uid.parse().unwrap_or(0);
                    }
                } else if let Some(rest) = line.strip_prefix("State:") {
                    if let Some(s) = rest.trim().chars().next() {
                        state = s;
                    }
                } else if let Some(rest) = line.strip_prefix("VmRSS:") {
                    if let Some(kb) = rest.split_whitespace().next() {
                        rss_kb = kb.parse().unwrap_or(0);
                    }
                }
            }
        }

        // 3. Cgroup (top-app, foreground, background)
        let mut cgroup = "default".to_string();
        if let Ok(cg_str) = fs::read_to_string(&cgroup_path) {
            for line in cg_str.lines() {
                if line.contains("cpuset") {
                    if let Some(grp) = line.split(':').nth(2) {
                        let trimmed = grp.trim_start_matches('/');
                        if !trimmed.is_empty() {
                            cgroup = trimmed.to_string();
                            break;
                        }
                    }
                }
            }
        }

        list.push(ProcessItem {
            pid,
            uid,
            process_name,
            package_name,
            rss_kb,
            state,
            cgroup,
        });
    }

    // Sort by RSS memory descending
    list.sort_unstable_by(|a, b| b.rss_kb.cmp(&a.rss_kb));
    Ok(list)
}
