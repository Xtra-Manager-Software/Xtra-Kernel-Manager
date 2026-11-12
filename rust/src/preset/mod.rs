use anyhow::Result;
use tracing::info;

use crate::platform::{
    cpu, gpu, memory,
    thermal::{self, XiaomiThermalPreset},
};

pub fn apply_preset(name: &str) -> Result<()> {
    let mode = name.trim().to_lowercase();
    info!("Applying system tuning preset: {}", mode);

    match mode.as_str() {
        "powersave" => {
            let _ = cpu::set_all_governors("powersave");
            let _ = gpu::set_gpu_powersave_mode();
            let _ = memory::set_swappiness(100);
            if thermal::is_sconfig_supported() {
                let _ = thermal::set_thermal_preset(XiaomiThermalPreset::Class0);
            }
        }
        "balance" | "balanced" => {
            cpu::online_all_cores();
            let _ = cpu::set_all_governors("schedutil");
            let _ = cpu::unlock_all(); // Release any hard frequency locks
            let _ = memory::set_swappiness(60);
            if thermal::is_sconfig_supported() {
                let _ = thermal::set_thermal_preset(XiaomiThermalPreset::Dynamic);
            }
        }
        "performance" => {
            cpu::online_all_cores();
            let _ = cpu::set_all_governors("performance");
            let _ = gpu::set_gpu_performance_mode();
            let _ = memory::drop_caches();
            let _ = memory::set_swappiness(30);
            if thermal::is_sconfig_supported() {
                let _ = thermal::set_thermal_preset(XiaomiThermalPreset::Extreme);
            }
        }
        "gaming" | "fas" => {
            cpu::online_all_cores();
            let _ = cpu::set_all_governors("performance");
            let _ = gpu::set_gpu_performance_mode();
            let _ = memory::drop_caches();
            let _ = memory::set_swappiness(10);
            if thermal::is_sconfig_supported() {
                let _ = thermal::set_thermal_preset(XiaomiThermalPreset::Extreme);
            }
        }
        _ => {
            anyhow::bail!("Unknown preset name: {}", name);
        }
    }

    Ok(())
}

pub fn restore_defaults() -> Result<()> {
    cpu::online_all_cores();
    let _ = cpu::set_all_governors("schedutil");
    let _ = cpu::unlock_all();
    let _ = memory::set_swappiness(60);
    if thermal::is_sconfig_supported() {
        let _ = thermal::set_thermal_preset(XiaomiThermalPreset::Dynamic);
    }
    Ok(())
}
