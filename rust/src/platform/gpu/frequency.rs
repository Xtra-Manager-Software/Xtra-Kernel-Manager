use std::fs;
use std::path::Path;

use super::detect::{get_gpu_info, GpuVendor};
use crate::platform::common;

#[inline]
fn normalize_freq_mhz(raw_hz: i64) -> i32 {
    match raw_hz {
        v if v > 1_000_000 => (v / 1_000_000) as i32,
        v if v > 1_000 => (v / 1_000) as i32,
        v => v as i32,
    }
}

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
                        return normalize_freq_mhz(val);
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
                        return normalize_freq_mhz(val);
                    }
                }
            }
        }
        GpuVendor::Unknown => {}
    }

    // Generic devfreq search
    let generic_paths = [
        "/sys/class/devfreq/1c00000.qcom,kgsl-3d0/cur_freq",
        "/sys/class/devfreq/gpufreq/cur_freq",
    ];
    for path in generic_paths {
        if let Some(val) = common::read_sysfs_i64(path) {
            if val > 0 {
                return normalize_freq_mhz(val);
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
                    .map(normalize_freq_mhz)
                    .collect();
                if !freqs.is_empty() {
                    return freqs;
                }
            }
        }
    }
    Vec::new()
}
