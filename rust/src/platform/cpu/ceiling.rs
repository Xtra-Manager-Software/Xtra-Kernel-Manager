use anyhow::{Context, Result};
#[cfg(unix)]
use libc::{MNT_DETACH, MS_BIND, MS_REC, mount, umount2};
use parking_lot::Mutex;
#[cfg(unix)]
use std::ffi::CString;
use std::fs;
use std::path::Path;
#[cfg(unix)]
use std::ptr;
use std::sync::OnceLock;
use tracing::{debug, info, warn};

#[derive(Debug, Clone)]
struct ActiveMount {
    target_path: String,
    mount_source: String,
}

static ACTIVE_MOUNTS: OnceLock<Mutex<Vec<ActiveMount>>> = OnceLock::new();

fn mounts_lock() -> &'static Mutex<Vec<ActiveMount>> {
    ACTIVE_MOUNTS.get_or_init(|| Mutex::new(Vec::new()))
}

#[cfg(unix)]
fn do_mount_bind(src: &str, dest: &str) -> bool {
    let (src_c, dest_c) = match (CString::new(src), CString::new(dest)) {
        (Ok(s), Ok(d)) => (s, d),
        _ => return false,
    };

    // SAFETY:
    // 1. `src_c` and `dest_c` are valid null-terminated CStrings verified by CString::new without interior NULs.
    // 2. Both allocations live across the entire unsafe block and their pointers are valid for read.
    // 3. `umount2(..., MNT_DETACH)` performs a lazy detach so any existing bind on `dest` is cleaned up without blocking.
    // 4. `mount(..., MS_BIND | MS_REC, ...)` creates a private bind mount; target and data pointers are null as required.
    unsafe {
        umount2(dest_c.as_ptr(), MNT_DETACH);
        mount(
            src_c.as_ptr(),
            dest_c.as_ptr(),
            ptr::null(),
            MS_BIND | MS_REC,
            ptr::null(),
        ) == 0
    }
}

#[cfg(unix)]
fn do_unmount(path: &str) -> bool {
    let p = match CString::new(path) {
        Ok(s) => s,
        Err(_) => return false,
    };
    // SAFETY:
    // 1. `p` is a valid null-terminated CString with no interior NUL bytes.
    // 2. `MNT_DETACH` performs a lazy unmount, safely detaching the mount point immediately
    //    and unmounting when it is no longer busy, avoiding kernel deadlock.
    unsafe { umount2(p.as_ptr(), MNT_DETACH) == 0 }
}

#[cfg(not(unix))]
fn do_mount_bind(_src: &str, _dest: &str) -> bool {
    false
}

#[cfg(not(unix))]
fn do_unmount(_path: &str) -> bool {
    false
}

/// Cleanup any leftover binds from previous session
pub fn cleanup_stale_mounts() {
    if let Ok(mounts) = fs::read_to_string("/proc/mounts") {
        for line in mounts.lines() {
            let mut parts = line.split_whitespace();
            let src = parts.next().unwrap_or("");
            let dest = parts.next().unwrap_or("");

            if src.contains(".xkm_ceiling")
                || (dest.contains("cpufreq")
                    && dest.contains("scaling_max_freq")
                    && src.starts_with("/data/local/tmp"))
            {
                do_unmount(dest);
                let _ = fs::remove_file(src);
                info!("Cleaned stale XKM ceiling mount: {} -> {}", src, dest);
            }
        }
    }
}

/// Lock a specific sysfs path (e.g. scaling_max_freq) to a target frequency value using bind mount
pub fn lock_path_frequency(path: &str, freq_khz: u64) -> Result<()> {
    if !Path::new(path).exists() {
        anyhow::bail!("Target path {} does not exist", path);
    }

    let sanitized = path.replace('/', "_");
    let mount_source = format!("/data/local/tmp/.xkm_ceiling{}", sanitized);
    let val_str = format!("{}\n", freq_khz);

    fs::write(&mount_source, &val_str)
        .with_context(|| format!("Failed to write ceiling value to {}", mount_source))?;

    // First try normal sysfs write as baseline
    let _ = fs::write(path, &val_str);

    // Apply bind-mount over the node to lock out vendor thermal daemons
    match do_mount_bind(&mount_source, path) {
        true => {
            info!("Hard-locked {} to {} kHz via bind-mount", path, freq_khz);
            let mut lock = mounts_lock().lock();
            lock.retain(|m| m.target_path != path);
            lock.push(ActiveMount {
                target_path: path.to_string(),
                mount_source,
            });
            Ok(())
        }
        false => {
            let _ = fs::remove_file(&mount_source);
            warn!(
                "Bind mount failed for {} (possibly lacking CAP_SYS_ADMIN), fell back to sysfs write",
                path
            );
            Ok(())
        }
    }
}

/// Unlock a specific sysfs path by unmounting and restoring normal sysfs node
pub fn unlock_path_frequency(path: &str) -> Result<()> {
    do_unmount(path);

    let mut lock = mounts_lock().lock();
    if let Some(pos) = lock.iter().position(|m| m.target_path == path) {
        let entry = lock.remove(pos);
        let _ = fs::remove_file(&entry.mount_source);
    }
    debug!("Unlocked {}", path);
    Ok(())
}

/// Lock a CPU cluster (or policy) scaling_max_freq using bind mount
pub fn lock_cluster_frequency(cluster: i32, max_freq_khz: u64) -> Result<()> {
    let policy_path = format!(
        "/sys/devices/system/cpu/cpufreq/policy{}/scaling_max_freq",
        cluster
    );
    let target = match Path::new(&policy_path).exists() {
        true => policy_path,
        false => format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_max_freq",
            cluster
        ),
    };
    lock_path_frequency(&target, max_freq_khz)
}

/// Unlock a CPU cluster frequency bind mount
pub fn unlock_cluster_frequency(cluster: i32) -> Result<()> {
    let policy_path = format!(
        "/sys/devices/system/cpu/cpufreq/policy{}/scaling_max_freq",
        cluster
    );
    let target = match Path::new(&policy_path).exists() {
        true => policy_path,
        false => format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_max_freq",
            cluster
        ),
    };
    unlock_path_frequency(&target)
}

/// Teardown all active bind mounts
pub fn unlock_all() -> Result<()> {
    let mut lock = mounts_lock().lock();
    for entry in lock.drain(..) {
        do_unmount(&entry.target_path);
        let _ = fs::remove_file(&entry.mount_source);
    }
    Ok(())
}
