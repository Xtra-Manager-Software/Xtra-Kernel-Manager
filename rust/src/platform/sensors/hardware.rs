use serde::{Deserialize, Serialize};
use std::process::Command;

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct PhysicalSensorsSummary {
    pub total_sensors_count: usize,
    pub accelerometer: Option<String>,
    pub magnetic_field: Option<String>,
    pub orientation: Option<String>,
    pub gyroscope: Option<String>,
    pub light: Option<String>,
    pub proximity: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PhysicalSensor {
    pub name: String,
    pub vendor: String,
    pub version: i32,
    pub sensor_type: String,
    pub type_id: i32,
    pub reporting_mode: String,
    pub is_wakeup: bool,
    pub min_rate_hz: Option<f32>,
    pub max_rate_hz: Option<f32>,
    pub fifo_max_events: Option<u32>,
    pub fifo_reserved_events: Option<u32>,
    pub category: String,
}

pub fn scan_hardware_sensors() -> (PhysicalSensorsSummary, Vec<PhysicalSensor>) {
    let output = match Command::new("dumpsys").arg("sensorservice").output() {
        Ok(out) => String::from_utf8_lossy(&out.stdout).to_string(),
        Err(_) => String::new(),
    };

    if output.is_empty() {
        return (PhysicalSensorsSummary::default(), Vec::new());
    }

    parse_sensorservice_output(&output)
}

pub fn parse_sensorservice_output(output: &str) -> (PhysicalSensorsSummary, Vec<PhysicalSensor>) {
    let mut sensors = Vec::new();
    let mut summary = PhysicalSensorsSummary::default();
    let mut current_sensor: Option<PhysicalSensor> = None;

    for line in output.lines() {
        let trimmed = line.trim();
        if trimmed.is_empty() {
            continue;
        }

        // Sensor header line: e.g. "0x0000000b) bmi26x Accelerometer Non-wakeup | BOSCH | ver: 65813 | type: android.sensor.accelerometer(1) | perm: n/a | flags: 0x00000980"
        if trimmed.starts_with("0x") && trimmed.contains(')') && trimmed.contains('|') {
            if let Some(prev) = current_sensor.take() {
                update_summary(&mut summary, &prev);
                sensors.push(prev);
            }

            let parts: Vec<&str> = trimmed.split('|').map(|s| s.trim()).collect();
            if parts.len() >= 4 {
                let name_raw = match parts[0].split_once(')') {
                    Some((_, rest)) => rest.trim(),
                    None => parts[0],
                };

                let is_wakeup =
                    name_raw.ends_with("Wakeup") && !name_raw.ends_with("Non-wakeup");
                let clean_name = name_raw
                    .trim_end_matches("Non-wakeup")
                    .trim_end_matches("Wakeup")
                    .trim();

                let vendor = parts[1].to_string();

                let version = parts[2]
                    .strip_prefix("ver:")
                    .and_then(|v| v.trim().parse::<i32>().ok())
                    .unwrap_or(1);

                let (sensor_type, type_id) = parse_sensor_type_and_id(parts[3]);
                let category = categorize_sensor(&sensor_type, type_id);

                current_sensor = Some(PhysicalSensor {
                    name: clean_name.to_string(),
                    vendor,
                    version,
                    sensor_type,
                    type_id,
                    reporting_mode: "continuous".to_string(),
                    is_wakeup,
                    min_rate_hz: None,
                    max_rate_hz: None,
                    fifo_max_events: None,
                    fifo_reserved_events: None,
                    category,
                });
            }
            continue;
        }

        // Detail line
        let is_mode_line = trimmed.starts_with("continuous")
            || trimmed.starts_with("on-change")
            || trimmed.starts_with("one-shot")
            || trimmed.starts_with("special-trigger");

        if let (true, Some(sensor)) = (is_mode_line, current_sensor.as_mut()) {
            for part in trimmed.split('|').map(|s| s.trim()) {
                    match () {
                        _ if part == "continuous"
                            || part == "on-change"
                            || part == "one-shot"
                            || part == "special-trigger" =>
                        {
                            sensor.reporting_mode = part.to_string();
                        }
                        _ if part.starts_with("minRate=") => {
                            let val = part
                                .trim_start_matches("minRate=")
                                .trim_end_matches("Hz")
                                .trim();
                            sensor.min_rate_hz = val.parse::<f32>().ok();
                        }
                        _ if part.starts_with("maxRate=") => {
                            let val = part
                                .trim_start_matches("maxRate=")
                                .trim_end_matches("Hz")
                                .trim();
                            sensor.max_rate_hz = val.parse::<f32>().ok();
                        }
                        _ if part.contains("FIFO") => {
                            if let Some((_, rhs)) = part.split_once('=') {
                                match (rhs.find('('), rhs.rfind(')')) {
                                    (Some(open), Some(close)) if open < close => {
                                        let inner = &rhs[open + 1..close];
                                        if let Some((max_s, res_s)) = inner.split_once(',') {
                                            sensor.fifo_max_events =
                                                max_s.trim().parse::<u32>().ok();
                                            sensor.fifo_reserved_events =
                                                res_s.trim().parse::<u32>().ok();
                                        }
                                    }
                                    _ => {}
                                }
                            }
                        }
                        _ => {}
                    }
            }
        }
    }

    if let Some(last) = current_sensor.take() {
        update_summary(&mut summary, &last);
        sensors.push(last);
    }

    summary.total_sensors_count = sensors.len();
    (summary, sensors)
}

fn parse_sensor_type_and_id(type_part: &str) -> (String, i32) {
    let s = type_part.trim_start_matches("type:").trim();
    match s.find('(') {
        Some(idx) => {
            let st = s[..idx].trim().to_string();
            let id = s[idx + 1..]
                .trim_end_matches(')')
                .trim()
                .parse::<i32>()
                .unwrap_or(0);
            (st, id)
        }
        None => (s.to_string(), 0),
    }
}

fn categorize_sensor(st: &str, type_id: i32) -> String {
    match type_id {
        1 | 4 | 9 | 10 | 11 | 15 | 16 | 20 | 35 | 36 => "Motion".to_string(),
        2 | 3 | 14 | 27 => "Position".to_string(),
        5 | 6 | 8 | 12 | 13 | 23 => "Environment".to_string(),
        18 | 19 | 21 | 22 | 24 | 25 | 26 | 29 | 30 => "Gesture & Health".to_string(),
        _ if st.contains("xiaomi") => "Xiaomi OEM".to_string(),
        _ => "Specialized".to_string(),
    }
}

fn update_summary(summary: &mut PhysicalSensorsSummary, sensor: &PhysicalSensor) {
    let formatted = format_sensor_name(&sensor.vendor, &sensor.name);
    match sensor.type_id {
        1 if summary.accelerometer.is_none() => summary.accelerometer = Some(formatted),
        2 if summary.magnetic_field.is_none() => summary.magnetic_field = Some(formatted),
        3 if summary.orientation.is_none() => summary.orientation = Some(formatted),
        4 if summary.gyroscope.is_none() => summary.gyroscope = Some(formatted),
        5 if summary.light.is_none() => summary.light = Some(formatted),
        8 if summary.proximity.is_none() => summary.proximity = Some(formatted),
        _ => {}
    }
}

fn format_sensor_name(vendor: &str, name: &str) -> String {
    let lower_name = name.to_lowercase();
    let lower_vendor = vendor.to_lowercase();

    match () {
        _ if lower_name.contains("bmi26x") => "Bosch BMI26X".to_string(),
        _ if lower_name.contains("qmc630x") => "QST QMC630X".to_string(),
        _ if lower_name.contains("tcs3701") => "AMS AG TCS3701 AMBIENT".to_string(),
        _ if lower_name.contains("proximity") => {
            if vendor.contains("(V1.1)") {
                format!("{} Proximity Proximity Sensor", vendor)
            } else {
                format!("{}(V1.1) Proximity Proximity Sensor", vendor)
            }
        }
        _ if lower_name.contains("orientation") => format!("{} Orientation", vendor),
        _ if lower_name.contains(&lower_vendor) => name.to_string(),
        _ => format!("{} {}", vendor, name),
    }
}
