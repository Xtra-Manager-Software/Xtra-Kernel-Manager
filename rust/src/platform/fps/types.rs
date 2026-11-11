use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum FpsSource {
    Ebpf,
    Sysfs,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum FrameDropCause {
    Smooth,
    CpuBound,
    GpuBound,
    ThermalThrottling,
    MemoryPressure,
    AppStall,
}

impl FrameDropCause {
    pub const fn as_str(&self) -> &'static str {
        match self {
            Self::Smooth => "Smooth",
            Self::CpuBound => "CPU Bound",
            Self::GpuBound => "GPU Bound",
            Self::ThermalThrottling => "Thermal Throttling",
            Self::MemoryPressure => "Memory Pressure",
            Self::AppStall => "App Stall",
        }
    }
}

impl Default for FrameDropCause {
    fn default() -> Self {
        Self::Smooth
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub struct FpsReading {
    pub fps: f64,
    pub source: FpsSource,
    #[serde(default)]
    pub frametime_ms: f64,
    #[serde(default)]
    pub avg_fps: f64,
    #[serde(default)]
    pub low_1pct_fps: f64,
    #[serde(default)]
    pub low_01pct_fps: f64,
    #[serde(default)]
    pub std_dev_ms: f64,
    #[serde(default)]
    pub cv: f64,
    #[serde(default)]
    pub jank_count: u32,
    #[serde(default)]
    pub jank_percent: f64,
    #[serde(default)]
    pub drop_cause: FrameDropCause,
}

impl FpsReading {
    pub fn new(fps: f64, source: FpsSource) -> Self {
        let frametime_ms = if fps > 0.0 {
            (1000.0 / fps * 100.0).round() / 100.0
        } else {
            0.0
        };
        Self {
            fps,
            source,
            frametime_ms,
            avg_fps: fps,
            low_1pct_fps: fps,
            low_01pct_fps: fps,
            std_dev_ms: 0.0,
            cv: 0.0,
            jank_count: 0,
            jank_percent: 0.0,
            drop_cause: FrameDropCause::Smooth,
        }
    }
}
