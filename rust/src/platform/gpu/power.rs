use anyhow::Result;
use std::fs;
use std::path::Path;
use tracing::debug;

use super::detect::{GpuVendor, get_gpu_info};

pub fn set_gpu_performance_mode() -> Result<()> {
    let (vendor, _) = get_gpu_info();
    match vendor {
        GpuVendor::Qualcomm => set_adreno_performance()?,
        GpuVendor::Mali => set_mali_performance()?,
        GpuVendor::Unknown => {
            debug!("GPU vendor unknown, skipping GPU performance mode");
        }
    }
    Ok(())
}

pub fn set_gpu_powersave_mode() -> Result<()> {
    let (vendor, _) = get_gpu_info();
    match vendor {
        GpuVendor::Qualcomm => set_adreno_powersave()?,
        GpuVendor::Mali => set_mali_powersave()?,
        GpuVendor::Unknown => {
            debug!("GPU vendor unknown, skipping GPU powersave mode");
        }
    }
    Ok(())
}

fn set_adreno_performance() -> Result<()> {
    let base = "/sys/class/kgsl/kgsl-3d0";
    if !Path::new(base).exists() {
        return Ok(());
    }

    // Set performance governor on kgsl
    let _ = fs::write(format!("{}/devfreq/governor", base), "performance");
    let _ = fs::write(format!("{}/max_pwrlevel", base), "0"); // 0 is max boost in KGSL
    let _ = fs::write(format!("{}/force_clk_on", base), "1");
    let _ = fs::write(format!("{}/force_bus_on", base), "1");
    let _ = fs::write(format!("{}/idle_timer", base), "10000");
    debug!("Adreno performance mode applied");
    Ok(())
}

fn set_adreno_powersave() -> Result<()> {
    let base = "/sys/class/kgsl/kgsl-3d0";
    if !Path::new(base).exists() {
        return Ok(());
    }

    let _ = fs::write(format!("{}/devfreq/governor", base), "msm-adreno-tz");

    let num_pwr = fs::read_to_string(format!("{}/num_pwrlevels", base))
        .ok()
        .and_then(|n| n.trim().parse::<u32>().ok())
        .unwrap_or(8);

    let cap_level = (num_pwr / 2).max(1);
    let min_pwr = match num_pwr {
        0 => 8,
        n => n - 1,
    };

    let _ = fs::write(format!("{}/max_pwrlevel", base), cap_level.to_string());
    let _ = fs::write(format!("{}/min_pwrlevel", base), min_pwr.to_string());
    let _ = fs::write(format!("{}/force_clk_on", base), "0");
    let _ = fs::write(format!("{}/force_bus_on", base), "0");
    let _ = fs::write(format!("{}/idle_timer", base), "40");
    debug!("Adreno powersave mode applied");
    Ok(())
}

fn set_mali_performance() -> Result<()> {
    if Path::new("/proc/gpufreqv2/fix_target_opp_index").exists() {
        let _ = fs::write("/proc/gpufreqv2/fix_target_opp_index", "0");
    }
    let _ = fs::write("/sys/devices/platform/mali/power_policy", "always_on");
    Ok(())
}

fn set_mali_powersave() -> Result<()> {
    if Path::new("/proc/gpufreqv2/fix_target_opp_index").exists() {
        let _ = fs::write("/proc/gpufreqv2/fix_target_opp_index", "-1");
    }
    let _ = fs::write("/sys/devices/platform/mali/power_policy", "coarse_demand");
    Ok(())
}
