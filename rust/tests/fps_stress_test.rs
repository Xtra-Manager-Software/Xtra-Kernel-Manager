use std::fs::File;
use std::io::Write;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use std::thread;
use std::time::Duration;
use tempfile::tempdir;
use xkm_native::platform::fps::{
    attach_fps_pid, clear_fps, detach_fps_pid, parse_sysfs_fps, read_fps, FpsMeter,
    SysfsFpsReader,
};

#[test]
fn test_worst_case_garbage_and_giant_strings() {
    // 1. Giant string (100,000 bytes) with no fps keywords
    let giant_garbage = "garbage_token_value_extra_long_word ".repeat(3000);
    assert_eq!(parse_sysfs_fps(&giant_garbage), None);

    // 2. Giant string with fps buried in the middle
    let mut buried = "leading_noise_text ".repeat(1500);
    buried.push_str(" fps: 88.5 duration:1000 ");
    buried.push_str(&"trailing_noise_text ".repeat(1500));
    assert_eq!(parse_sysfs_fps(&buried), Some(88.5));

    // 3. String with embedded null bytes and control chars
    let with_nulls = "noise\0\r\n\t fps: 120.0 \0\0 more noise";
    assert_eq!(parse_sysfs_fps(with_nulls), Some(120.0));

    // 4. Repeated colons and broken delimiters
    assert_eq!(parse_sysfs_fps("fps::60"), None);
    assert_eq!(parse_sysfs_fps("fps===120"), None);
    assert_eq!(parse_sysfs_fps("fps: , ; 60.0"), Some(60.0));
    assert_eq!(parse_sysfs_fps(";;;,,,   fps: 90.0   ,,,;;;"), Some(90.0));
}

#[test]
fn test_worst_case_boundary_and_special_floats() {
    // 1. Minimum and maximum valid bounds [0.0, 500.0]
    assert_eq!(parse_sysfs_fps("0.0"), Some(0.0));
    assert_eq!(parse_sysfs_fps("0.1"), Some(0.1));
    assert_eq!(parse_sysfs_fps("500.0"), Some(500.0));

    // 2. Out of bounds (negative, zero subnormal, above 500)
    assert_eq!(parse_sysfs_fps("-0.1"), None);
    assert_eq!(parse_sysfs_fps("-500.0"), None);
    assert_eq!(parse_sysfs_fps("500.1"), None);
    assert_eq!(parse_sysfs_fps("99999.0"), None);

    // 3. Special float literals (NaN, Inf)
    assert_eq!(parse_sysfs_fps("NaN"), None);
    assert_eq!(parse_sysfs_fps("nan"), None);
    assert_eq!(parse_sysfs_fps("inf"), None);
    assert_eq!(parse_sysfs_fps("+inf"), None);
    assert_eq!(parse_sysfs_fps("-inf"), None);

    // 4. Scientific notation
    assert_eq!(parse_sysfs_fps("fps: 1e2"), Some(100.0));
    assert_eq!(parse_sysfs_fps("fps: 1e-1"), Some(0.1));
    assert_eq!(parse_sysfs_fps("fps: 1e5"), None); // 100000 > 500
}

#[test]
fn test_sysfs_missing_or_empty_files() {
    // 1. Non-existent path
    let mut missing_reader = SysfsFpsReader {
        path: Some("/non/existent/dev/drm/fps_test".to_string()),
        last_poll: std::time::Instant::now()
            .checked_sub(Duration::from_secs(1))
            .unwrap(),
        cached: None,
    };
    assert_eq!(missing_reader.read(), None);

    // 2. Empty file (0 bytes)
    let dir = tempdir().unwrap();
    let empty_file_path = dir.path().join("empty_fps");
    File::create(&empty_file_path).unwrap();

    let mut empty_reader = SysfsFpsReader {
        path: Some(empty_file_path.to_str().unwrap().to_string()),
        last_poll: std::time::Instant::now()
            .checked_sub(Duration::from_secs(1))
            .unwrap(),
        cached: None,
    };
    assert_eq!(empty_reader.read(), None);

    // 3. File containing only spaces and newlines
    let whitespace_file_path = dir.path().join("whitespace_fps");
    let mut wf = File::create(&whitespace_file_path).unwrap();
    writeln!(wf, "   \n\r\t   ").unwrap();

    let mut whitespace_reader = SysfsFpsReader {
        path: Some(whitespace_file_path.to_str().unwrap().to_string()),
        last_poll: std::time::Instant::now()
            .checked_sub(Duration::from_secs(1))
            .unwrap(),
        cached: None,
    };
    assert_eq!(whitespace_reader.read(), None);
}

#[test]
fn test_concurrent_hammering_stress() {
    // 16 threads concurrently calling read_fps(), attach_fps_pid(), clear_fps()
    const THREADS: usize = 16;
    const OPS_PER_THREAD: usize = 1000;

    let stop_flag = Arc::new(AtomicBool::new(false));
    let mut handles = Vec::with_capacity(THREADS);

    for t in 0..THREADS {
        let stop = Arc::clone(&stop_flag);
        handles.push(thread::spawn(move || {
            for i in 0..OPS_PER_THREAD {
                if stop.load(Ordering::Relaxed) {
                    break;
                }
                match i % 4 {
                    0 => {
                        let _ = read_fps();
                    }
                    1 => {
                        let fake_pid = (1000 + (t * 100) + (i % 50)) as i32;
                        let _ = attach_fps_pid(fake_pid);
                    }
                    2 => {
                        let _ = detach_fps_pid();
                    }
                    _ => {
                        if i % 100 == 0 {
                            clear_fps();
                        }
                    }
                }
            }
        }));
    }

    for h in handles {
        h.join().expect("Worker thread panicked under concurrency stress");
    }

    clear_fps();
}

#[test]
fn test_rapid_pid_churn_stress() {
    let mut meter = FpsMeter::new();

    // Rapidly attach and detach 500 different PIDs in a tight loop
    for pid in 1000..1500 {
        meter.attach_pid(pid);
        meter.detach_current();
    }

    // Ensure state remains clean
    meter.clear();
    assert_eq!(meter.read(), None);
}
