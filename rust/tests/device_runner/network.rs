use crate::util;
use std::time::Instant;
use xkm_native::platform::network;

pub fn run() {
    println!("\n[network]");
    let t0 = Instant::now();
    let net = network::scan_network_subsystem();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  wifi_status: {}", net.wifi.status);
    println!("  ssid: {}", net.wifi.ssid);
    println!("  bssid: {}", net.wifi.bssid);
    println!("  standard: {}", net.wifi.standard);
    println!("  link_speed: {}", net.wifi.link_speed);
    println!("  signal_strength: {}", net.wifi.signal_strength);
    println!("  frequency: {}", net.wifi.frequency);
    println!("  ip_address: {}", net.wifi.ip_address);
    println!("  telephony_dual_sim: {}", net.telephony.dual_sim);
    println!(
        "  sim1: {} ({}) [{}]",
        net.telephony.sim1.carrier,
        net.telephony.sim1.operator_code,
        net.telephony.sim1.network_type
    );
    println!(
        "  sim2: {} ({}) [{}]",
        net.telephony.sim2.carrier,
        net.telephony.sim2.operator_code,
        net.telephony.sim2.network_type
    );
}
