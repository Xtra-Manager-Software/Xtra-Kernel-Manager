use std::fs;
use std::path::Path;

pub struct DrmDaemonsAndTeeInfo {
    pub tee_interface: Option<String>,
    pub crypto_engine: Option<String>,
    pub widevine_service: Option<String>,
    pub clearkey_service: Option<String>,
    pub keymint_service: Option<String>,
}

pub fn scan_drm_daemons_and_tee() -> DrmDaemonsAndTeeInfo {
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
            if let Ok(cmdline) = fs::read_to_string(&cmdline_path) {
                let proc_name = cmdline.replace('\0', " ");
                let trimmed = proc_name.trim();

                match () {
                    _ if (trimmed.contains("android.hardware.drm-service.widevine")
                        || trimmed.contains("widevine@"))
                        && widevine_service.is_none() =>
                    {
                        widevine_service = Some(trimmed.to_string());
                    }
                    _ if (trimmed.contains("android.hardware.drm-service.clearkey")
                        || trimmed.contains("clearkey@"))
                        && clearkey_service.is_none() =>
                    {
                        clearkey_service = Some(trimmed.to_string());
                    }
                    _ if (trimmed.contains("android.hardware.security.keymint")
                        || trimmed.contains("keymaster@")
                        || trimmed.contains("keymint@"))
                        && keymint_service.is_none() =>
                    {
                        keymint_service = Some(trimmed.to_string());
                    }
                    _ => {}
                }
            }
        }
    }

    DrmDaemonsAndTeeInfo {
        tee_interface,
        crypto_engine,
        widevine_service,
        clearkey_service,
        keymint_service,
    }
}
