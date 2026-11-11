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
    let temp_c = if raw_temp > 1000 {
        raw_temp as f32 / 10.0 // sysfs temp is often in tenths of degree (e.g. 345 = 34.5°C) or milli-°C
    } else if raw_temp > 100 {
        raw_temp as f32 / 10.0
    } else {
        raw_temp as f32
    };

    let raw_current = common::read_sysfs_i64(&format!("{}/current_now", base)).unwrap_or(0);
    let current_ma = if raw_current.abs() > 10_000 {
        (raw_current / 1000) as i32 // microamps to milliamps
    } else {
        raw_current as i32
    };

    let raw_voltage = common::read_sysfs_i64(&format!("{}/voltage_now", base)).unwrap_or(0);
    let voltage_v = if raw_voltage > 1_000_000 {
        raw_voltage as f32 / 1_000_000.0 // microvolts to volts
    } else if raw_voltage > 1000 {
        raw_voltage as f32 / 1000.0 // millivolts to volts
    } else {
        raw_voltage as f32
    };

    let status = common::read_sysfs_trimmed(&format!("{}/status", base))
        .unwrap_or_else(|| "Discharging".to_string());
    let is_charging = status.eq_ignore_ascii_case("charging");

    // Standardize charging current convention (positive while charging)
    let current_ma = if is_charging && current_ma < 0 {
        current_ma.abs()
    } else {
        current_ma
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
