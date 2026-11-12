use std::time::Instant;
use xkm_native::platform::thermal;
use crate::util;

pub fn run() {
    println!("\n[thermal]");
    let t0 = Instant::now();
    let thermal_summary = thermal::scan_thermal_zones();
    let sconfig = thermal::read_sconfig_index();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!(
        "  cpu_temp: {:.1} °C",
        thermal_summary.cpu_temp_c.unwrap_or(0.0)
    );
    println!(
        "  gpu_temp: {:.1} °C",
        thermal_summary.gpu_temp_c.unwrap_or(0.0)
    );
    println!("  active_zones: {}", thermal_summary.zones.len());
    for z in thermal_summary.zones.iter().take(10) {
        println!(
            "    zone {:<2} ({:<10} {:<16}): {:.1} °C",
            z.id, z.category, z.name, z.temp
        );
    }
    if let Some(sc) = sconfig {
        println!("  xiaomi_sconfig: index {}", sc);
    }
}
