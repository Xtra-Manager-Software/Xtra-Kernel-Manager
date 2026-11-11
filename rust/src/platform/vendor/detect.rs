use std::sync::OnceLock;

use crate::platform::common;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum SocVendor {
    Snapdragon,
    MediaTek,
    Tensor,
    Exynos,
    Generic,
}

impl std::fmt::Display for SocVendor {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            SocVendor::Snapdragon => write!(f, "Qualcomm Snapdragon"),
            SocVendor::MediaTek => write!(f, "MediaTek Dimensity"),
            SocVendor::Tensor => write!(f, "Google Tensor"),
            SocVendor::Exynos => write!(f, "Samsung Exynos"),
            SocVendor::Generic => write!(f, "Generic SoC"),
        }
    }
}

static DETECTED_SOC: OnceLock<SocVendor> = OnceLock::new();

pub fn detect_soc_vendor() -> SocVendor {
    *DETECTED_SOC.get_or_init(|| {
        let hardware = common::get_system_property("ro.hardware").unwrap_or_default().to_lowercase();
        let platform = common::get_system_property("ro.board.platform").unwrap_or_default().to_lowercase();
        let soc_model = common::get_system_property("ro.soc.model").unwrap_or_default().to_lowercase();

        let combined = format!("{} {} {}", hardware, platform, soc_model);

        if combined.contains("qcom") || combined.contains("snapdragon") || combined.contains("sm8") || combined.contains("sm7") || combined.contains("lahaina") || combined.contains("taro") || combined.contains("kalama") || combined.contains("cliffs") {
            SocVendor::Snapdragon
        } else if combined.contains("mt") || combined.contains("mediatek") || combined.contains("dimensity") {
            SocVendor::MediaTek
        } else if combined.contains("tensor") || combined.contains("gs101") || combined.contains("gs201") || combined.contains("zuma") {
            SocVendor::Tensor
        } else if combined.contains("exynos") || combined.contains("s5e") {
            SocVendor::Exynos
        } else {
            SocVendor::Generic
        }
    })
}
