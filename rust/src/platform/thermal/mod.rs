pub mod sconfig;
pub mod zones;

pub use sconfig::{
    XiaomiThermalPreset, is_sconfig_supported, read_sconfig_index, set_sconfig_index,
    set_thermal_preset,
};
pub use zones::{
    ThermalSummary, ThermalZoneInfo, read_cpu_temperature, read_primary_thermal_summary,
    scan_thermal_zones,
};
