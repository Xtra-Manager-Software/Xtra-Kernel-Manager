use criterion::{Criterion, black_box, criterion_group, criterion_main};
use std::fs::File;
use std::io::Write;
use tempfile::tempdir;
use xkm_native::platform::fps::{FpsReading, FpsSource, SysfsFpsReader, parse_sysfs_fps, read_fps};

fn bench_parse_sysfs_fps(c: &mut Criterion) {
    let mut group = c.benchmark_group("fps_parsing");

    // Case 1: Best case - clean raw float
    group.bench_function("parse_raw_float_best_case", |b| {
        let input = "60.0";
        b.iter(|| {
            black_box(parse_sysfs_fps(black_box(input)));
        });
    });

    // Case 2: Realistic vendor format (Qualcomm SDE CRTC measured_fps)
    group.bench_function("parse_sde_vendor_format", |b| {
        let input = "fps: 119.8 duration:1000000 frame_count:120 vsync_rate: 120";
        b.iter(|| {
            black_box(parse_sysfs_fps(black_box(input)));
        });
    });

    // Case 3: Alternate key-value format
    group.bench_function("parse_key_value_semicolon", |b| {
        let input = "FPS: 90.0; duration=1000; status=active";
        b.iter(|| {
            black_box(parse_sysfs_fps(black_box(input)));
        });
    });

    // Case 4: Worst case - large garbage text (4KB of non-matching tokens)
    let garbage: String = "unknown vendor parameter metric value counter ".repeat(100);
    group.bench_function("parse_large_garbage_worst_case", |b| {
        b.iter(|| {
            black_box(parse_sysfs_fps(black_box(&garbage)));
        });
    });

    // Case 5: Empty string
    group.bench_function("parse_empty_string", |b| {
        b.iter(|| {
            black_box(parse_sysfs_fps(black_box("")));
        });
    });

    group.finish();
}

fn bench_sysfs_reader(c: &mut Criterion) {
    let mut group = c.benchmark_group("fps_sysfs_reader");

    let dir = tempdir().unwrap();
    let file_path = dir.path().join("measured_fps");
    let mut file = File::create(&file_path).unwrap();
    writeln!(file, "fps: 119.8 duration:1000000").unwrap();

    let mut reader = SysfsFpsReader {
        path: Some(file_path.to_str().unwrap().to_string()),
        last_poll: std::time::Instant::now(),
        cached: Some(FpsReading::new(120.0, FpsSource::Sysfs)),
    };

    // Case 1: Best case - cached hit (within 250ms interval)
    group.bench_function("sysfs_cached_hit_best_case", |b| {
        b.iter(|| {
            black_box(reader.read());
        });
    });

    // Case 2: Worst case - uncached disk I/O read
    group.bench_function("sysfs_uncached_disk_read_worst_case", |b| {
        b.iter(|| {
            reader.cached = None;
            reader.last_poll = std::time::Instant::now()
                .checked_sub(std::time::Duration::from_secs(1))
                .unwrap();
            black_box(reader.read());
        });
    });

    group.finish();
}

fn bench_fps_meter_read(c: &mut Criterion) {
    let mut group = c.benchmark_group("fps_meter");

    // Benchmarking global read_fps() function call
    group.bench_function("global_read_fps", |b| {
        b.iter(|| {
            black_box(read_fps());
        });
    });

    group.finish();
}

criterion_group!(
    benches,
    bench_parse_sysfs_fps,
    bench_sysfs_reader,
    bench_fps_meter_read
);
criterion_main!(benches);
