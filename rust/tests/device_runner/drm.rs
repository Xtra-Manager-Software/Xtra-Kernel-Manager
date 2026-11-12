use std::time::Instant;
use xkm_native::platform::drm;
use crate::util;

pub fn run() {
    println!("\n[drm_security]");
    let t0 = Instant::now();
    let info = drm::scan_drm_security_subsystem();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  widevine_available: {}", info.widevine_available);
    println!("  widevine_security_level: {}", info.widevine_security_level);
    if let Some(s) = info.widevine_service {
        println!("  widevine_service: {}", s);
    }
    println!("  clearkey_available: {}", info.clearkey_available);
    if let Some(s) = info.clearkey_service {
        println!("  clearkey_service: {}", s);
    }
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
        println!("  display_connector: {} ({:?})", conn, info.display_status.unwrap_or_default());
    }
}
