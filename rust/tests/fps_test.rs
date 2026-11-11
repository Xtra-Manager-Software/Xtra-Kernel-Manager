use std::fs::File;
use std::io::Write;
use tempfile::tempdir;
use xkm_native::platform::fps::{
    FpsMeter, FpsReading, FpsSource, SysfsFpsReader, parse_sysfs_fps,
};

#[test]
fn test_parse_sysfs_fps_formats() {
    // 1. Raw float
    assert_eq!(parse_sysfs_fps("60.0"), Some(60.0));
    assert_eq!(parse_sysfs_fps("120.000\n"), Some(120.0));
    assert_eq!(parse_sysfs_fps("90"), Some(90.0));

    // 2. Qualcomm SDE CRTC measured_fps format
    let sde_sample = "fps: 119.8 duration:1000000 frame_count:120";
    assert_eq!(parse_sysfs_fps(sde_sample), Some(119.8));

    // 3. Variant key-values
    assert_eq!(parse_sysfs_fps("FPS: 59.94"), Some(59.94));
    assert_eq!(parse_sysfs_fps("fps=144.0"), Some(144.0));
    assert_eq!(parse_sysfs_fps("fps 90.0"), Some(90.0));
    assert_eq!(parse_sysfs_fps("FPS: 60; duration=1000"), Some(60.0));

    // 4. Out of bounds & garbage
    assert_eq!(parse_sysfs_fps(""), None);
    assert_eq!(parse_sysfs_fps("   \n"), None);
    assert_eq!(parse_sysfs_fps("not_a_number"), None);
    assert_eq!(parse_sysfs_fps("-10.0"), None);
    assert_eq!(parse_sysfs_fps("9999.0"), None);
}

#[test]
fn test_sysfs_fps_reader_mock() {
    let dir = tempdir().unwrap();
    let file_path = dir.path().join("measured_fps");

    let mut file = File::create(&file_path).unwrap();
    writeln!(file, "fps: 120.0 duration:1000000").unwrap();

    let mut reader = SysfsFpsReader {
        path: Some(file_path.to_str().unwrap().to_string()),
        last_poll: std::time::Instant::now()
            .checked_sub(std::time::Duration::from_secs(1))
            .unwrap(),
        cached: None,
    };

    let reading = reader.read();
    assert!(reading.is_some());
    let r = reading.unwrap();
    assert_eq!(r.fps, 120.0);
    assert_eq!(r.source, FpsSource::Sysfs);

    // Immediate second read should return cached without re-reading
    let cached_r = reader.read().unwrap();
    assert_eq!(cached_r.fps, 120.0);

    // Clear works
    reader.clear();
    assert!(reader.cached.is_none());
}

#[test]
fn test_fps_meter_pid_guard() {
    let mut meter = FpsMeter::new();
    // Negative or zero PID rejected
    assert!(!meter.attach_pid(-1));
    assert!(!meter.attach_pid(0));
}

#[test]
fn test_fps_types_serialization() {
    let reading = FpsReading::new(120.5, FpsSource::Ebpf);
    let json = serde_json::to_string(&reading).unwrap();
    assert!(json.contains("120.5"));
    assert!(json.contains("Ebpf"));

    let deserialized: FpsReading = serde_json::from_str(&json).unwrap();
    assert_eq!(deserialized, reading);
}
