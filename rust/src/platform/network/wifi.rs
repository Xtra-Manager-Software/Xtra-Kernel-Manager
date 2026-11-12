use std::process::Command;
use super::types::WifiInfo;

pub fn scan_wifi() -> WifiInfo {
    let mut hw_features = Vec::new();
    if let Ok(out) = Command::new("pm").args(["list", "features"]).output() {
        let text = String::from_utf8_lossy(&out.stdout);
        for line in text.lines() {
            let t = line.trim();
            match t {
                "feature:android.hardware.wifi" => {
                    hw_features.push("802.11 b/a/g/n/ac/ax".to_string())
                }
                "feature:android.hardware.wifi.direct" => {
                    hw_features.push("Wi-Fi Direct support".to_string())
                }
                "feature:android.hardware.wifi.aware" => {
                    hw_features.push("Wi-Fi Aware support".to_string())
                }
                "feature:android.hardware.wifi.passpoint" => {
                    hw_features.push("Wi-Fi Passpoint support".to_string())
                }
                "feature:android.hardware.wifi.rtt" => {
                    hw_features.push("Wi-Fi RTT support".to_string())
                }
                _ => {}
            }
        }
    }
    if hw_features.is_empty() {
        hw_features.push("802.11 Wi-Fi".to_string());
    }

    let mut info = WifiInfo {
        status: "Disconnected".to_string(),
        hardware_features: hw_features,
        ..Default::default()
    };

    // 1. Query live network link properties from dumpsys connectivity
    if let Ok(out) = Command::new("dumpsys").arg("connectivity").output() {
        let dump = String::from_utf8_lossy(&out.stdout);
        for line in dump.lines() {
            let trimmed = line.trim();
            if trimmed.contains("InterfaceName: wlan0") {
                info.status = "Connected".to_string();

                // Parse LinkAddresses: [ 192.168.1.6/24, fe80::..., ... ]
                if let Some(pos) = trimmed.find("LinkAddresses: [") {
                    let rest = &trimmed[pos + 16..];
                    if let Some(end) = rest.find(']') {
                        for addr in rest[..end].split(',') {
                            let a = addr.trim();
                            if a.contains(':') {
                                let ipv6_clean = match a.split_once('/') {
                                    Some((ip, _)) => ip.to_string(),
                                    None => a.to_string(),
                                };
                                if !info.ipv6.contains(&ipv6_clean) {
                                    info.ipv6.push(ipv6_clean);
                                }
                            } else if let Some((ip, prefix)) = a.split_once('/') {
                                info.ip_address = ip.to_string();
                                info.subnet_mask = match prefix {
                                    "24" => "255.255.255.0".to_string(),
                                    "16" => "255.255.0.0".to_string(),
                                    "8" => "255.0.0.0".to_string(),
                                    _ => format!("/{}", prefix),
                                };
                            } else {
                                info.ip_address = a.to_string();
                            }
                        }
                    }
                }

                // Parse DnsAddresses: [ /2404:c0:ab00::9,/182.8.32.5,/182.8.32.6 ]
                if let Some(pos) = trimmed.find("DnsAddresses: [") {
                    let rest = &trimmed[pos + 15..];
                    if let Some(end) = rest.find(']') {
                        let mut ipv4_dns = Vec::new();
                        for addr in rest[..end].split(',') {
                            let clean = addr.trim().trim_start_matches('/');
                            if !clean.contains(':') {
                                ipv4_dns.push(clean.to_string());
                            }
                        }
                        match ipv4_dns.as_slice() {
                            [d1, d2, ..] => {
                                info.dns1 = d1.clone();
                                info.dns2 = d2.clone();
                            }
                            [d1] => info.dns1 = d1.clone(),
                            _ => {}
                        }
                    }
                }

                // Parse ServerAddress (Gateway): e.g. ServerAddress: /192.168.1.1
                if let Some(pos) = trimmed.find("ServerAddress: /") {
                    let rest = &trimmed[pos + 16..];
                    let gw = rest.split_whitespace().next().unwrap_or_default();
                    info.gateway = gw.to_string();
                }
            }
        }
    }

    // 2. Query cmd wifi status for active Wi-Fi radio parameters
    if let Ok(out) = Command::new("cmd").args(["wifi", "status"]).output() {
        let text = String::from_utf8_lossy(&out.stdout);
        for line in text.lines() {
            let trimmed = line.trim();
            if let Some(stripped) = trimmed.strip_prefix("WifiInfo:") {
                for part in stripped.split(',') {
                    let p = part.trim();
                    if let Some((k, v)) = p.split_once(':') {
                        let key = k.trim();
                        let val = v.trim().trim_matches('"');
                        match key {
                            "SSID" if !val.is_empty() => info.ssid = val.to_string(),
                            "BSSID" if !val.is_empty() => info.bssid = val.to_uppercase(),
                            "Link speed" if !val.is_empty() => info.link_speed = val.to_string(),
                            "RSSI" if !val.is_empty() => {
                                info.signal_strength = format!("{} dBm", val)
                            }
                            "Frequency" if !val.is_empty() => {
                                info.frequency = val.to_string();
                                if let Ok(freq_num) =
                                    val.trim_end_matches("MHz").trim().parse::<u32>()
                                {
                                    info.channel = match freq_num {
                                        2412..=2484 => (freq_num - 2407) / 5,
                                        5170..=5825 => (freq_num - 5000) / 5,
                                        5925..=7125 => (freq_num - 5940) / 5,
                                        _ => 0,
                                    };
                                }
                            }
                            "Wi-Fi standard" => {
                                info.standard = if val.contains("11ax") {
                                    "Wi-Fi 6 (802.11ax)".to_string()
                                } else {
                                    val.to_string()
                                };
                            }
                            _ => {}
                        }
                    }
                }
            }
        }
    }

    info
}
