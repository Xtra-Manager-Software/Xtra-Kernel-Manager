use xkm_native::telemetry::{collect_snapshot, TelemetrySnapshot};

#[test]
fn test_collect_snapshot() {
    let snapshot = collect_snapshot();
    assert!(snapshot.timestamp_ms > 0);
    assert!(!snapshot.cpu.model.is_empty());
    println!("Snapshot collected: timestamp={}, cpu_load={:.1}%, gpu_freq={}MHz, ram_avail={}MB",
        snapshot.timestamp_ms, snapshot.cpu.load_pct, snapshot.gpu.freq_mhz, snapshot.memory.available_mb);
}

#[test]
fn test_snapshot_json_serialization() {
    let snapshot = collect_snapshot();
    let json = serde_json::to_string(&snapshot).expect("TelemetrySnapshot should serialize to JSON");
    assert!(!json.is_empty());

    let deserialized: TelemetrySnapshot = serde_json::from_str(&json)
        .expect("TelemetrySnapshot should deserialize from JSON");
    assert_eq!(deserialized.timestamp_ms, snapshot.timestamp_ms);
    assert_eq!(deserialized.cpu.model, snapshot.cpu.model);
    assert_eq!(deserialized.gpu.vendor, snapshot.gpu.vendor);
}
