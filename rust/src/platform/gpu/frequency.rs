use std::fs;
use std::path::Path;

use super::detect::{get_gpu_info, GpuVendor};
use crate::platform::common;

pub fn read_gpu_freq_mhz() -> i32 {
    let (vendor, _) = get_gpu_info();

    match vendor {
        GpuVendor::Qualcomm => {
            let paths = [
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
            ];
            for path in paths {
                if let Some(val) = common::read_sysfs_i64(path) {
                    if val > 0 {
                        return if val > 1_000_000 {
                            (val / 1_000_000) as i32
                        } else if val > 1_000 {
                            (val / 1_000) as i32
                        } else {
                            val as i32
                        };
                    }
                }
            }
        }
        GpuVendor::Mali => {
            let paths = [
                "/sys/class/misc/mali0/device/cur_freq",
                "/sys/devices/platform/mali/cur_freq",
                "/sys/kernel/gpu/gpu_clock",
                "/sys/class/devfreq/gpufreq/cur_freq",
                "/proc/gpufreq/gpufreq_var_dump",
            ];
            for path in paths {
                if let Some(val) = common::read_sysfs_i64(path) {
                    if val > 0 {
                        return if val > 1_000_000 {
                            (val / 1_000_000) as i32
                        } else if val > 1_000 {
                            (val / 1_000) as i32
                        } else {
                            val as i32
                        };
                    }
                }
            }
        }
        GpuVendor::Unknown => {}
    }

    // Generic devfreq search
    if let Ok(entries) = fs::read_dir("/sys/class/devfreq") {
        for entry in entries.flatten() {
            let name = entry.file_name().to_string_lossy().to_string();
            if name.contains("gpu") || name.contains("kgsl") || name.contains("mali") {
                let cur = format!("{}/cur_freq", entry.path().display());
                if let Some(val) = common::read_sysfs_i64(&cur) {
                    if val > 0 {
                        return if val > 1_000_000 {
                            (val / 1_000_000) as i32
                        } else {
                            (val / 1_000) as i32
                        };
                    }
                }
            }
        }
    }

    0
}

pub fn get_available_frequencies() -> Vec<i32> {
    let candidates = [
        "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies",
        "/sys/class/kgsl/kgsl-3d0/devfreq/available_frequencies",
        "/sys/class/misc/mali0/device/available_frequencies",
    ];

    for path in candidates {
        if Path::new(path).exists() {
            if let Ok(content) = fs::read_to_string(path) {
                let freqs: Vec<i32> = content
                    .split_whitespace()
                    .filter_map(|s| s.parse::<i64>().ok())
                    .map(|v| {
                        if v > 1_000_000 {
                            (v / 1_000_000) as i32
                        } else if v > 1_000 {
                            (v / 1_000) as i32
                        } else {
                            v as i32
                        }
                    })
                    .collect();
                if !freqs.is_empty() {
                    return freqs;
                }
            }
        }
    }
    Vec::new()
}
