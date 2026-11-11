use std::time::Instant;
use xkm_native::platform::{battery, binder, cpu, fps, gpu, memory, thermal, vendor};
use xkm_native::telemetry::collect_snapshot;

fn main() {
    println!("   XTRA KERNEL MANAGER - NATIVE   ");

    // 1. System & SoC Identification
    let soc = vendor::detect::detect_soc_vendor();
    let cpu_model = cpu::get_cpu_model();
    println!("\n[1] PLATFORM IDENTIFICATION");
    println!("  * SoC Vendor     : {}", soc);
    println!("  * CPU Model      : {}", cpu_model);

    // 2. CPU Topology & Frequencies
    println!("\n[2] CPU TOPOLOGY & FREQUENCIES");
    let clusters = cpu::detect_cpu_clusters();
    let online_cores = cpu::parse_online_cores();
    println!("  * Online Cores   : {:?}", online_cores);
    println!("  * Total Clusters : {}", clusters.len());
    for cluster in &clusters {
        println!(
            "    - Cluster {} (Policy {}): ({} cores: {:?}) [{} MHz - {} MHz] Gov: {}",
            cluster.cluster_number,
            cluster.policy_path,
            cluster.cores.len(),
            cluster.cores,
            cluster.min_freq / 1000,
            cluster.max_freq / 1000,
            cluster.governor
        );
    }
    let av_govs = cpu::get_available_governors();
    println!("  * Available Govs : {:?}", av_govs);

    let cores_data = cpu::read_core_data();
    print!("  * Live Core Freqs: ");
    for c in &cores_data {
        print!("C{}: {}MHz ", c.core_number, c.current_freq / 1000);
    }
    println!();

    let load = cpu::read_cpu_load();
    println!("  * Current CPU Load: {:.2}%", load);

    // 3. GPU Subsystem
    println!("\n[3] GPU SUBSYSTEM");
    let (gpu_vendor, gpu_model) = gpu::get_gpu_info();
    let gpu_freq = gpu::read_gpu_freq_mhz();
    let gpu_busy = gpu::read_gpu_busy_pct();
    let gpu_avail_freqs = gpu::get_available_frequencies();
    println!("  * GPU Vendor     : {}", gpu_vendor);
    println!("  * GPU Model      : {}", gpu_model);
    println!("  * Current Freq   : {} MHz", gpu_freq);
    println!("  * GPU Load       : {:.1}%", gpu_busy);
    println!("  * Available Freqs: {:?}", gpu_avail_freqs);

    // 4. Thermal Zones
    println!("\n[4] THERMAL MONITORING");
    let thermal_summary = thermal::scan_thermal_zones();
    println!(
        "  * CPU Temp       : {:.1} \u{00b0}C",
        thermal_summary.cpu_temp_c.unwrap_or(0.0)
    );
    println!(
        "  * GPU Temp       : {:.1} \u{00b0}C",
        thermal_summary.gpu_temp_c.unwrap_or(0.0)
    );
    println!("  * Total Active Zones: {}", thermal_summary.zones.len());
    for z in thermal_summary.zones.iter().take(10) {
        println!(
            "    - {:<10} (zone {:<2} - {:<16}): {:.1} \u{00b0}C",
            z.category, z.id, z.name, z.temp
        );
    }
    if let Some(sconfig) = thermal::read_sconfig_index() {
        println!("  * Xiaomi sconfig : index {}", sconfig);
    }

    // 5. Memory & ZRAM
    println!("\n[5] MEMORY & SWAP (ZRAM)");
    let (mem, swap) = memory::read_memory_and_swap();
    let zram = memory::read_zram_stats();
    let swappiness = memory::get_swappiness();
    println!("  * MemTotal       : {} MB", mem.total_kb / 1024);
    println!("  * MemAvailable   : {} MB", mem.available_kb / 1024);
    println!(
        "  * MemUsed        : {} MB",
        (mem.total_kb - mem.available_kb).max(0) / 1024
    );
    println!("  * SwapTotal      : {} MB", swap.total_kb / 1024);
    println!("  * SwapFree       : {} MB", swap.free_kb / 1024);
    println!("  * Swappiness     : {}", swappiness);
    println!("  * ZRAM Disksize  : {} MB", zram.disksize / (1024 * 1024));
    println!(
        "  * ZRAM Used      : {} MB",
        zram.mem_used_total / (1024 * 1024)
    );
    println!("  * Compression    : {:.2}x", zram.compression_ratio);

    // 6. Battery Subsystem
    println!("\n[6] BATTERY METRICS");
    let bat = battery::read_battery_snapshot();
    println!("  * Capacity       : {}%", bat.level_pct);
    println!("  * Temperature    : {:.1} \u{00b0}C", bat.temp_c);
    println!("  * Current        : {} mA", bat.current_ma);
    println!("  * Voltage        : {:.3} V", bat.voltage_v);
    println!("  * Status         : {}", bat.status);
    println!("  * Health         : {}", bat.health);

    // 7. Foreground App Detection (Zero-Permission CGroup)
    println!("\n[7] FOREGROUND APP DETECTION (ZERO-PERMISSION)");
    let fg_app = binder::get_foreground_app();
    println!("  * Top-App Target : {:?}", fg_app);

    // 8. FPS Meter Lifecycle (libgui.so eBPF on-demand & safe detach like Auriya)
    println!("\n[8] FPS METER LIFECYCLE (AURIYA-STYLE TARGET PROBING & SAFE DETACH)");
    println!(
        "  * Detected Hardware Sysfs Node: {:?}",
        fps::SysfsFpsReader::detect_sysfs()
    );

    // Phase A: No target set -> zero eBPF probe injected, pure passive sysfs
    println!("  --- Phase A: Passive Baseline (No Target App Set) ---");
    let init_attached = fps::is_ebpf_attached();
    let init_fps = fps::read_fps();
    println!(
        "  * eBPF Probe Attached : {} (Expected: false)",
        init_attached
    );
    println!("  * Baseline FPS Reading: {:?}", init_fps);

    // Phase B: Attach only to specific target app
    if let Some(ref fg) = fg_app {
        println!(
            "  --- Phase B: Target App Monitoring ({}) ---",
            fg.package_name
        );
        fps::set_target_package(Some(fg.package_name.clone()));
        // First read triggers dynamic attach to matching target
        let _ = fps::read_fps();
        std::thread::sleep(std::time::Duration::from_millis(100));
        println!(
            "  * eBPF Attached to Target: {} (PID: {:?})",
            fps::is_ebpf_attached(),
            fps::get_attached_pid()
        );
        println!("  * Sampling 5 frames with target active (Jank, 1% Low & Cause telemetry)...");
        for i in 1..=5 {
            std::thread::sleep(std::time::Duration::from_millis(150));
            match fps::read_fps() {
                Some(r) => println!(
                    "    [Sample {:>2}] FPS: {:>5.1} | FT: {:>5.2}ms | Avg: {:>5.1} | 1% Low: {:>5.1} | Jank: {} ({:>4.1}%) | Cause: {} ({:?})",
                    i,
                    r.fps,
                    r.frametime_ms,
                    r.avg_fps,
                    r.low_1pct_fps,
                    r.jank_count,
                    r.jank_percent,
                    r.drop_cause.as_str(),
                    r.source
                ),
                None => println!("    [Sample {:>2}] FPS: None", i),
            }
        }
    }

    // Phase C: Safe Detach (Probe dicabut dari process & eBPF di-unload)
    println!("  --- Phase C: Safe Detach / Unload Probe ---");
    let detached_ok = fps::detach_fps_pid();
    std::thread::sleep(std::time::Duration::from_millis(50));
    println!("  * detach_fps_pid() returned   : {}", detached_ok);
    println!(
        "  * eBPF Probe Attached After Detach: {} (Expected: false)",
        fps::is_ebpf_attached()
    );
    let fallback_fps = fps::read_fps();
    println!("  * Immediate Fallback FPS Reading  : {:?}", fallback_fps);

    // Phase D: Out-of-Target App Protection (Probe is NEVER attached to untargeted apps)
    println!("  --- Phase D: Out-of-Target App Isolation ---");
    fps::set_target_package(Some("com.fake.nonexistent.game".to_string()));
    let _ = fps::read_fps();
    println!(
        "  * Probing untargeted foreground app : is_attached = {} (Expected: false)",
        fps::is_ebpf_attached()
    );
    fps::clear_fps();
    println!("  * Full cleanup done via clear_fps()");

    // 9. Bind-Mount Frequency Ceiling Lock Test
    println!("\n[9] MOUNT CEILING FREQUENCY LOCK TEST");
    if let Some(first_cluster) = clusters.first() {
        let cluster_num = first_cluster.cluster_number;
        let test_lock_freq = first_cluster.min_freq as u64;
        println!(
            "  * Testing bind-mount lock on Cluster {} at {} KHz...",
            cluster_num, test_lock_freq
        );
        match cpu::lock_cluster_frequency(cluster_num, test_lock_freq) {
            Ok(_) => {
                println!("    -> Lock SUCCESS: bind-mount applied over scaling_max_freq");
                let mounts = std::fs::read_to_string("/proc/mounts").unwrap_or_default();
                let has_mount = mounts.contains(&format!("policy{}/scaling_max_freq", cluster_num));
                let status_str = match has_mount {
                    true => "VERIFIED",
                    false => "NOT FOUND",
                };
                println!("    -> Verification in /proc/mounts: {}", status_str);
                match cpu::unlock_cluster_frequency(cluster_num) {
                    Ok(_) => println!("    -> Unlock SUCCESS: bind-mount detached cleanly"),
                    Err(e) => println!("    -> Unlock FAILED: {}", e),
                }
            }
            Err(e) => println!("    -> Lock failed: {}", e),
        }
    }

    // 10. Telemetry Snapshot Sample
    println!("\n[10] FULL 1-PASS TELEMETRY SNAPSHOT SAMPLE");
    let snapshot = collect_snapshot();
    let json = serde_json::to_string_pretty(&snapshot).unwrap_or_default();
    println!("  Snapshot JSON length: {} bytes", json.len());

    // 11. Real Hardware Performance Benchmark (1000 iterations)
    println!("\n[11] REAL HARDWARE PERFORMANCE BENCHMARK (1,000 ITERATIONS)");
    println!("  Benchmarking collect_snapshot() on real Snapdragon 7+ Gen 2...");

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

    println!("  Total iterations : {}", ITERS);
    println!("  Total time       : {:?}", total_elapsed);
    println!("  Throughput       : {:.1} snapshots/sec", throughput);
    println!(
        "  Min latency      : {} \u{00b5}s ({:.3} ms)",
        min_us,
        min_us as f64 / 1000.0
    );
    println!(
        "  Avg latency      : {:.1} \u{00b5}s ({:.3} ms)",
        avg_us,
        avg_us / 1000.0
    );
    println!(
        "  Median (p50)     : {} \u{00b5}s ({:.3} ms)",
        median_us,
        median_us as f64 / 1000.0
    );
    println!(
        "  95th percentile  : {} \u{00b5}s ({:.3} ms)",
        p95_us,
        p95_us as f64 / 1000.0
    );
    println!(
        "  99th percentile  : {} \u{00b5}s ({:.3} ms)",
        p99_us,
        p99_us as f64 / 1000.0
    );
    println!(
        "  Max latency      : {} \u{00b5}s ({:.3} ms)",
        max_us,
        max_us as f64 / 1000.0
    );

    println!("\n[12] SUB-SYSTEM MICRO-BENCHMARKS (1,000 ITERATIONS)");
    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = cpu::read_cpu_load();
    }
    let cpu_load_avg = t0.elapsed().as_micros() as f64 / ITERS as f64;
    println!(
        "  * read_cpu_load()       : {:.1} \u{00b5}s / call",
        cpu_load_avg
    );

    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = memory::read_memory_and_swap();
    }
    let mem_avg = t0.elapsed().as_micros() as f64 / ITERS as f64;
    println!(
        "  * read_memory_and_swap(): {:.1} \u{00b5}s / call",
        mem_avg
    );

    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = thermal::read_primary_thermal_summary();
    }
    let prim_thermal_avg = t0.elapsed().as_micros() as f64 / ITERS as f64;
    println!(
        "  * primary_thermal_summary: {:.1} \u{00b5}s / call",
        prim_thermal_avg
    );

    let sample = collect_snapshot();
    let t0 = Instant::now();
    for _ in 0..ITERS {
        let _ = serde_json::to_string(&sample);
    }
    let json_avg = t0.elapsed().as_micros() as f64 / ITERS as f64;
    println!(
        "  * serde_json serialize  : {:.1} \u{00b5}s / call",
        json_avg
    );

    // FPS Micro-benchmarks (Best-case, Vendor, and Worst-case)
    let t0 = Instant::now();
    for _ in 0..10_000 {
        let _ = fps::parse_sysfs_fps("60.0");
    }
    let fps_best_ns = (t0.elapsed().as_nanos() as f64) / 10_000.0;
    println!(
        "  * parse_sysfs_fps (Best: '60.0')     : {:.1} ns / call",
        fps_best_ns
    );

    let sde_sample = "fps: 119.8 duration:1000000 frame_count:120 vsync_rate: 120";
    let t0 = Instant::now();
    for _ in 0..10_000 {
        let _ = fps::parse_sysfs_fps(sde_sample);
    }
    let fps_vendor_ns = (t0.elapsed().as_nanos() as f64) / 10_000.0;
    println!(
        "  * parse_sysfs_fps (Vendor SDE CRTC)   : {:.1} ns / call",
        fps_vendor_ns
    );

    let garbage = "unknown vendor parameter metric value counter ".repeat(50);
    let t0 = Instant::now();
    for _ in 0..1_000 {
        let _ = fps::parse_sysfs_fps(&garbage);
    }
    let fps_worst_ns = (t0.elapsed().as_nanos() as f64) / 1_000.0;
    println!(
        "  * parse_sysfs_fps (Worst: 2KB garbage): {:.1} ns / call",
        fps_worst_ns
    );

    let t0 = Instant::now();
    for _ in 0..1_000 {
        let _ = fps::read_fps();
    }
    let fps_meter_avg = t0.elapsed().as_micros() as f64 / 1_000.0;
    println!(
        "  * fps::read_fps() (End-to-end pipe)  : {:.1} \u{00b5}s / call",
        fps_meter_avg
    );

    // [13] Native Binder Actuators
    println!("\n[13] NATIVE BINDER ACTUATORS ");
    let binder_init = binder::ensure_initialized();
    println!("  * Native Binder Init        : {}", binder_init);
    println!(
        "  * Active Display Refresh Hz : {:.1} Hz",
        binder::get_active_display_refresh_rate()
    );
    println!(
        "  * Supported Refresh Rates   : {:?}",
        binder::query_binder_supported_rates()
    );
    println!(
        "  * Current Screen Brightness : {:?}",
        binder::get_screen_brightness()
    );
    println!(
        "  * Android Battery Saver     : {:?}",
        binder::is_power_save_mode().unwrap_or(false)
    );
    println!(
        "  * PowerHAL Game Mode Supp   : {}",
        binder::is_power_mode_supported(binder::PowerMode::Game)
    );
    println!(
        "  * PowerHAL Sustained Perf   : {}",
        binder::is_power_mode_supported(binder::PowerMode::SustainedPerformance)
    );
    println!(
        "  * PowerHAL Expensive Render : {}",
        binder::is_power_mode_supported(binder::PowerMode::ExpensiveRendering)
    );
    println!(
        "  * Wi-Fi Low-Latency Active  : {}",
        binder::is_wifi_low_latency_active()
    );
    println!(
        "  * Status Bar Gesture Locked : {}",
        binder::is_statusbar_locked()
    );
    println!(
        "  * Screen Rotation Frozen    : {}",
        binder::is_display_rotation_frozen(0)
    );

    // [14] Native Binder App Monitor, Touch, Thermal & Charging
    println!("\n[14] NATIVE BINDER APP MONITOR, TOUCH, THERMAL & CHARGING");
    let proc_list = binder::get_running_process_list().unwrap_or_default();
    println!("  * Total Running Processes   : {}", proc_list.len());
    println!("  * Top 5 Memory Consumers    :");
    for p in proc_list.iter().take(5) {
        println!(
            "    - PID {:<5} [{}] RSS: {:>6} MB | State: {} | Cgroup: {}",
            p.pid,
            p.package_name,
            p.rss_kb / 1024,
            p.state,
            p.cgroup
        );
    }

    let mlbb_bucket = binder::get_app_standby_bucket("com.mobile.legends", None);
    println!("  * MLBB Standby Bucket       : {:?}", mlbb_bucket);

    let thermal_status = binder::get_thermal_status();
    println!("  * IThermalService Status    : {:?}", thermal_status);
    let thermal_headroom = binder::get_thermal_headroom(0);
    println!("  * Thermal Headroom (0s)     : {:?}", thermal_headroom);

    let touch_supported = binder::is_high_touch_polling_rate_supported();
    let touch_enabled = binder::get_high_touch_polling_rate_enabled();
    println!("  * High Touch Polling Supp   : {}", touch_supported);
    println!("  * High Touch Polling State  : {:?}", touch_enabled);
    let touch_toggle_on = binder::set_high_touch_polling_rate_enabled(true);
    println!("  * High Touch Rate (Set ON)  : {:?}", touch_toggle_on);
    let touch_state_on = binder::get_high_touch_polling_rate_enabled();
    println!("  * High Touch State After ON : {:?}", touch_state_on);
    let _ = binder::set_high_touch_polling_rate_enabled(false);
    let touch_state_off = binder::get_high_touch_polling_rate_enabled();
    println!("  * High Touch (Restore OFF)  : {:?}", touch_state_off);

    let charging_supported = binder::is_charging_control_supported();
    let charging_enabled = binder::get_charging_enabled();
    println!("  * Charging Control Supp     : {}", charging_supported);
    println!("  * Charging Enabled (AC)     : {:?}", charging_enabled);

    println!("           ON-DEVICE VERIFICATION COMPLETE              ");
}
