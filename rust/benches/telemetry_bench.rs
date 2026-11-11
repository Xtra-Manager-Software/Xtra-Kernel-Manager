use criterion::{black_box, criterion_group, criterion_main, Criterion};
use xkm_native::platform::cpu::frequency::read_cpu_load;
use xkm_native::platform::memory::meminfo::read_memory_and_swap;
use xkm_native::telemetry::collect_snapshot;

fn bench_collect_snapshot(c: &mut Criterion) {
    c.bench_function("collect_snapshot_1_pass", |b| {
        b.iter(|| {
            black_box(collect_snapshot());
        });
    });
}

fn bench_cpu_load(c: &mut Criterion) {
    c.bench_function("read_cpu_load", |b| {
        b.iter(|| {
            black_box(read_cpu_load());
        });
    });
}

fn bench_meminfo(c: &mut Criterion) {
    c.bench_function("read_memory_and_swap", |b| {
        b.iter(|| {
            black_box(read_memory_and_swap());
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
    bench_cpu_load,
    bench_meminfo,
    bench_json_serialization
);
criterion_main!(benches);
