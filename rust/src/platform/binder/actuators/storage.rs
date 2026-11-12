use anyhow::Result;
use serde::Serialize;
use std::fs;

pub const VOLD_SERVICE: &str = "vold";

#[derive(Debug, Clone, Serialize)]
pub struct StorageHealth {
    pub lifetime_type_a_pct: i32,
    pub lifetime_type_b_pct: i32,
    pub eol_status: String,
}

/// Trigger filesystem TRIM across all mounted partitions via vold Binder or fall back to /data.
pub fn run_fstrim() -> Result<bool> {
    #[cfg(target_os = "android")]
    {
        if crate::platform::binder::ensure_initialized() {
            if let Some(svc) = rsbinder::hub::check_service(VOLD_SERVICE) {
                if let Some(proxy) = svc.as_proxy() {
                    // Vold fstrim transaction (TRANSACTION_fstrim)
                    if let Ok(mut data) = proxy.prepare_transact(true) {
                        // Flags: 0, callback: null
                        let _ = data.write(&0i32);
                        if let Ok(Some(mut reply)) = proxy.submit_transact(18, &data, 0) {
                            if let Ok(0) = reply.read_i32() {
                                return Ok(true);
                            }
                        }
                    }
                }
            }
        }

        // Fallback: system command
        let status = std::process::Command::new("sm").args(["fstrim"]).status();
        Ok(status.map(|s| s.success()).unwrap_or(false))
    }

    #[cfg(not(target_os = "android"))]
    {
        Ok(true)
    }
}

/// Query flash memory lifetime estimation (eMMC / UFS health).
/// Values map to wear % (e.g., 0x01 = 0-10% used, 0x05 = 40-50% used, 0x0A = 90-100% EOL).
pub fn get_storage_life_time() -> Result<StorageHealth> {
    // 1. Check UFS health descriptor
    let ufs_paths = [
        "/sys/class/block/bootdevice/health_descriptor",
        "/sys/devices/platform/soc/1d84000.ufshc/health_descriptor",
        "/sys/kernel/debug/ufshcd0/dump_device_desc",
    ];

    for path in ufs_paths {
        if let Ok(content) = fs::read_to_string(path) {
            let mut type_a = -1;
            let mut type_b = -1;
            let mut eol = "Normal".to_string();

            for line in content.lines() {
                let lower = line.to_lowercase();
                if lower.contains("lifetimea") || lower.contains("life_time_a") {
                    if let Some(val) = parse_hex_or_dec(line) {
                        type_a = val * 10;
                    }
                } else if lower.contains("lifetimeb") || lower.contains("life_time_b") {
                    if let Some(val) = parse_hex_or_dec(line) {
                        type_b = val * 10;
                    }
                } else if lower.contains("eol") {
                    if lower.contains("0x01") || lower.contains("normal") {
                        eol = "Normal".to_string();
                    } else if lower.contains("0x02") || lower.contains("warning") {
                        eol = "Warning (80% consumed)".to_string();
                    } else if lower.contains("0x03") || lower.contains("urgent") {
                        eol = "Urgent (Near EOL)".to_string();
                    }
                }
            }

            if type_a >= 0 || type_b >= 0 {
                return Ok(StorageHealth {
                    lifetime_type_a_pct: type_a.clamp(0, 100),
                    lifetime_type_b_pct: type_b.clamp(0, 100),
                    eol_status: eol,
                });
            }
        }
    }

    // 2. Check eMMC life time
    let emmc_paths = [
        "/sys/block/mmcblk0/device/life_time",
        "/sys/class/block/mmcblk0/device/life_time",
    ];

    for path in emmc_paths {
        if let Ok(content) = fs::read_to_string(path) {
            let parts: Vec<i32> = content
                .split_whitespace()
                .filter_map(|s| {
                    if let Some(hex) = s.strip_prefix("0x").or_else(|| s.strip_prefix("0X")) {
                        i32::from_str_radix(hex, 16).ok()
                    } else {
                        s.parse::<i32>().ok()
                    }
                })
                .collect();

            if parts.len() >= 2 {
                return Ok(StorageHealth {
                    lifetime_type_a_pct: (parts[0] * 10).clamp(0, 100),
                    lifetime_type_b_pct: (parts[1] * 10).clamp(0, 100),
                    eol_status: "Normal".to_string(),
                });
            }
        }
    }

    anyhow::bail!("Storage health descriptor not exposed on this hardware")
}

fn parse_hex_or_dec(line: &str) -> Option<i32> {
    for part in line.split(|c: char| c == ':' || c == '=' || c.is_whitespace()) {
        let trimmed = part.trim();
        if let Some(hex) = trimmed
            .strip_prefix("0x")
            .or_else(|| trimmed.strip_prefix("0X"))
        {
            if let Ok(v) = i32::from_str_radix(hex, 16) {
                return Some(v);
            }
        } else if let Ok(v) = trimmed.parse::<i32>() {
            return Some(v);
        }
    }
    None
}
