use std::process::Command;
use super::types::{SimCardInfo, TelephonyInfo};

pub fn scan_telephony() -> TelephonyInfo {
    let mut sim1 = SimCardInfo {
        slot: 1,
        state: "Ready".to_string(),
        country: "ID".to_string(),
        roaming: "Disabled".to_string(),
        network_type: "4G LTE".to_string(),
        ..Default::default()
    };

    let mut sim2 = SimCardInfo {
        slot: 2,
        state: "Ready".to_string(),
        country: "ID".to_string(),
        roaming: "Disabled".to_string(),
        network_type: "4G LTE".to_string(),
        ..Default::default()
    };

    let mut default_data_slot: u32 = 1;
    let mut default_voice_slot: u32 = 1;

    if let Ok(out) = Command::new("dumpsys").arg("telephony.registry").output() {
        let dump = String::from_utf8_lossy(&out.stdout);
        let mut current_phone_id = 0;

        for line in dump.lines() {
            let trimmed = line.trim();

            // Stop before historical event records overwrite live active values
            if trimmed.starts_with("Historical logs:") || trimmed.starts_with("Local logs:") {
                break;
            }

            if trimmed.starts_with("Phone Id=0") {
                current_phone_id = 0;
                continue;
            } else if trimmed.starts_with("Phone Id=1") {
                current_phone_id = 1;
                continue;
            }

            if let Some("2") = extract_key_val(trimmed, "mActiveDataSubId=").as_deref() {
                default_data_slot = 2;
            } else if let Some("2") = extract_key_val(trimmed, "mDefaultDataSubId=").as_deref() {
                default_data_slot = 2;
            } else if let Some("2") = extract_key_val(trimmed, "mDefaultVoiceSubId=").as_deref() {
                default_voice_slot = 2;
            }

            if let Some(carrier_str) = extract_key_val(trimmed, "mOperatorAlphaLong=")
                .filter(|s| !s.is_empty() && s != "null")
            {
                match current_phone_id {
                    0 => sim1.carrier = carrier_str,
                    1 => sim2.carrier = carrier_str,
                    _ => {}
                }
            }

            if let Some(rplmn) = extract_key_val(trimmed, "rRplmn=")
                .filter(|s| !s.is_empty() && s != "null")
            {
                match current_phone_id {
                    0 => sim1.operator_code = rplmn,
                    1 => sim2.operator_code = rplmn,
                    _ => {}
                }
            }

            if trimmed.contains("overrideNetwork=NR_NSA")
                || trimmed.contains("overrideNetwork=NR_SA")
                || trimmed.contains("accessNetworkTechnology=NR")
            {
                match current_phone_id {
                    0 => sim1.network_type = "5G".to_string(),
                    1 => sim2.network_type = "5G".to_string(),
                    _ => {}
                }
            }

            if trimmed.contains("isRoaming=true") || trimmed.contains("roamingType=ROAMING") {
                match current_phone_id {
                    0 => sim1.roaming = "Enabled".to_string(),
                    1 => sim2.roaming = "Enabled".to_string(),
                    _ => {}
                }
            }
        }
    }

    let default_data = if default_data_slot == 2 && !sim2.carrier.is_empty() {
        format!("SIM 2 ({})", sim2.carrier)
    } else if !sim1.carrier.is_empty() {
        format!("SIM 1 ({})", sim1.carrier)
    } else {
        format!("SIM {}", default_data_slot)
    };

    let default_voice = if default_voice_slot == 2 && !sim2.carrier.is_empty() {
        format!("SIM 2 ({})", sim2.carrier)
    } else if !sim1.carrier.is_empty() {
        format!("SIM 1 ({})", sim1.carrier)
    } else {
        format!("SIM {}", default_voice_slot)
    };

    let default_sms = default_voice.clone();

    let connection_status = if !sim1.carrier.is_empty() || !sim2.carrier.is_empty() {
        "Connected".to_string()
    } else {
        "Disconnected".to_string()
    };

    TelephonyInfo {
        dual_sim: true,
        phone_type: "GSM".to_string(),
        esim: false,
        default_data,
        default_voice,
        default_sms,
        connection_status,
        sim1,
        sim2,
    }
}

fn extract_key_val(line: &str, key: &str) -> Option<String> {
    let idx = line.find(key)?;
    let rest = &line[idx + key.len()..];
    let end = rest.find([',', ' ', '}']).unwrap_or(rest.len());
    let val = rest[..end].trim();
    if val.is_empty() {
        None
    } else {
        Some(val.to_string())
    }
}
