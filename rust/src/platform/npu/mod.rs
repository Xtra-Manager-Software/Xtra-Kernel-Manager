use serde::{Deserialize, Serialize};
use std::fs;
use std::path::Path;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CoprocessorInfo {
    pub name: String,
    pub subsystem_type: String,
    pub state: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct NpuInfo {
    pub is_available: bool,
    pub vendor_family: String,
    pub hardware_state: String,
    pub coprocessors: Vec<CoprocessorInfo>,
    pub dev_nodes: Vec<String>,
    pub active_daemons: Vec<String>,
    pub ai_runtimes: Vec<String>,
    pub hal_services: Vec<String>,
}

/// Scan for active NPU (Neural Processing Unit), AI accelerators, and DSP coprocessors.
pub fn scan_npu_subsystem() -> NpuInfo {
    let mut coprocessors = Vec::new();
    let mut dev_nodes = Vec::new();
    let mut active_daemons = Vec::new();
    let mut ai_runtimes = Vec::new();
    let mut hal_services = Vec::new();

    // 1. Scan Linux remoteproc coprocessor framework
    if let Ok(entries) = fs::read_dir("/sys/class/remoteproc") {
        for entry in entries.flatten() {
            let path = entry.path();
            let name = fs::read_to_string(path.join("name"))
                .map(|s| s.trim().to_string())
                .unwrap_or_default();
            let state = fs::read_to_string(path.join("state"))
                .map(|s| s.trim().to_string())
                .unwrap_or_else(|_| "unknown".to_string());

            if name.is_empty() {
                continue;
            }

            let lower_name = name.to_lowercase();
            let subsystem_type = match () {
                _ if lower_name.contains("cdsp") => "Compute DSP (NPU/HTP Tensor Core)".to_string(),
                _ if lower_name.contains("adsp") => {
                    "Audio DSP (Hexagon Voice/Audio AI)".to_string()
                }
                _ if lower_name.contains("slpi") => "Sensor Low-Power Island".to_string(),
                _ if lower_name.contains("mss") => "Modem Subsystem".to_string(),
                _ if lower_name.contains("npu")
                    || lower_name.contains("apu")
                    || lower_name.contains("tpu") =>
                {
                    "Neural Processing Unit".to_string()
                }
                _ => "Hardware Coprocessor".to_string(),
            };

            coprocessors.push(CoprocessorInfo {
                name,
                subsystem_type,
                state,
            });
        }
    }

    // 2. Scan compute character device nodes
    let candidate_devs = [
        "/dev/adsprpc-smd",
        "/dev/adsprpc-smd-secure",
        "/dev/cdsprpc-smd",
        "/dev/msm_npu",
        "/dev/kgsl-3d0",
        "/dev/apusys",
        "/dev/mtk_apu",
        "/dev/edgetpu",
        "/dev/janeiro",
        "/dev/vertex",
        "/dev/npu",
    ];

    for dev in candidate_devs {
        if Path::new(dev).exists() {
            dev_nodes.push(dev.to_string());
        }
    }

    // 3. Scan running DSP / NPU daemons from /proc
    let candidate_daemons = [
        "cdsprpcd",
        "adsprpcd",
        "dspservice",
        "audioadsprpcd",
        "edgetpu_service",
        "apu_hidl_service",
    ];

    if let Ok(proc_entries) = fs::read_dir("/proc") {
        for entry in proc_entries.flatten() {
            let Ok(file_name) = entry.file_name().into_string() else {
                continue;
            };
            if !file_name.chars().all(|c| c.is_ascii_digit()) {
                continue;
            }

            let comm_path = entry.path().join("comm");
            if let Ok(comm) = fs::read_to_string(comm_path) {
                let trimmed = comm.trim();
                for &d in &candidate_daemons {
                    if trimmed == d && !active_daemons.contains(&d.to_string()) {
                        active_daemons.push(d.to_string());
                    }
                }
            }
        }
    }

    // 4. Scan vendor AI runtimes and shared libraries
    let candidate_libs = [
        (
            "/vendor/lib64/libcdsprpc.so",
            "Qualcomm cDSP Compute FastRPC",
        ),
        ("/vendor/lib64/libadsprpc.so", "Qualcomm aDSP FastRPC"),
        (
            "/vendor/lib64/libsnpe_dsp_domains_v3.so",
            "Snapdragon Neural Processing Engine (SNPE v3)",
        ),
        (
            "/vendor/lib64/libsnpe_dsp_domains_v2.so",
            "Snapdragon Neural Processing Engine (SNPE v2)",
        ),
        (
            "/vendor/lib64/libfastcvdsp_stub.so",
            "Fast Computer Vision DSP (FastCV)",
        ),
        (
            "/vendor/lib64/libQnnHtp.so",
            "Qualcomm Neural Network (QNN HTP Engine)",
        ),
        (
            "/vendor/lib64/libQnnGpu.so",
            "Qualcomm Neural Network (QNN GPU Engine)",
        ),
        (
            "/vendor/lib64/libneuron_runtime.so",
            "MediaTek NeuroPilot Runtime",
        ),
        (
            "/system/lib64/libnpumanager.so",
            "Android NpuManager Framework (libnpumanager.so)",
        ),
        (
            "/system/lib64/libneuralnetworks.so",
            "Android Neural Networks API (NNAPI)",
        ),
    ];

    for &(lib_path, desc) in &candidate_libs {
        if Path::new(lib_path).exists() {
            ai_runtimes.push(desc.to_string());
        }
    }

    // 5. Scan VINTF & HAL declarations for NNAPI / NPU
    let vintf_paths = [
        "/system/etc/vintf/compatibility_matrix.device.xml",
        "/system/etc/vintf/compatibility_matrix.202604.xml",
        "/system/etc/vintf/compatibility_matrix.202404.xml",
        "/system/etc/vintf/compatibility_matrix.8.xml",
        "/vendor/etc/vintf/manifest.xml",
    ];

    for vpath in vintf_paths {
        if let Ok(content) = fs::read_to_string(vpath) {
            if content.contains("android.hardware.neuralnetworks")
                && !hal_services
                    .contains(&"android.hardware.neuralnetworks (AIDL/VINTF)".to_string())
            {
                hal_services.push("android.hardware.neuralnetworks (AIDL/VINTF)".to_string());
            }
            if content.contains("android.hardware.npu")
                && !hal_services.contains(&"android.hardware.npu (AIDL/VINTF)".to_string())
            {
                hal_services.push("android.hardware.npu (AIDL/VINTF)".to_string());
            }
        }
    }

    if Path::new("/vendor/lib64/vendor.qti.hardware.dsp@1.0.so").exists() {
        hal_services.push("vendor.qti.hardware.dsp@1.0 (Hexagon DSP HAL)".to_string());
    }

    // 6. Determine vendor family and availability
    let has_cdsp = coprocessors
        .iter()
        .any(|c| c.name.contains("cdsp") && c.state == "running")
        || dev_nodes
            .iter()
            .any(|d| d.contains("cdsprpc") || d.contains("adsprpc"))
        || active_daemons.iter().any(|d| d.contains("cdsprpcd"));

    let has_apu = dev_nodes.iter().any(|d| d.contains("apu"))
        || active_daemons.iter().any(|d| d.contains("apu"));

    let has_tpu = dev_nodes
        .iter()
        .any(|d| d.contains("edgetpu") || d.contains("janeiro"))
        || active_daemons.iter().any(|d| d.contains("edgetpu"));

    let has_exynos_npu = dev_nodes
        .iter()
        .any(|d| d.contains("vertex") || d.contains("npu"));

    let (vendor_family, is_available) = match () {
        _ if has_cdsp => (
            "Qualcomm Hexagon (HTP/cDSP Tensor Engine)".to_string(),
            true,
        ),
        _ if has_apu => ("MediaTek NeuroPilot APU".to_string(), true),
        _ if has_tpu => ("Google Tensor EdgeTPU".to_string(), true),
        _ if has_exynos_npu => ("Samsung Exynos NPU".to_string(), true),
        _ if !ai_runtimes.is_empty() || !hal_services.is_empty() => {
            ("Android Generic NNAPI / NpuManager".to_string(), true)
        }
        _ => ("Not Detected".to_string(), false),
    };

    let hardware_state = match (
        is_available,
        coprocessors.iter().any(|c| c.state == "running"),
    ) {
        (true, true) => "Active (Hardware Powered & Running)".to_string(),
        (true, false) => "Available (Driver/Library Accessible)".to_string(),
        (false, _) => "Offline / Unsupported".to_string(),
    };

    NpuInfo {
        is_available,
        vendor_family,
        hardware_state,
        coprocessors,
        dev_nodes,
        active_daemons,
        ai_runtimes,
        hal_services,
    }
}
