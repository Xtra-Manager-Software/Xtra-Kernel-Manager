use crate::util;
use std::time::Instant;
use xkm_native::platform::gpu;

pub fn run() {
    println!("\n[gpu]");
    let t0 = Instant::now();
    let (gpu_vendor, gpu_model) = gpu::get_gpu_info();
    let gpu_freq = gpu::read_gpu_freq_mhz();
    let gpu_busy = gpu::read_gpu_busy_pct();
    let gpu_avail_freqs = gpu::get_available_frequencies();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  vendor: {}", gpu_vendor);
    println!("  model: {}", gpu_model);
    println!("  current_freq: {} MHz", gpu_freq);
    println!("  load: {:.1}%", gpu_busy);
    println!("  available_freqs: {:?}", gpu_avail_freqs);
}
