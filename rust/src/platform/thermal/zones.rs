use parking_lot::RwLock;
use serde::{Deserialize, Serialize};
use std::path::Path;
use std::sync::OnceLock;

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

#[derive(Clone, Default)]
struct CachedSensor {
    id: i32,
    name: String,
    category: &'static str,
    temp_path: String,
}

static ACTIVE_SENSORS_CACHE: OnceLock<RwLock<Vec<CachedSensor>>> = OnceLock::new();

fn categorize_zone(name: &str) -> &'static str {
    let lower = name.to_lowercase();
    if lower.contains("cpuss") || lower.contains("cpu") || lower.contains("tsens_tz_sensor") || lower.contains("mtkts_cpu") {
        "cpu"
    } else if lower.contains("gpuss") || lower.contains("gpu") || lower.contains("kgsl") || lower.contains("mali") || lower.contains("adreno") || lower.contains("g3d") {
        "gpu"
    } else if lower.contains("batt") || lower.contains("bms") {
        "battery"
    } else if lower.contains("soc") || lower.contains("skin") || lower.contains("quiet") || lower.contains("chg") {
        "soc"
    } else if lower.contains("pmic") || lower.contains("pa") || lower.contains("pm8") || lower.contains("pmr") || lower.contains("modem") {
        "pmic"
    } else {
        "other"
    }
}

fn parse_raw_temp(raw_temp: i64) -> Option<f32> {
    // Filter out sensor offline, uncalibrated, or error values (-273000, -40000, 0, etc.)
    if raw_temp <= -40_000 || raw_temp >= 150_000 || raw_temp == 0 {
        return None;
    }

    let temp_c = if raw_temp.abs() >= 1000 {
        raw_temp as f32 / 1000.0
    } else if raw_temp.abs() >= 100 {
        raw_temp as f32 / 10.0
    } else {
        raw_temp as f32
    };

    if (0.0..=125.0).contains(&temp_c) {
        Some(temp_c)
    } else {
        None
    }
}

pub fn scan_thermal_zones() -> ThermalSummary {
    let cache_lock = ACTIVE_SENSORS_CACHE.get_or_init(|| {
        let mut discovered = Vec::new();
        for i in 0..96 {
            let base = format!("/sys/class/thermal/thermal_zone{}", i);
            let type_path = format!("{}/type", base);
            let temp_path = format!("{}/temp", base);

            if !Path::new(&temp_path).exists() {
                continue;
            }

            let name = match common::read_sysfs_trimmed(&type_path) {
                Some(n) => n,
                None => continue,
            };

            // Test if sensor responds with valid temperature
            if let Some(raw) = common::read_sysfs_i64(&temp_path) {
                if parse_raw_temp(raw).is_some() {
                    let category = categorize_zone(&name);
                    discovered.push(CachedSensor {
                        id: i,
                        name,
                        category,
                        temp_path,
                    });
                }
            }
        }
        RwLock::new(discovered)
    });

    let sensors = cache_lock.read();
    let mut zones = Vec::with_capacity(sensors.len());
    let mut cpu_temps = Vec::new();
    let mut gpu_temps = Vec::new();
    let mut battery_temp = None;

    for s in sensors.iter() {
        if let Some(raw) = common::read_sysfs_i64(&s.temp_path) {
            if let Some(temp_c) = parse_raw_temp(raw) {
                match s.category {
                    "cpu" => cpu_temps.push(temp_c),
                    "gpu" => gpu_temps.push(temp_c),
                    "battery" => battery_temp = Some(temp_c),
                    _ => {}
                }

                zones.push(ThermalZoneInfo {
                    id: s.id,
                    name: s.name.clone(),
                    temp: temp_c,
                    category: s.category.to_string(),
                });
            }
        }
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

#[derive(Clone, Default)]
struct PrimarySensors {
    cpu_paths: Vec<String>,
    gpu_paths: Vec<String>,
    battery_paths: Vec<String>,
}

static PRIMARY_SENSORS: OnceLock<PrimarySensors> = OnceLock::new();

fn get_primary_sensors() -> &'static PrimarySensors {
    PRIMARY_SENSORS.get_or_init(|| {
        let mut cpu_paths = Vec::new();
        let mut gpu_paths = Vec::new();
        let mut battery_paths = Vec::new();

        for i in 0..96 {
            let base = format!("/sys/class/thermal/thermal_zone{}", i);
            let type_path = format!("{}/type", base);
            let temp_path = format!("{}/temp", base);

            if !Path::new(&temp_path).exists() {
                continue;
            }

            if let Some(name) = common::read_sysfs_trimmed(&type_path) {
                if let Some(raw) = common::read_sysfs_i64(&temp_path) {
                    if parse_raw_temp(raw).is_some() {
                        let cat = categorize_zone(&name);
                        match cat {
                            "cpu" => cpu_paths.push(temp_path),
                            "gpu" => gpu_paths.push(temp_path),
                            "battery" => battery_paths.push(temp_path),
                            _ => {}
                        }
                    }
                }
            }
        }

        PrimarySensors {
            cpu_paths,
            gpu_paths,
            battery_paths,
        }
    })
}

pub fn read_primary_thermal_summary() -> ThermalSummary {
    let prim = get_primary_sensors();
    let mut cpu_max: Option<f32> = None;
    let mut gpu_max: Option<f32> = None;
    let mut battery_temp: Option<f32> = None;

    for path in &prim.cpu_paths {
        if let Some(raw) = common::read_sysfs_i64(path) {
            if let Some(t) = parse_raw_temp(raw) {
                cpu_max = Some(cpu_max.map_or(t, |c| c.max(t)));
            }
        }
    }

    for path in &prim.gpu_paths {
        if let Some(raw) = common::read_sysfs_i64(path) {
            if let Some(t) = parse_raw_temp(raw) {
                gpu_max = Some(gpu_max.map_or(t, |g| g.max(t)));
            }
        }
    }

    for path in &prim.battery_paths {
        if let Some(raw) = common::read_sysfs_i64(path) {
            if let Some(t) = parse_raw_temp(raw) {
                battery_temp = Some(t);
                break;
            }
        }
    }

    ThermalSummary {
        cpu_temp_c: cpu_max,
        gpu_temp_c: gpu_max,
        battery_temp_c: battery_temp,
        zones: Vec::new(),
    }
}

pub fn read_cpu_temperature() -> f32 {
    read_primary_thermal_summary().cpu_temp_c.unwrap_or(0.0)
}
