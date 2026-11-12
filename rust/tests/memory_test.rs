use xkm_native::platform::memory::meminfo::read_memory_and_swap;
use xkm_native::platform::memory::zram::{
    get_available_zram_algorithms, get_swappiness, read_zram_stats,
};

#[test]
fn test_read_memory_and_swap() {
    let (mem, swap) = read_memory_and_swap();
    assert!(mem.total_kb > 0, "Total memory should be > 0");
    assert!(mem.available_kb > 0, "Available memory should be > 0");
    assert!(
        mem.available_kb <= mem.total_kb,
        "Available should not exceed total"
    );
    println!(
        "RAM: Total={} MB, Avail={} MB, Free={} MB",
        mem.total_kb / 1024,
        mem.available_kb / 1024,
        mem.free_kb / 1024
    );
    println!(
        "Swap: Total={} MB, Free={} MB, Used={} MB",
        swap.total_kb / 1024,
        swap.free_kb / 1024,
        swap.used_kb / 1024
    );
}

#[test]
fn test_read_zram_stats() {
    let zram = read_zram_stats();
    assert!(zram.disksize >= 0);
    assert!(zram.compression_ratio >= 0.0);
    println!(
        "ZRAM: disksize={} bytes, orig={} bytes, compr={} bytes, ratio={:.2}",
        zram.disksize, zram.orig_data_size, zram.compr_data_size, zram.compression_ratio
    );
}

#[test]
fn test_get_swappiness() {
    let swappiness = get_swappiness();
    assert!(
        (0..=200).contains(&swappiness),
        "Swappiness should be in valid Linux range (0..200), got {}",
        swappiness
    );
}

#[test]
fn test_available_zram_algorithms() {
    let algos = get_available_zram_algorithms();
    assert!(!algos.is_empty(), "ZRAM algorithms should not be empty");
}
