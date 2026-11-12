use crate::util;
use std::time::Instant;
use xkm_native::platform::memory;

pub fn run() {
    println!("\n[memory]");
    let t0 = Instant::now();
    let (mem, swap) = memory::read_memory_and_swap();
    let zram = memory::read_zram_stats();
    let swappiness = memory::get_swappiness();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  mem_total: {} MB", mem.total_kb / 1024);
    println!("  mem_available: {} MB", mem.available_kb / 1024);
    println!(
        "  mem_used: {} MB",
        (mem.total_kb - mem.available_kb).max(0) / 1024
    );
    println!("  swap_total: {} MB", swap.total_kb / 1024);
    println!("  swap_free: {} MB", swap.free_kb / 1024);
    println!("  swappiness: {}", swappiness);
    println!("  zram_disksize: {} MB", zram.disksize / (1024 * 1024));
    println!("  zram_used: {} MB", zram.mem_used_total / (1024 * 1024));
    println!("  zram_compression: {:.2}x", zram.compression_ratio);
}
