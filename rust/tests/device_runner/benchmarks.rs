use std::time::Instant;
use xkm_native::platform::{cpu, fps, memory, thermal};
use xkm_native::telemetry::collect_snapshot;
use crate::util;

pub fn run() {
    println!("\n[telemetry_sample]");
    let t0 = Instant::now();
    let snapshot = collect_snapshot();
    let fetch_time = t0.elapsed();
    let json = serde_json::to_string(&snapshot).unwrap_or_default();
    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  json_bytes: {}", json.len());
    println!("  cpu_load: {:.2}%", snapshot.cpu.load_pct);
    println!("  cpu_temp: {:.1} °C", snapshot.thermal.cpu_temp_c);

    println!("\n[benchmark_snapshot]");
    const ITERS: usize = 1000;
    let mut latencies_us = Vec::with_capacity(ITERS);

    let bench_start = Instant::now();
    for _ in 0..ITERS {
        let start = Instant::now();
        let _ = collect_snapshot();
        latencies_us.push(start.elapsed().as_micros() as u64);
    }
    let total_elapsed = bench_start.elapsed();
    latencies_us.sort_unstable();

    let min_us = latencies_us[0];
    let max_us = latencies_us[ITERS - 1];
    let sum_us: u64 = latencies_us.iter().sum();
    let avg_us = sum_us as f64 / ITERS as f64;
    let median_us = latencies_us[ITERS / 2];
    let p95_us = latencies_us[(ITERS as f64 * 0.95) as usize];
    let p99_us = latencies_us[(ITERS as f64 * 0.99) as usize];
    let throughput = (ITERS as f64) / total_elapsed.as_secs_f64();

    println!("  iterations: {}", ITERS);
    println!("  total_time: {:?}", total_elapsed);
    println!("  throughput: {:.1} snapshots/sec", throughput);
    println!("  min: {} µs ({:.3} ms)", min_us, min_us as f64 / 1000.0);
    println!("  avg: {:.1} µs ({:.3} ms)", avg_us, avg_us / 1000.0);
    println!("  p50: {} µs ({:.3} ms)", median_us, median_us as f64 / 1000.0);
    println!("  p95: {} µs ({:.3} ms)", p95_us, p95_us as f64 / 1000.0);
    println!("  p99: {} µs ({:.3} ms)", p99_us, p99_us as f64 / 1000.0);
    println!("  max: {} µs ({:.3} ms)", max_us, max_us as f64 / 1000.0);

    println!("\n[micro_benchmarks]");
    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = cpu::read_cpu_load();
    }
    println!(
        "  read_cpu_load: {:.1} µs/call",
        t0.elapsed().as_micros() as f64 / ITERS as f64
    );

    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = memory::read_memory_and_swap();
    }
    println!(
        "  read_memory_and_swap: {:.1} µs/call",
        t0.elapsed().as_micros() as f64 / ITERS as f64
    );

    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = thermal::read_primary_thermal_summary();
    }
    println!(
        "  primary_thermal_summary: {:.1} µs/call",
        t0.elapsed().as_micros() as f64 / ITERS as f64
    );

    let sample = collect_snapshot();
    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = serde_json::to_string(&sample);
    }
    println!(
        "  json_serialize: {:.1} µs/call",
        t0.elapsed().as_micros() as f64 / ITERS as f64
    );

    let t0 = Instant::now();
    for _ in 0..10_000 {
        let _ = fps::parse_sysfs_fps("60.0");
    }
    println!(
        "  parse_sysfs_fps (60.0): {:.1} ns/call",
        t0.elapsed().as_nanos() as f64 / 10_000.0
    );

    let sde_sample = "fps: 119.8 duration:1000000 frame_count:120 vsync_rate: 120";
    let t0 = Instant::now();
    for _ in 0..10_000 {
        let _ = fps::parse_sysfs_fps(sde_sample);
    }
    println!(
        "  parse_sysfs_fps (sde): {:.1} ns/call",
        t0.elapsed().as_nanos() as f64 / 10_000.0
    );

    let garbage = "unknown vendor parameter metric value counter ".repeat(50);
    let t0 = Instant::now();
    for _ in 0..1_000 {
        let _ = fps::parse_sysfs_fps(&garbage);
    }
    println!(
        "  parse_sysfs_fps (2kb_garbage): {:.1} ns/call",
        t0.elapsed().as_nanos() as f64 / 1_000.0
    );

    let t0 = Instant::now();
    for _ in 0..1_000 {
        let _ = fps::read_fps();
    }
    println!(
        "  read_fps_pipeline: {:.1} µs/call",
        t0.elapsed().as_micros() as f64 / 1_000.0
    );
}
