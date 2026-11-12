pub mod caps;
pub mod ffi;
pub mod helpers;
pub mod loader;
pub mod props;
pub mod structs;

pub use caps::build_vulkan_capabilities;
pub use loader::query_vulkan_driver_live;

use crate::platform::common;
use super::opencl::probe_opencl_hardware;
use super::types::FullGpuCapabilities;

pub fn scan_full_gpu_capabilities() -> FullGpuCapabilities {
    let (sf_vendor, sf_renderer, sf_opengl) = match query_opengl_from_surfaceflinger() {
        Some((v, r, gl)) => (v, r, gl),
        None => (
            common::get_system_property("ro.hardware.egl").unwrap_or_default(),
            common::read_sysfs_trimmed("/sys/class/kgsl/kgsl-3d0/gpu_model").unwrap_or_default(),
            String::new(),
        ),
    };

    let chip_name = match common::read_sysfs_trimmed("/sys/class/kgsl/kgsl-3d0/gpu_model") {
        Some(m) if m.starts_with("Adreno") => m.trim_start_matches("Adreno").to_string(),
        Some(m) => m,
        None => String::new(),
    };

    // Dynamically probe hardware GPU compute units and on-chip memory via OpenCL HAL
    let (compute_units, on_chip_memory) = match probe_opencl_hardware() {
        Some(cl) => (cl.compute_units, cl.on_chip_memory),
        None => (0, String::new()),
    };

    let vulkan = query_vulkan_driver_live();

    FullGpuCapabilities {
        vendor: sf_vendor,
        renderer: sf_renderer,
        chip: chip_name,
        on_chip_memory,
        compute_units,
        vulkan_api_version: vulkan.device_api.clone(),
        opengl_es_version: sf_opengl,
        vulkan,
    }
}

fn query_opengl_from_surfaceflinger() -> Option<(String, String, String)> {
    let output = std::process::Command::new("dumpsys")
        .arg("SurfaceFlinger")
        .output()
        .ok()?;
    let text = String::from_utf8_lossy(&output.stdout);
    for line in text.lines() {
        let trimmed = line.trim();
        if trimmed.starts_with("GLES:") {
            let rest = trimmed.trim_start_matches("GLES:").trim();
            let parts: Vec<&str> = rest.splitn(3, ',').collect();
            if let [v, r, gl] = parts.as_slice() {
                return Some((
                    v.trim().to_string(),
                    r.trim().to_string(),
                    gl.trim().to_string(),
                ));
            }
        }
    }
    None
}
