use crate::util;
use std::time::Instant;
use xkm_native::platform::drm;

pub fn run() {
    println!("\n[drm_security]");
    let t0 = Instant::now();
    let info = drm::scan_drm_security_subsystem();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  widevine_available: {}", info.widevine_available);
    println!(
        "  widevine_security_level: {}",
        info.widevine_security_level
    );
    if let Some(s) = info.widevine_service {
        println!("  widevine_service: {}", s);
    }
    if let Some(wv) = info.widevine_cdm {
        println!("  widevine_cdm:");
        if let Some(v) = wv.vendor {
            println!("    vendor: {}", v);
        }
        if let Some(v) = wv.version {
            println!("    version: {}", v);
        }
        if let Some(v) = wv.security_level {
            println!("    security_level: {}", v);
        }
        if let Some(v) = wv.system_id {
            println!("    system_id: {}", v);
        }
        if let Some(v) = wv.max_hdcp_level {
            println!("    max_hdcp_level: {}", v);
        }
        if let Some(v) = wv.current_hdcp_level {
            println!("    current_hdcp_level: {}", v);
        }
        if let Some(v) = wv.max_sessions {
            println!("    max_sessions: {}", v);
        }
        if let Some(v) = wv.oem_crypto_api_version {
            println!("    oem_crypto_api_version: {}", v);
        }
    }
    println!("  clearkey_available: {}", info.clearkey_available);
    if let Some(s) = info.clearkey_service {
        println!("  clearkey_service: {}", s);
    }
    if let Some(ck) = info.clearkey_cdm {
        println!("  clearkey_cdm:");
        if let Some(v) = ck.vendor {
            println!("    vendor: {}", v);
        }
        if let Some(v) = ck.version {
            println!("    version: {}", v);
        }
        if let Some(v) = ck.max_hdcp_level {
            println!("    max_hdcp_level: {}", v);
        }
        if let Some(v) = ck.current_hdcp_level {
            println!("    current_hdcp_level: {}", v);
        }
    }
    println!("  wfd_hdcp_available: {}", info.wfd_hdcp_available);
    if let Some(tee) = info.tee_interface {
        println!("  tee_interface: {}", tee);
    }
    if let Some(qce) = info.crypto_engine {
        println!("  crypto_engine: {}", qce);
    }
    if let Some(km) = info.keymint_service {
        println!("  keymint_service: {}", km);
    }
    if let Some(conn) = info.display_connector {
        println!(
            "  display_connector: {} ({})",
            conn,
            info.display_status.unwrap_or_default()
        );
    }
    if !info.supported_refresh_rates.is_empty() {
        println!(
            "  supported_refresh_rates: {:?}",
            info.supported_refresh_rates
        );
    }
    if !info.hdr_types.is_empty() {
        println!("  supported_hdr_types: {:?}", info.hdr_types);
    }
    println!("  wide_color_gamut: {}", info.wide_color_gamut);
    if !info.display_modes.is_empty() {
        println!("  display_modes: {:?}", info.display_modes);
    }
}
