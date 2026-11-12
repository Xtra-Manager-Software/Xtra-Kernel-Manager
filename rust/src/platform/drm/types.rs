use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct CdmInfo {
    pub name: String,
    pub vendor: Option<String>,
    pub version: Option<String>,
    pub description: Option<String>,
    pub security_level: Option<String>,
    pub system_id: Option<String>,
    pub max_hdcp_level: Option<String>,
    pub current_hdcp_level: Option<String>,
    pub max_sessions: Option<String>,
    pub oem_crypto_api_version: Option<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct DrmSecurityInfo {
    pub widevine_available: bool,
    pub widevine_security_level: String,
    pub widevine_service: Option<String>,
    pub widevine_cdm: Option<CdmInfo>,
    pub clearkey_available: bool,
    pub clearkey_service: Option<String>,
    pub clearkey_cdm: Option<CdmInfo>,
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
