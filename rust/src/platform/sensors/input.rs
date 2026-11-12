use serde::{Deserialize, Serialize};
use std::fs;
use std::mem;

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct InputDevice {
    pub name: String,
    pub sysfs_path: String,
    pub handlers: String,
    pub device_type: String,
    pub bus: String,
    pub vendor: String,
    pub product: String,
    pub version: String,
}

pub fn scan_input_devices() -> Vec<InputDevice> {
    let content = match fs::read_to_string("/proc/bus/input/devices") {
        Ok(c) => c,
        Err(_) => return Vec::new(),
    };

    let mut devices = Vec::with_capacity(16);
    let mut current_name = String::new();
    let mut current_sysfs = String::new();
    let mut current_handlers = String::new();
    let mut current_bus = String::new();
    let mut current_vendor = String::new();
    let mut current_product = String::new();
    let mut current_version = String::new();

    for line in content.lines() {
        let trimmed = line.trim();
        if trimmed.is_empty() {
            if !current_name.is_empty() {
                let device_type = classify_input_device(&current_name);
                devices.push(InputDevice {
                    device_type,
                    name: mem::take(&mut current_name),
                    sysfs_path: mem::take(&mut current_sysfs),
                    handlers: mem::take(&mut current_handlers),
                    bus: mem::take(&mut current_bus),
                    vendor: mem::take(&mut current_vendor),
                    product: mem::take(&mut current_product),
                    version: mem::take(&mut current_version),
                });
            }
            continue;
        }

        match () {
            _ if trimmed.starts_with("N: Name=") => {
                let val = trimmed["N: Name=".len()..].trim_matches('"');
                current_name = val.to_string();
            }
            _ if trimmed.starts_with("S: Sysfs=") => {
                current_sysfs = trimmed["S: Sysfs=".len()..].to_string();
            }
            _ if trimmed.starts_with("H: Handlers=") => {
                current_handlers = trimmed["H: Handlers=".len()..].to_string();
            }
            _ if trimmed.starts_with("I: ") => {
                for part in trimmed["I: ".len()..].split_whitespace() {
                    let mut kv = part.split('=');
                    match (kv.next(), kv.next()) {
                        (Some("Bus"), Some(v)) => current_bus = v.to_string(),
                        (Some("Vendor"), Some(v)) => current_vendor = v.to_string(),
                        (Some("Product"), Some(v)) => current_product = v.to_string(),
                        (Some("Version"), Some(v)) => current_version = v.to_string(),
                        _ => {}
                    }
                }
            }
            _ => {}
        }
    }

    if !current_name.is_empty() {
        let device_type = classify_input_device(&current_name);
        devices.push(InputDevice {
            device_type,
            name: current_name,
            sysfs_path: current_sysfs,
            handlers: current_handlers,
            bus: current_bus,
            vendor: current_vendor,
            product: current_product,
            version: current_version,
        });
    }

    devices
}

fn classify_input_device(name: &str) -> String {
    let lower = name.to_lowercase();
    match () {
        _ if lower.contains("ts") || lower.contains("touch") => "Touchscreen".to_string(),
        _ if lower.contains("haptic") || lower.contains("vibrator") => "Haptic Motor".to_string(),
        _ if lower.contains("pwrkey") || lower.contains("key") => "Hardware Key".to_string(),
        _ if lower.contains("jack") || lower.contains("headset") => "Audio Jack".to_string(),
        _ if lower.contains("nav") || lower.contains("uinput") => "Virtual Navigation".to_string(),
        _ => "General Input".to_string(),
    }
}
