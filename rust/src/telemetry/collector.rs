use std::time::{SystemTime, UNIX_EPOCH};

use super::snapshot::{
    CpuTelemetry, GpuTelemetry, MemoryTelemetry, TelemetrySnapshot, ThermalTelemetry,
};
use crate::platform::{
    battery, binder, cpu, gpu, memory, thermal,
};

pub fn collect_snapshot() -> TelemetrySnapshot {
    let timestamp_ms = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis() as u64)
        .unwrap_or(0);

    // CPU
    let cpu_load = cpu::read_cpu_load();
    let cores = cpu::read_core_data();
    let clusters = cpu::detect_cpu_clusters();
    let cpu_model = cpu::get_cpu_model();

    let cpu_telemetry = CpuTelemetry {
        load_pct: cpu_load,
        cores,
        clusters,
        model: cpu_model,
    };

    // GPU
    let gpu_freq = gpu::read_gpu_freq_mhz();
    let gpu_load = gpu::read_gpu_busy_pct();
    let (gpu_vendor, gpu_model) = gpu::get_gpu_info();

    let gpu_telemetry = GpuTelemetry {
        freq_mhz: gpu_freq,
        load_pct: gpu_load,
        vendor: gpu_vendor.to_string(),
        model: gpu_model.clone(),
    };

    // Battery
    let battery_snap = battery::read_battery_snapshot();

    // Memory
    let (mem_info, _) = memory::read_memory_and_swap();
    let zram_stats = memory::read_zram_stats();
    let swappiness = memory::get_swappiness();

    let total_mb = mem_info.total_kb / 1024;
    let avail_mb = mem_info.available_kb / 1024;
    let used_mb = total_mb.saturating_sub(avail_mb).max(0);
    let zram_used_mb = zram_stats.mem_used_total / (1024 * 1024);
    let zram_total_mb = zram_stats.disksize / (1024 * 1024);

    let memory_telemetry = MemoryTelemetry {
        total_mb,
        available_mb: avail_mb,
        used_mb,
        zram_used_mb,
        zram_total_mb,
        swappiness,
    };

    // Thermal (high-efficiency primary sensor polling)
    let thermal_summary = thermal::read_primary_thermal_summary();
    let thermal_telemetry = ThermalTelemetry {
        cpu_temp_c: thermal_summary.cpu_temp_c.unwrap_or(0.0),
        gpu_temp_c: thermal_summary.gpu_temp_c.unwrap_or(0.0),
        battery_temp_c: thermal_summary.battery_temp_c.unwrap_or(battery_snap.temp_c),
        zones: thermal_summary.zones,
    };

    // Foreground task
    let foreground = binder::get_foreground_app().map(|f| f.package_name);

    TelemetrySnapshot {
        timestamp_ms,
        cpu: cpu_telemetry,
        gpu: gpu_telemetry,
        battery: battery_snap,
        memory: memory_telemetry,
        thermal: thermal_telemetry,
        foreground_app: foreground,
    }
}
