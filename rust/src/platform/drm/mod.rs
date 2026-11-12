use serde::{Deserialize, Serialize};
use std::fs;
use std::path::Path;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DrmSecurityInfo {
    pub widevine_available: bool,
    pub widevine_security_level: String,
    pub widevine_service: Option<String>,
    pub clearkey_available: bool,
    pub clearkey_service: Option<String>,
    pub tee_interface: Option<String>,
    pub crypto_engine: Option<String>,
    pub keymint_service: Option<String>,
    pub display_connector: Option<String>,
    pub display_status: Option<String>,
}

/// Scan for active DRM, Widevine CDM, ClearKey, TEE TrustZone, and hardware crypto accelerators.
pub fn scan_drm_security_subsystem() -> DrmSecurityInfo {
    // 1. Detect TEE (Trusted Execution Environment) & hardware crypto nodes
    let has_qseecom = Path::new("/dev/qseecom").exists();
    let has_smcinvoke = Path::new("/dev/smcinvoke").exists();
    let has_generic_tee = Path::new("/dev/tee0").exists() || Path::new("/dev/teepriv0").exists();
    let has_qce = Path::new("/dev/qce").exists();

    let tee_interface = if has_qseecom || has_smcinvoke {
        let mut parts = Vec::new();
        if has_qseecom {
            parts.push("QSEECOM (Qualcomm Secure OS)");
        }
        if has_smcinvoke {
            parts.push("SMCInvoke (TrustZone SMC)");
        }
        Some(parts.join(" / "))
    } else if has_generic_tee {
        Some("Generic TEE / OP-TEE (/dev/tee0)".to_string())
    } else {
        None
    };

    let crypto_engine = if has_qce {
        Some("Qualcomm QCE (Hardware AES/SHA/HMAC Engine)".to_string())
    } else {
        None
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
                if cmdline.contains("widevine") && widevine_service.is_none() {
                    widevine_service = Some(cmdline.trim_matches('\0').to_string());
                } else if cmdline.contains("clearkey") && clearkey_service.is_none() {
                    clearkey_service = Some(cmdline.trim_matches('\0').to_string());
                } else if cmdline.contains("keymint") && keymint_service.is_none() {
                    keymint_service = Some(cmdline.trim_matches('\0').to_string());
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

    // Security level: L1 when hardware TEE is verified, L3 when software only
    let widevine_security_level = if widevine_available {
        if tee_interface.is_some() || keymint_service.is_some() {
            "L1 (Hardware Backed Secure Enclave / TrustZone)".to_string()
        } else {
            "L3 (Software CDM Fallback)".to_string()
        }
    } else {
        "Unavailable".to_string()
    };

    // 3. Scan primary Display DRM connector
    let mut display_connector = None;
    let mut display_status = None;

    if let Ok(entries) = fs::read_dir("/sys/class/drm") {
        for entry in entries.flatten() {
            let name = entry.file_name().to_string_lossy().to_string();
            if name.starts_with("card0-") && (name.contains("DSI") || name.contains("eDP")) {
                let status_path = entry.path().join("status");
                let status = fs::read_to_string(status_path)
                    .map(|s| s.trim().to_string())
                    .unwrap_or_else(|_| "connected".to_string());

                display_connector = Some(name);
                display_status = Some(status);
                break;
            }
        }
    }

    DrmSecurityInfo {
        widevine_available,
        widevine_security_level,
        widevine_service,
        clearkey_available,
        clearkey_service,
        tee_interface,
        crypto_engine,
        keymint_service,
        display_connector,
        display_status,
    }
}
