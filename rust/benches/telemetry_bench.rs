use criterion::{black_box, criterion_group, criterion_main, Criterion};
use xkm_native::platform::cpu::frequency::{read_core_data, read_cpu_load};
use xkm_native::platform::cpu::topology::detect_cpu_clusters;
use xkm_native::platform::memory::meminfo::read_memory_and_swap;
use xkm_native::platform::memory::zram::read_zram_stats;
use xkm_native::platform::thermal::zones::scan_thermal_zones;
use xkm_native::telemetry::collect_snapshot;

fn bench_collect_snapshot(c: &mut Criterion) {
    c.bench_function("collect_snapshot_1_pass", |b| {
        b.iter(|| {
            black_box(collect_snapshot());
        });
    });
}

fn bench_cpu_metrics(c: &mut Criterion) {
    let mut group = c.benchmark_group("cpu");
    group.bench_function("read_cpu_load", |b| {
        b.iter(|| {
            black_box(read_cpu_load());
        });
    });
    group.bench_function("read_core_data", |b| {
        b.iter(|| {
            black_box(read_core_data());
        });
    });
    group.bench_function("detect_cpu_clusters", |b| {
        b.iter(|| {
            black_box(detect_cpu_clusters());
        });
    });
    group.finish();
}

fn bench_memory_metrics(c: &mut Criterion) {
    let mut group = c.benchmark_group("memory");
    group.bench_function("read_memory_and_swap", |b| {
        b.iter(|| {
            black_box(read_memory_and_swap());
        });
    });
    group.bench_function("read_zram_stats", |b| {
        b.iter(|| {
            black_box(read_zram_stats());
        });
    });
    group.finish();
}

fn bench_thermal_metrics(c: &mut Criterion) {
    c.bench_function("scan_thermal_zones", |b| {
        b.iter(|| {
            black_box(scan_thermal_zones());
        });
    });
}

fn bench_json_serialization(c: &mut Criterion) {
    let snapshot = collect_snapshot();
    c.bench_function("snapshot_to_json", |b| {
        b.iter(|| {
            black_box(serde_json::to_string(&snapshot).unwrap());
        });
    });
}

criterion_group!(
    benches,
    bench_collect_snapshot,
    bench_cpu_metrics,
    bench_memory_metrics,
    bench_thermal_metrics,
    bench_json_serialization
);
criterion_main!(benches);
