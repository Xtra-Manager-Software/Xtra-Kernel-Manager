use serde::Serialize;
use std::fs;
use std::path::Path;

#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct KernelDriverInfo {
    pub name: String,
    pub bus: String,
    pub bound_devices: Vec<String>,
}

const MONITORED_BUSES: &[&str] = &["platform", "i2c", "spi", "pci", "usb"];

/// Scan and list all active hardware drivers registered across the Linux device model bus.
pub fn list_kernel_drivers() -> Vec<KernelDriverInfo> {
    let mut drivers = Vec::new();

    for &bus in MONITORED_BUSES {
        let bus_drivers_dir = format!("/sys/bus/{}/drivers", bus);
        let Ok(entries) = fs::read_dir(bus_drivers_dir) else {
            continue;
        };

        for entry in entries.flatten() {
            let file_name = entry.file_name();
            let driver_name = file_name.to_string_lossy().to_string();

            // Collect devices bound to this driver (symlinks in the driver folder)
            let mut bound_devices = Vec::new();
            if let Ok(dev_entries) = fs::read_dir(entry.path()) {
                for dev in dev_entries.flatten() {
                    let dev_name = dev.file_name().to_string_lossy().to_string();
                    // Skip sysfs standard control files: bind, unbind, uevent, module
                    if dev_name == "bind"
                        || dev_name == "unbind"
                        || dev_name == "uevent"
                        || dev_name == "module"
                    {
                        continue;
                    }
                    if Path::new(&dev.path()).is_dir() || dev.path().read_link().is_ok() {
                        bound_devices.push(dev_name);
                    }
                }
            }

            drivers.push(KernelDriverInfo {
                name: driver_name,
                bus: bus.to_string(),
                bound_devices,
            });
        }
    }

    drivers
}
