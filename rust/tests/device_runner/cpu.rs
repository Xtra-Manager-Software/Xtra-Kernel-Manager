use crate::util;
use std::time::Instant;
use xkm_native::platform::cpu;

pub fn run() {
    println!("\n[cpu]");
    let t0 = Instant::now();
    let clusters = cpu::detect_cpu_clusters();
    let online_cores = cpu::parse_online_cores();
    let av_govs = cpu::get_available_governors();
    let cores_data = cpu::read_core_data();
    let load = cpu::read_cpu_load();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  online_cores: {:?}", online_cores);
    println!("  total_clusters: {}", clusters.len());
    for cluster in &clusters {
        println!(
            "  cluster {}: policy={}, cores={:?}, freq={}MHz-{}MHz, gov={}",
            cluster.cluster_number,
            cluster.policy_path,
            cluster.cores,
            cluster.min_freq / 1000,
            cluster.max_freq / 1000,
            cluster.governor
        );
    }
    println!("  available_governors: {:?}", av_govs);

    print!("  live_frequencies: ");
    for c in &cores_data {
        print!("C{}: {}MHz ", c.core_number, c.current_freq / 1000);
    }
    println!();
    println!("  load: {:.2}%", load);

    if let Some(first_cluster) = clusters.first() {
        let cluster_num = first_cluster.cluster_number;
        let test_lock_freq = first_cluster.min_freq as u64;
        println!(
            "  ceiling_lock_test: cluster={}, freq={}KHz",
            cluster_num, test_lock_freq
        );
        let t_lock = Instant::now();
        match cpu::lock_cluster_frequency(cluster_num, test_lock_freq) {
            Ok(_) => {
                let lock_time = t_lock.elapsed();
                let mounts = std::fs::read_to_string("/proc/mounts").unwrap_or_default();
                let has_mount = mounts.contains(&format!("policy{}/scaling_max_freq", cluster_num));
                println!(
                    "    mount_status: applied (in /proc/mounts: {}) [took {}]",
                    has_mount,
                    util::fmt_duration(lock_time)
                );
                let t_unlock = Instant::now();
                match cpu::unlock_cluster_frequency(cluster_num) {
                    Ok(_) => println!(
                        "    unlock_status: detached cleanly [took {}]",
                        util::fmt_duration(t_unlock.elapsed())
                    ),
                    Err(e) => println!("    unlock_status: failed ({})", e),
                }
            }
            Err(e) => println!("    lock_status: failed ({})", e),
        }
    }
}
