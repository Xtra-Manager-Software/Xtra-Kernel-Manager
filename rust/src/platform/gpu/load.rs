use std::fs;

use super::detect::{GpuVendor, get_gpu_info};
use crate::platform::common;

fn find_first_valid_load(paths: &[&str]) -> Option<i32> {
    for &path in paths {
        match common::read_sysfs_i64(path) {
            Some(val) if val >= 0 => return Some(val.clamp(0, 100) as i32),
            _ => continue,
        }
    }
    None
}

pub fn read_gpu_busy_pct() -> i32 {
    let (vendor, _) = get_gpu_info();

    match vendor {
        GpuVendor::Qualcomm => {
            // Check direct percentage node
            let direct_pct_nodes = [
                "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
                "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_load",
            ];
            if let Some(pct) = find_first_valid_load(&direct_pct_nodes) {
                return pct;
            }

            // Fallback to gpubusy (busy total)
            if let Some(s) = common::read_sysfs_trimmed("/sys/class/kgsl/kgsl-3d0/gpubusy") {
                let mut parts = s.split_whitespace().filter_map(|p| p.parse::<i64>().ok());
                if let (Some(busy), Some(total)) = (parts.next(), parts.next())
                    && total > 0
                    && busy >= 0
                {
                    return ((busy as f64 / total as f64) * 100.0)
                        .round()
                        .clamp(0.0, 100.0) as i32;
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
            if let Some(pct) = find_first_valid_load(&mali_paths) {
                return pct;
            }
        }
        GpuVendor::Unknown => {}
    }

    // Generic devfreq load
    if let Ok(entries) = fs::read_dir("/sys/class/devfreq") {
        for entry in entries.flatten() {
            let file_name = entry.file_name();
            let name = file_name.to_string_lossy();
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
