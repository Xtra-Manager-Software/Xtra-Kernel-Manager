pub mod sconfig;
pub mod zones;

pub use sconfig::{is_sconfig_supported, read_sconfig_index, set_sconfig_index, set_thermal_preset, XiaomiThermalPreset};
pub use zones::{read_cpu_temperature, scan_thermal_zones, ThermalSummary, ThermalZoneInfo};
