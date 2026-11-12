pub mod daemons;
pub mod display;
pub mod mediadrm;
pub mod types;

pub use daemons::scan_drm_daemons_and_tee;
pub use display::scan_drm_display_subsystem;
pub use mediadrm::query_mediadrm_cdms;
pub use types::{CdmInfo, DrmSecurityInfo};

use std::path::Path;

/// Scan for active DRM, Widevine CDM, ClearKey, TEE TrustZone, and hardware crypto accelerators.
pub fn scan_drm_security_subsystem() -> DrmSecurityInfo {
    let daemons_info = scan_drm_daemons_and_tee();
    let display_info = scan_drm_display_subsystem();

    // Query Widevine and ClearKey CDM properties live using Android NDK AMediaDrm API
    let (widevine_cdm, clearkey_cdm) = query_mediadrm_cdms();

    let widevine_available = widevine_cdm.is_some() || daemons_info.widevine_service.is_some();
    let clearkey_available = clearkey_cdm.is_some() || daemons_info.clearkey_service.is_some();

    // Check Wi-Fi Display (Miracast) HDCP 2.x
    let wfd_hdcp_available = Path::new("/vendor/lib64/libwfdnative.so").exists()
        || Path::new("/system/lib64/libwfdnative.so").exists();

    // Determine security level
    let widevine_security_level = match &widevine_cdm {
        Some(cdm) => match cdm.security_level.as_deref() {
            Some(lvl) => format!("{} (Hardware Protected)", lvl),
            None => match daemons_info.tee_interface.is_some() || daemons_info.keymint_service.is_some() {
                true => "L1 (Hardware Backed Secure Enclave / TrustZone)".to_string(),
                false => "L3 (Software CDM Fallback)".to_string(),
            },
        },
        None => match (
            widevine_available,
            daemons_info.tee_interface.is_some() || daemons_info.keymint_service.is_some(),
        ) {
            (true, true) => "L1 (Hardware Backed Secure Enclave / TrustZone)".to_string(),
            (true, false) => "L3 (Software CDM Fallback)".to_string(),
            (false, _) => "Unavailable".to_string(),
        },
    };

    DrmSecurityInfo {
        widevine_available,
        widevine_security_level,
        widevine_service: daemons_info.widevine_service,
        widevine_cdm,
        clearkey_available,
        clearkey_service: daemons_info.clearkey_service,
        clearkey_cdm,
        wfd_hdcp_available,
        tee_interface: daemons_info.tee_interface,
        crypto_engine: daemons_info.crypto_engine,
        keymint_service: daemons_info.keymint_service,
        display_connector: display_info.display_connector,
        display_status: display_info.display_status,
        display_modes: display_info.display_modes,
        supported_refresh_rates: display_info.supported_refresh_rates,
        hdr_types: display_info.hdr_types,
        wide_color_gamut: display_info.wide_color_gamut,
    }
}
