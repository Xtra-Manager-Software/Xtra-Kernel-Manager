use serde::{Deserialize, Serialize};
use std::fs;
use std::path::Path;

use crate::platform::common;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DrmSecurityInfo {
    pub widevine_available: bool,
    pub widevine_security_level: String,
    pub widevine_service: Option<String>,
    pub clearkey_available: bool,
    pub clearkey_service: Option<String>,
    pub wfd_hdcp_available: bool,
    pub tee_interface: Option<String>,
    pub crypto_engine: Option<String>,
    pub keymint_service: Option<String>,
    pub display_connector: Option<String>,
    pub display_status: Option<String>,
    pub display_modes: Vec<String>,
    pub supported_refresh_rates: Vec<u32>,
    pub hdr_types: Vec<String>,
    pub wide_color_gamut: bool,
}

/// Scan for active DRM, Widevine CDM, ClearKey, TEE TrustZone, and hardware crypto accelerators.
pub fn scan_drm_security_subsystem() -> DrmSecurityInfo {
    // 1. Detect TEE (Trusted Execution Environment) & hardware crypto nodes
    let has_qseecom = Path::new("/dev/qseecom").exists();
    let has_smcinvoke = Path::new("/dev/smcinvoke").exists();
    let has_generic_tee = Path::new("/dev/tee0").exists() || Path::new("/dev/teepriv0").exists();
    let has_qce = Path::new("/dev/qce").exists();

    let tee_interface = match (has_qseecom || has_smcinvoke, has_generic_tee) {
        (true, _) => {
            let mut parts = Vec::with_capacity(2);
            if has_qseecom {
                parts.push("QSEECOM (Qualcomm Secure OS)");
            }
            if has_smcinvoke {
                parts.push("SMCInvoke (TrustZone SMC)");
            }
            Some(parts.join(" / "))
        }
        (false, true) => Some("Generic TEE / OP-TEE (/dev/tee0)".to_string()),
        (false, false) => None,
    };

    let crypto_engine = match has_qce {
        true => Some("Qualcomm QCE (Hardware AES/SHA/HMAC Engine)".to_string()),
        false => None,
    };

    // 2. Scan running DRM & Security daemons from /proc
    let mut widevine_service = None;
    let mut clearkey_service = None;
    let mut keymint_service = None;

    if let Ok(entries) = fs::read_dir("/proc") {
        for entry in entries.flatten() {
            let Ok(file_name) = entry.file_name().into_string() else {
                continue;
            };
            if !file_name.chars().all(|c| c.is_ascii_digit()) {
                continue;
            }

            let cmdline_path = entry.path().join("cmdline");
            if let Ok(cmdline) = fs::read_to_string(cmdline_path) {
                let trimmed = cmdline.trim_matches('\0');
                match () {
                    _ if trimmed.contains("widevine") && widevine_service.is_none() => {
                        widevine_service = Some(trimmed.to_string());
                    }
                    _ if trimmed.contains("clearkey") && clearkey_service.is_none() => {
                        clearkey_service = Some(trimmed.to_string());
                    }
                    _ if trimmed.contains("keymint") && keymint_service.is_none() => {
                        keymint_service = Some(trimmed.to_string());
                    }
                    _ => {}
                }
            }
        }
    }

    // Fallback to library detection if daemon cmdline reading was restricted
    let has_widevine_lib = Path::new("/vendor/lib64/mediadrm/libwvdrmengine.so").exists()
        || Path::new("/vendor/bin/hw/android.hardware.drm@1.4-service.widevine").exists();
    let widevine_available = widevine_service.is_some() || has_widevine_lib;

    let has_clearkey_lib = Path::new("/vendor/lib64/mediadrm/libdrmclearkeyplugin.so").exists()
        || Path::new("/vendor/bin/hw/android.hardware.drm-service.clearkey").exists();
    let clearkey_available = clearkey_service.is_some() || has_clearkey_lib;

    let wfd_hdcp_available = Path::new("/vendor/bin/hw/android.hardware.drm@1.3-service.wfdhdcp")
        .exists()
        || common::get_system_property("init.svc.wfdhdcphalservice")
            .as_deref()
            .map(|v| v == "running")
            .unwrap_or(false);

    // Security level: L1 when hardware TEE is verified, L3 when software only
    let widevine_security_level = match (
        widevine_available,
        tee_interface.is_some() || keymint_service.is_some(),
    ) {
        (true, true) => "L1 (Hardware Backed Secure Enclave / TrustZone)".to_string(),
        (true, false) => "L3 (Software CDM Fallback)".to_string(),
        (false, _) => "Unavailable".to_string(),
    };

    // 3. Scan primary Display DRM connector & Supported Display Modes
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
                                && !supported_refresh_rates.contains(&hz)
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
    supported_refresh_rates.sort_unstable();

    if supported_refresh_rates.is_empty() {
        supported_refresh_rates = vec![60, 90, 120];
    }

    // 4. Scan Display HDR & Color Management capabilities
    let wide_color_gamut =
        match common::get_system_property("ro.surface_flinger.has_wide_color_display") {
            Some(val) => val == "true" || val == "1",
            None => true,
        };

    let mut hdr_types = Vec::new();
    let has_hdr_display = match common::get_system_property("ro.surface_flinger.has_HDR_display") {
        Some(val) => val == "true" || val == "1",
        None => true,
    };

    if has_hdr_display {
        hdr_types.push("HDR10".to_string());
        let has_hdr10plus =
            match common::get_system_property("vendor.display.enable_hdr10_gpu_target") {
                Some(val) => val == "1",
                None => true,
            };
        if has_hdr10plus {
            hdr_types.push("HDR10+".to_string());
        }
        hdr_types.push("HLG".to_string());
    }

    DrmSecurityInfo {
        widevine_available,
        widevine_security_level,
        widevine_service,
        clearkey_available,
        clearkey_service,
        wfd_hdcp_available,
        tee_interface,
        crypto_engine,
        keymint_service,
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
