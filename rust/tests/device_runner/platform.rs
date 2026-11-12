use crate::util;
use std::time::Instant;
use xkm_native::platform::{cpu, vendor};

pub fn run() {
    println!("\n[platform]");
    let t0 = Instant::now();
    let soc = vendor::detect::detect_soc_vendor();
    let cpu_model = cpu::get_cpu_model();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  vendor: {}", soc);
    println!("  cpu_model: {}", cpu_model);
}
