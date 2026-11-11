use serde::{Deserialize, Serialize};

use crate::platform::{
    battery::BatterySnapshot,
    cpu::{CoreInfo, CpuCluster},
    thermal::ThermalZoneInfo,
};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CpuTelemetry {
    pub load_pct: f32,
    pub cores: Vec<CoreInfo>,
    pub clusters: Vec<CpuCluster>,
    pub model: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GpuTelemetry {
    pub freq_mhz: i32,
    pub load_pct: i32,
    pub vendor: String,
    pub model: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MemoryTelemetry {
    pub total_mb: i64,
    pub available_mb: i64,
    pub used_mb: i64,
    pub zram_used_mb: i64,
    pub zram_total_mb: i64,
    pub swappiness: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ThermalTelemetry {
    pub cpu_temp_c: f32,
    pub gpu_temp_c: f32,
    pub battery_temp_c: f32,
    pub zones: Vec<ThermalZoneInfo>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TelemetrySnapshot {
    pub timestamp_ms: u64,
    pub cpu: CpuTelemetry,
    pub gpu: GpuTelemetry,
    pub battery: BatterySnapshot,
    pub memory: MemoryTelemetry,
    pub thermal: ThermalTelemetry,
    pub foreground_app: Option<String>,
}
