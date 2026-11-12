use super::types::FrameDropCause;

/// Diagnoses root cause of a frame drop / jank by examining real-time SoC telemetry.
pub fn diagnose_drop_cause() -> FrameDropCause {
    let cpu_temp = crate::platform::thermal::read_cpu_temperature();
    let gpu_load = crate::platform::gpu::read_gpu_busy_pct();
    let cpu_load = crate::platform::cpu::read_cpu_load();
    let (mem, _) = crate::platform::memory::read_memory_and_swap();

    match () {
        _ if cpu_temp >= 75.0 => FrameDropCause::ThermalThrottling,
        _ if gpu_load >= 85 => FrameDropCause::GpuBound,
        _ if cpu_load >= 85.0 => FrameDropCause::CpuBound,
        _ if mem.available_kb > 0 && mem.available_kb < 350 * 1024 => {
            FrameDropCause::MemoryPressure
        }
        _ => FrameDropCause::AppStall,
    }
}
