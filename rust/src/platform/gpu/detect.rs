use std::path::Path;
use std::sync::OnceLock;

use crate::platform::common;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum GpuVendor {
    Qualcomm,
    Mali,
    Unknown,
}

impl std::fmt::Display for GpuVendor {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            GpuVendor::Qualcomm => write!(f, "Qualcomm"),
            GpuVendor::Mali => write!(f, "ARM Mali"),
            GpuVendor::Unknown => write!(f, "Unknown"),
        }
    }
}

static GPU_INFO: OnceLock<(GpuVendor, String)> = OnceLock::new();

pub fn get_gpu_info() -> &'static (GpuVendor, String) {
    GPU_INFO.get_or_init(|| {
        if Path::new("/sys/class/kgsl/kgsl-3d0").exists() {
            let model = detect_adreno_model().unwrap_or_else(|| "Adreno".to_string());
            return (GpuVendor::Qualcomm, model);
        }

        let mali_paths = [
            "/sys/class/misc/mali0/device/gpuinfo",
            "/sys/devices/platform/mali/gpuinfo",
            "/dev/mali0",
        ];
        for path in mali_paths {
            if Path::new(path).exists() {
                let model = detect_mali_model().unwrap_or_else(|| "Mali".to_string());
                return (GpuVendor::Mali, model);
            }
        }

        if let Some(hardware) = common::get_system_property("ro.hardware") {
            let hw = hardware.to_lowercase();
            match () {
                _ if hw.contains("qcom") || hw.contains("snapdragon") => {
                    return (GpuVendor::Qualcomm, "Adreno".to_string());
                }
                _ if hw.contains("mt") || hw.contains("mediatek") => {
                    return (GpuVendor::Mali, "Mali".to_string());
                }
                _ => {}
            }
        }

        (GpuVendor::Unknown, "Generic GPU".to_string())
    })
}

fn detect_adreno_model() -> Option<String> {
    let paths = [
        "/sys/class/kgsl/kgsl-3d0/gpu_model",
        "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_model",
    ];
    for path in paths {
        if let Some(m) = common::read_sysfs_trimmed(path) {
            let trimmed = m.trim();
            if !trimmed.is_empty() {
                return Some(trimmed.to_string());
            }
        }
    }
    None
}

fn detect_mali_model() -> Option<String> {
    let paths = [
        "/sys/class/misc/mali0/device/gpuinfo",
        "/sys/devices/platform/mali/gpuinfo",
    ];
    for path in paths {
        if let Some(m) = common::read_sysfs_trimmed(path) {
            return Some(m);
        }
    }
    None
}
