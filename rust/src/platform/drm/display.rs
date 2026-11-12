use std::fs;
use crate::platform::common;

pub struct DisplayDrmInfo {
    pub display_connector: Option<String>,
    pub display_status: Option<String>,
    pub display_modes: Vec<String>,
    pub supported_refresh_rates: Vec<u32>,
    pub hdr_types: Vec<String>,
    pub wide_color_gamut: bool,
}

pub fn scan_drm_display_subsystem() -> DisplayDrmInfo {
    let mut display_connector = None;
    let mut display_status = None;
    let mut display_modes = Vec::new();
    let mut supported_refresh_rates: Vec<u32> = Vec::new();

    if let Ok(entries) = fs::read_dir("/sys/class/drm") {
        for entry in entries.flatten() {
            let name = entry.file_name().to_string_lossy().to_string();
            if name.starts_with("card0-") && (name.contains("DSI") || name.contains("eDP")) {
                let status_path = entry.path().join("status");
                let status = match fs::read_to_string(&status_path) {
                    Ok(s) => s.trim().to_string(),
                    Err(_) => "connected".to_string(),
                };

                let modes_path = entry.path().join("modes");
                if let Ok(modes_str) = fs::read_to_string(modes_path) {
                    for line in modes_str.lines() {
                        let trimmed = line.trim();
                        if !trimmed.is_empty() {
                            display_modes.push(trimmed.to_string());
                            if let Some(hz) = parse_refresh_rate_from_mode(trimmed)
                                .filter(|h| !supported_refresh_rates.contains(h))
                            {
                                supported_refresh_rates.push(hz);
                            }
                        }
                    }
                }

                display_connector = Some(name);
                display_status = Some(status);
                break;
            }
        }
    }

    // If sysfs didn't expose modes, query dumpsys SurfaceFlinger dynamically
    if supported_refresh_rates.is_empty() {
        let flinger_res = std::process::Command::new("dumpsys")
            .arg("SurfaceFlinger")
            .output();
        if let Ok(output) = flinger_res {
            let text = String::from_utf8_lossy(&output.stdout);
            for line in text.lines() {
                if let Some(pos) = line.find("vsyncRate=") {
                    let rest = &line[pos + 10..];
                    let num_str: String = rest
                        .chars()
                        .take_while(|c| c.is_ascii_digit() || *c == '.')
                        .collect();
                    if let Ok(f) = num_str.parse::<f32>() {
                        let hz = f.round() as u32;
                        if hz > 0 && !supported_refresh_rates.contains(&hz) {
                            supported_refresh_rates.push(hz);
                        }
                    }
                }
            }
        }
    }

    supported_refresh_rates.sort_unstable();

    // Scan Display HDR & Color Management capabilities dynamically from dumpsys display
    let mut hdr_types = Vec::new();
    let mut wide_color_gamut =
        match common::get_system_property("ro.surface_flinger.has_wide_color_display") {
            Some(val) => val == "true" || val == "1",
            None => false,
        };

    if let Ok(output) = std::process::Command::new("dumpsys")
        .arg("display")
        .output()
    {
        let text = String::from_utf8_lossy(&output.stdout);
        for line in text.lines() {
            if let Some(start) = line.find("mSupportedHdrTypes=[") {
                let sub = &line[start + 20..];
                if let Some(end) = sub.find(']') {
                    for num_str in sub[..end].split(',') {
                        match num_str.trim() {
                            "1" if !hdr_types.contains(&"Dolby Vision".to_string()) => {
                                hdr_types.push("Dolby Vision".to_string())
                            }
                            "2" if !hdr_types.contains(&"HDR10".to_string()) => {
                                hdr_types.push("HDR10".to_string())
                            }
                            "3" if !hdr_types.contains(&"HLG".to_string()) => {
                                hdr_types.push("HLG".to_string())
                            }
                            "4" if !hdr_types.contains(&"HDR10+".to_string()) => {
                                hdr_types.push("HDR10+".to_string())
                            }
                            _ => {}
                        }
                    }
                }
            }
            if line.contains("supportedColorModes [") && (line.contains('7') || line.contains('9'))
            {
                wide_color_gamut = true;
            }
        }
    }

    match (
        hdr_types.is_empty(),
        common::get_system_property("ro.surface_flinger.has_HDR_display"),
    ) {
        (true, Some(val)) if val == "true" || val == "1" => {
            hdr_types.push("HDR10".to_string());
            if let Some("1") = common::get_system_property("vendor.display.enable_hdr10_gpu_target").as_deref() {
                hdr_types.push("HDR10+".to_string());
            }
        }
        _ => {}
    }

    DisplayDrmInfo {
        display_connector,
        display_status,
        display_modes,
        supported_refresh_rates,
        hdr_types,
        wide_color_gamut,
    }
}

/// Helper function to parse refresh rate (Hz) from standard DRM modes (e.g. "1080x2400x120vid").
fn parse_refresh_rate_from_mode(mode: &str) -> Option<u32> {
    let parts: Vec<&str> = mode.split(['x', '@', '-']).collect();
    match parts.as_slice() {
        [_, _, hz_part, ..] => {
            let digits: String = hz_part.chars().take_while(|c| c.is_ascii_digit()).collect();
            digits.parse::<u32>().ok()
        }
        _ => None,
    }
}
