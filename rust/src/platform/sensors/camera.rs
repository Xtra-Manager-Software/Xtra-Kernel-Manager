use std::fs;
use std::process::Command;

pub use super::camera_dumpsys::parse_camera_dumpsys;
pub use super::camera_types::{CameraDeviceDetails, CameraSensorsSummary};

pub fn scan_camera_subsystem() -> (CameraSensorsSummary, Vec<CameraDeviceDetails>) {
    let summary = scan_camera_libraries();
    let devices = scan_camera_devices(&summary);
    (summary, devices)
}

pub fn scan_camera_libraries() -> CameraSensorsSummary {
    let mut summary = CameraSensorsSummary::default();

    let search_dirs = [
        "/vendor/lib64/camera",
        "/vendor/lib/camera",
        "/vendor/etc/camera",
    ];

    for dir in search_dirs {
        let entries = match fs::read_dir(dir) {
            Ok(e) => e,
            Err(_) => continue,
        };

        for entry in entries.flatten() {
            let fname = entry.file_name().to_string_lossy().to_string();
            let lower = fname.to_lowercase();

            match () {
                _ if (lower.contains("ov64b") || lower.contains("ov64b40"))
                    && summary.rear_camera.is_empty() =>
                {
                    summary.rear_camera = "OmniVision OV64B40".to_string();
                }
                _ if lower.contains("imx355") && summary.ultra_wide_lens.is_empty() => {
                    summary.ultra_wide_lens = "Sony IMX355".to_string();
                }
                _ if (lower.contains("s5k3p9") || lower.contains("s5k3p9sp04"))
                    && summary.front_camera.is_empty() =>
                {
                    summary.front_camera = "Samsung S5K3P9SP04".to_string();
                }
                _ if (lower.contains("gc02m1") || lower.contains("gc02m1b"))
                    && summary.macro_lens.is_empty() =>
                {
                    summary.macro_lens = "GalaxyCore GC02M1".to_string();
                }
                _ => {}
            }
        }
    }

    summary
}

pub fn scan_camera_devices(summary: &CameraSensorsSummary) -> Vec<CameraDeviceDetails> {
    let raw_dump = match Command::new("dumpsys").arg("media.camera").output() {
        Ok(out) => match out.status.success() {
            true => String::from_utf8_lossy(&out.stdout).into_owned(),
            false => return Vec::new(),
        },
        Err(_) => return Vec::new(),
    };

    parse_camera_dumpsys(&raw_dump, summary)
}
