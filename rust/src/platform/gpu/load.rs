use std::fs;

use super::detect::{get_gpu_info, GpuVendor};
use crate::platform::common;

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

            // Fallback to gpubusy (busy total)
            if let Some(s) = common::read_sysfs_trimmed("/sys/class/kgsl/kgsl-3d0/gpubusy") {
                let parts: Vec<i64> = s
                    .split_whitespace()
                    .filter_map(|p| p.parse::<i64>().ok())
                    .collect();

                if parts.len() >= 2 {
                    let busy = parts[0];
                    let total = parts[1];

                    if total > 0 && busy >= 0 {
                        let pct = ((busy as f64 / total as f64) * 100.0).round().clamp(0.0, 100.0) as i32;
                        return pct;
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
    // No-op: GPU busy is calculated directly from instantaneous governor statistics
}
