use serde::{Deserialize, Serialize};

use crate::platform::common;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BatterySnapshot {
    pub level_pct: i32,
    pub temp_c: f32,
    pub current_ma: i32,
    pub voltage_v: f32,
    pub is_charging: bool,
    pub status: String,
    pub health: String,
    pub cycle_count: Option<i32>,
}

pub fn read_battery_snapshot() -> BatterySnapshot {
    let base = "/sys/class/power_supply/battery";

    let level_pct = common::read_sysfs_i64(&format!("{}/capacity", base)).unwrap_or(0) as i32;

    let raw_temp = common::read_sysfs_i64(&format!("{}/temp", base)).unwrap_or(0);
    let temp_c = match raw_temp {
        t if t > 1000 => t as f32 / 10.0,
        t if t > 100 => t as f32 / 10.0,
        t => t as f32,
    };

    let raw_current = common::read_sysfs_i64(&format!("{}/current_now", base)).unwrap_or(0);
    let current_ma = match raw_current.abs() {
        c if c > 10_000 => (raw_current / 1000) as i32,
        _ => raw_current as i32,
    };

    let raw_voltage = common::read_sysfs_i64(&format!("{}/voltage_now", base)).unwrap_or(0);
    let voltage_v = match raw_voltage {
        v if v > 1_000_000 => v as f32 / 1_000_000.0,
        v if v > 1000 => v as f32 / 1000.0,
        v => v as f32,
    };

    let status = common::read_sysfs_trimmed(&format!("{}/status", base))
        .unwrap_or_else(|| "Discharging".to_string());
    let is_charging = status.eq_ignore_ascii_case("charging");

    // Standardize charging current convention (positive while charging)
    let current_ma = match (is_charging, current_ma < 0) {
        (true, true) => current_ma.abs(),
        _ => current_ma,
    };

    let health = common::read_sysfs_trimmed(&format!("{}/health", base))
        .unwrap_or_else(|| "Good".to_string());

    let cycle_count = common::read_sysfs_i64(&format!("{}/cycle_count", base)).map(|v| v as i32);

    BatterySnapshot {
        level_pct,
        temp_c,
        current_ma,
        voltage_v,
        is_charging,
        status,
        health,
        cycle_count,
    }
}
