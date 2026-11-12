use std::fs;
use tempfile::tempdir;

#[test]
fn test_mocked_cpu_policy_reading() {
    let dir = tempdir().expect("Failed to create tempdir");
    let base = dir.path();

    // Create policy0
    let policy0 = base.join("policy0");
    fs::create_dir_all(&policy0).unwrap();
    fs::write(policy0.join("affected_cpus"), "0 1 2 3\n").unwrap();
    fs::write(policy0.join("cpuinfo_min_freq"), "300000\n").unwrap();
    fs::write(policy0.join("cpuinfo_max_freq"), "1800000\n").unwrap();
    fs::write(policy0.join("scaling_cur_freq"), "1200000\n").unwrap();
    fs::write(policy0.join("scaling_governor"), "schedutil\n").unwrap();
    fs::write(
        policy0.join("scaling_available_governors"),
        "schedutil performance powersave\n",
    )
    .unwrap();

    // Verify file content parsing
    let affected = fs::read_to_string(policy0.join("affected_cpus")).unwrap();
    let cores: Vec<i32> = affected
        .split_whitespace()
        .filter_map(|s| s.parse().ok())
        .collect();
    assert_eq!(cores, vec![0, 1, 2, 3]);

    let max_freq: i64 = fs::read_to_string(policy0.join("cpuinfo_max_freq"))
        .unwrap()
        .trim()
        .parse()
        .unwrap();
    assert_eq!(max_freq, 1800000);

    let gov = fs::read_to_string(policy0.join("scaling_governor")).unwrap();
    assert_eq!(gov.trim(), "schedutil");
}

#[test]
fn test_mocked_thermal_zone_reading() {
    let dir = tempdir().expect("Failed to create tempdir");
    let base = dir.path();

    let tz0 = base.join("thermal_zone0");
    let tz1 = base.join("thermal_zone1");
    fs::create_dir_all(&tz0).unwrap();
    fs::create_dir_all(&tz1).unwrap();

    fs::write(tz0.join("type"), "cpu-1-0\n").unwrap();
    fs::write(tz0.join("temp"), "42500\n").unwrap();

    fs::write(tz1.join("type"), "battery\n").unwrap();
    fs::write(tz1.join("temp"), "34000\n").unwrap();

    let temp0: f32 = fs::read_to_string(tz0.join("temp"))
        .unwrap()
        .trim()
        .parse::<f32>()
        .unwrap()
        / 1000.0;
    assert_eq!(temp0, 42.5);

    let temp1: f32 = fs::read_to_string(tz1.join("temp"))
        .unwrap()
        .trim()
        .parse::<f32>()
        .unwrap()
        / 1000.0;
    assert_eq!(temp1, 34.0);
}

#[test]
fn test_mocked_proc_meminfo_parsing() {
    let dir = tempdir().expect("Failed to create tempdir");
    let meminfo_file = dir.path().join("meminfo");

    let sample_meminfo = "\
MemTotal:        8192000 kB
MemFree:         1024000 kB
MemAvailable:    4096000 kB
Buffers:          256000 kB
Cached:          2816000 kB
SwapTotal:       2048000 kB
SwapFree:        1024000 kB
";
    fs::write(&meminfo_file, sample_meminfo).unwrap();

    let content = fs::read_to_string(&meminfo_file).unwrap();
    let mut total = 0i64;
    let mut avail = 0i64;

    for line in content.lines() {
        let parts: Vec<&str> = line.split_whitespace().collect();
        if parts.len() >= 2 {
            if parts[0] == "MemTotal:" {
                total = parts[1].parse().unwrap();
            } else if parts[0] == "MemAvailable:" {
                avail = parts[1].parse().unwrap();
            }
        }
    }

    assert_eq!(total, 8192000);
    assert_eq!(avail, 4096000);
}

#[test]
fn test_mocked_kgsl_gpu_busy() {
    let dir = tempdir().expect("Failed to create tempdir");
    let gpubusy_file = dir.path().join("gpubusy");

    // "busy_cycles total_cycles"
    fs::write(&gpubusy_file, "150000 600000\n").unwrap();

    let content = fs::read_to_string(&gpubusy_file).unwrap();
    let parts: Vec<i64> = content
        .split_whitespace()
        .filter_map(|s| s.parse().ok())
        .collect();
    assert_eq!(parts.len(), 2);
    let busy = parts[0];
    let total = parts[1];

    let pct = (busy as f64 / total as f64) * 100.0;
    assert_eq!(pct as i32, 25);
}
