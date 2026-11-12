use anyhow::{Context, Result};
use std::fs;
use std::path::Path;

const SCONFIG_PATH: &str = "/sys/class/thermal/thermal_message/sconfig";

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum XiaomiThermalPreset {
    Class0,    // 11 or 0 - Default stock
    Extreme,   // 2 - Maximum gaming performance
    Dynamic,   // 10 - Balanced / adaptive
    Incalls,   // 8 - Cool calls
    Thermal20, // 20 - Relaxed throttling
}

impl XiaomiThermalPreset {
    pub fn index(self) -> i32 {
        match self {
            Self::Class0 => 11,
            Self::Extreme => 2,
            Self::Dynamic => 10,
            Self::Incalls => 8,
            Self::Thermal20 => 20,
        }
    }
}

pub fn is_sconfig_supported() -> bool {
    Path::new(SCONFIG_PATH).exists()
}

pub fn read_sconfig_index() -> Option<i32> {
    if !is_sconfig_supported() {
        return None;
    }
    fs::read_to_string(SCONFIG_PATH)
        .ok()
        .and_then(|s| s.trim().parse::<i32>().ok())
}

pub fn set_sconfig_index(index: i32) -> Result<()> {
    if !is_sconfig_supported() {
        anyhow::bail!("Xiaomi sconfig is not supported on this device");
    }
    fs::write(SCONFIG_PATH, index.to_string())
        .with_context(|| format!("Failed to write index {} to {}", index, SCONFIG_PATH))
}

pub fn set_thermal_preset(preset: XiaomiThermalPreset) -> Result<()> {
    set_sconfig_index(preset.index())
}
