use xkm_native::platform::cpu::frequency::{read_core_data, read_cpu_load};
use xkm_native::platform::cpu::governor::get_available_governors;
use xkm_native::platform::cpu::topology::{detect_cpu_clusters, get_cpu_model, parse_online_cores};

#[test]
fn test_parse_online_cores() {
    // Normal 8-core single range
    let cores = parse_online_cores();
    assert!(!cores.is_empty(), "Online cores list should not be empty");
}

#[test]
fn test_get_cpu_model() {
    let model = get_cpu_model();
    assert!(
        !model.is_empty(),
        "CPU model should return a non-empty string"
    );
}

#[test]
fn test_detect_cpu_clusters() {
    let clusters = detect_cpu_clusters();
    // On any Linux/Android host, clusters are either detected or grouped
    println!("Detected {} CPU clusters", clusters.len());
    for cluster in &clusters {
        assert!(cluster.cluster_number >= 0);
        println!(
            "Cluster {}: cores {:?}, min={}, max={}, gov={}",
            cluster.cluster_number,
            cluster.cores,
            cluster.min_freq,
            cluster.max_freq,
            cluster.governor
        );
    }
}

#[test]
fn test_read_core_data() {
    let cores = read_core_data();
    println!("Read {} cores data", cores.len());
    for core in &cores {
        assert!(core.core_number >= 0);
        assert!(core.min_freq >= 0);
        assert!(core.max_freq >= 0);
    }
}

#[test]
fn test_read_cpu_load() {
    let load = read_cpu_load();
    assert!(
        (0.0..=100.0).contains(&load),
        "CPU load should be between 0.0 and 100.0, got {}",
        load
    );
}

#[test]
fn test_available_governors() {
    let govs = get_available_governors();
    assert!(!govs.is_empty(), "Available governors should not be empty");
}
