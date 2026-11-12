use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct WifiInfo {
    pub status: String,
    pub ssid: String,
    pub bssid: String,
    pub vendor: String,
    pub link_speed: String,
    pub signal_strength: String,
    pub frequency: String,
    pub channel: u32,
    pub standard: String,
    pub capabilities: String,
    pub ip_address: String,
    pub subnet_mask: String,
    pub gateway: String,
    pub dns1: String,
    pub dns2: String,
    pub dhcp_lease_duration: String,
    pub ipv6: Vec<String>,
    pub hardware_features: Vec<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct SimCardInfo {
    pub slot: u32,
    pub state: String,
    pub carrier: String,
    pub operator_code: String,
    pub country: String,
    pub roaming: String,
    pub network_type: String,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct TelephonyInfo {
    pub dual_sim: bool,
    pub phone_type: String,
    pub esim: bool,
    pub default_data: String,
    pub default_voice: String,
    pub default_sms: String,
    pub connection_status: String,
    pub sim1: SimCardInfo,
    pub sim2: SimCardInfo,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct NetworkSubsystemInfo {
    pub wifi: WifiInfo,
    pub telephony: TelephonyInfo,
}
