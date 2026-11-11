use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum FpsSource {
    Ebpf,
    Sysfs,
}

#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub struct FpsReading {
    pub fps: f64,
    pub source: FpsSource,
}

impl FpsReading {
    pub const fn new(fps: f64, source: FpsSource) -> Self {
        Self { fps, source }
    }
}
