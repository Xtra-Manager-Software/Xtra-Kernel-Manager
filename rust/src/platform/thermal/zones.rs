use serde::{Deserialize, Serialize};
use std::path::Path;

use crate::platform::common;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ThermalZoneInfo {
    pub id: i32,
    pub name: String,
    pub temp: f32,
    pub category: String,
}

#[derive(Debug, Clone, Default)]
pub struct ThermalSummary {
    pub cpu_temp_c: Option<f32>,
    pub gpu_temp_c: Option<f32>,
    pub battery_temp_c: Option<f32>,
    pub zones: Vec<ThermalZoneInfo>,
}

fn categorize_zone(name: &str) -> &'static str {
    let lower = name.to_lowercase();
    if lower.contains("cpu") || lower.contains("tsens_tz_sensor") || lower.contains("mtkts_cpu") {
        "cpu"
    } else if lower.contains("gpu") || lower.contains("kgsl") || lower.contains("mali") || lower.contains("g3d") {
        "gpu"
    } else if lower.contains("batt") || lower.contains("bms") {
        "battery"
    } else if lower.contains("soc") || lower.contains("skin") || lower.contains("quiet") || lower.contains("chg") {
        "soc"
    } else if lower.contains("pmic") || lower.contains("pa") || lower.contains("modem") {
        "pmic"
    } else {
        "other"
    }
}

pub fn scan_thermal_zones() -> ThermalSummary {
    let mut zones = Vec::new();
    let mut cpu_temps = Vec::new();
    let mut gpu_temps = Vec::new();
    let mut battery_temp = None;

    for i in 0..64 {
        let base = format!("/sys/class/thermal/thermal_zone{}", i);
        if !Path::new(&base).exists() {
            break;
        }

        let type_path = format!("{}/type", base);
        let temp_path = format!("{}/temp", base);

        let name = match common::read_sysfs_trimmed(&type_path) {
            Some(n) => n,
            None => continue,
        };

        let raw_temp = match common::read_sysfs_i64(&temp_path) {
            Some(t) => t,
            None => continue,
        };

        // Standard sysfs temp is milli-Celsius (e.g. 42000 = 42.0°C)
        let temp_c = if raw_temp > 1000 {
            raw_temp as f32 / 1000.0
        } else {
            raw_temp as f32
        };

        let category = categorize_zone(&name);
        match category {
            "cpu" if (0.0..120.0).contains(&temp_c) => cpu_temps.push(temp_c),
            "gpu" if (0.0..120.0).contains(&temp_c) => gpu_temps.push(temp_c),
            "battery" if (-10.0..80.0).contains(&temp_c) => battery_temp = Some(temp_c),
            _ => {}
        }

        zones.push(ThermalZoneInfo {
            id: i,
            name,
            temp: temp_c,
            category: category.to_string(),
        });
    }

    let cpu_max = cpu_temps.iter().cloned().fold(None, |acc, val| {
        Some(acc.map_or(val, |a: f32| a.max(val)))
    });

    let gpu_max = gpu_temps.iter().cloned().fold(None, |acc, val| {
        Some(acc.map_or(val, |a: f32| a.max(val)))
    });

    ThermalSummary {
        cpu_temp_c: cpu_max,
        gpu_temp_c: gpu_max,
        battery_temp_c: battery_temp,
        zones,
    }
}

pub fn read_cpu_temperature() -> f32 {
    scan_thermal_zones().cpu_temp_c.unwrap_or(0.0)
}
