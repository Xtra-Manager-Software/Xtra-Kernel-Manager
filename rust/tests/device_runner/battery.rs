use crate::util;
use std::time::Instant;
use xkm_native::platform::battery;

pub fn run() {
    println!("\n[battery]");
    let t0 = Instant::now();
    let bat = battery::read_battery_snapshot();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  capacity: {}%", bat.level_pct);
    println!("  temp: {:.1} °C", bat.temp_c);
    println!("  current: {} mA", bat.current_ma);
    println!("  voltage: {:.3} V", bat.voltage_v);
    println!("  status: {}", bat.status);
    println!("  health: {}", bat.health);
}
