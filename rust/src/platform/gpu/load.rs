use parking_lot::Mutex;
use std::fs;
use std::sync::OnceLock;

use super::detect::{get_gpu_info, GpuVendor};
use crate::platform::common;

#[derive(Default, Clone, Copy)]
struct BusyState {
    busy: i64,
    total: i64,
}

static LAST_BUSY_STATE: OnceLock<Mutex<Option<BusyState>>> = OnceLock::new();

pub fn read_gpu_busy_pct() -> i32 {
    let (vendor, _) = get_gpu_info();

    match vendor {
        GpuVendor::Qualcomm => {
            // Check direct percentage node
            let direct_pct_nodes = [
                "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
                "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_load",
            ];
            for path in direct_pct_nodes {
                if let Some(val) = common::read_sysfs_i64(path) {
                    if val >= 0 {
                        return val.clamp(0, 100) as i32;
                    }
                }
            }

            // Fallback to gpubusy delta (busy total)
            if let Some(s) = common::read_sysfs_trimmed("/sys/class/kgsl/kgsl-3d0/gpubusy") {
                let parts: Vec<i64> = s
                    .split_whitespace()
                    .filter_map(|p| p.parse::<i64>().ok())
                    .collect();

                if parts.len() >= 2 {
                    let busy = parts[0];
                    let total = parts[1];

                    let cache = LAST_BUSY_STATE.get_or_init(|| Mutex::new(None));
                    let mut lock = cache.lock();

                    if let Some(prev) = *lock {
                        let d_busy = busy.saturating_sub(prev.busy);
                        let d_total = total.saturating_sub(prev.total);
                        *lock = Some(BusyState { busy, total });

                        if d_total > 0 {
                            return ((d_busy as f64 / d_total as f64) * 100.0).clamp(0.0, 100.0) as i32;
                        }
                    } else {
                        *lock = Some(BusyState { busy, total });
                    }
                }
            }
        }
        GpuVendor::Mali => {
            let mali_paths = [
                "/sys/class/misc/mali0/device/utilization",
                "/sys/devices/platform/mali/utilization",
                "/sys/kernel/gpu/gpu_busy",
                "/sys/class/devfreq/gpufreq/load",
            ];
            for path in mali_paths {
                if let Some(val) = common::read_sysfs_i64(path) {
                    if val >= 0 {
                        return val.clamp(0, 100) as i32;
                    }
                }
            }
        }
        GpuVendor::Unknown => {}
    }

    // Generic devfreq load
    if let Ok(entries) = fs::read_dir("/sys/class/devfreq") {
        for entry in entries.flatten() {
            let name = entry.file_name().to_string_lossy().to_string();
            if name.contains("gpu") || name.contains("kgsl") || name.contains("mali") {
                let load_file = format!("{}/load", entry.path().display());
                if let Some(val) = common::read_sysfs_i64(&load_file) {
                    return val.clamp(0, 100) as i32;
                }
            }
        }
    }

    0
}

pub fn reset_gpu_busy_stats() {
    let cache = LAST_BUSY_STATE.get_or_init(|| Mutex::new(None));
    *cache.lock() = None;
}
