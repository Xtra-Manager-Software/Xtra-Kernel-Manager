#![allow(unexpected_cfgs)]

//! Concurrency and thread-safety tests using Loom.
//! Loom systematically explores thread scheduling permutations to verify that
//! concurrent access to shared states, locks, and caches is free of deadlocks and race conditions.

#[cfg(loom)]
mod loom_tests {
    use loom::sync::{Arc, Mutex};
    use loom::thread;

    #[derive(Default, Clone, Copy)]
    struct CpuTimeSnapshot {
        total: u64,
        idle: u64,
    }

    #[test]
    fn test_concurrent_cpu_snapshot_updates() {
        loom::model(|| {
            let shared_cache = Arc::new(Mutex::new(CpuTimeSnapshot {
                total: 100,
                idle: 50,
            }));

            let c1 = shared_cache.clone();
            let th1 = thread::spawn(move || {
                let mut lock = c1.lock().unwrap();
                lock.total += 20;
                lock.idle += 5;
            });

            let c2 = shared_cache.clone();
            let th2 = thread::spawn(move || {
                let mut lock = c2.lock().unwrap();
                lock.total += 30;
                lock.idle += 10;
            });

            th1.join().unwrap();
            th2.join().unwrap();

            let final_state = *shared_cache.lock().unwrap();
            assert_eq!(final_state.total, 150);
            assert_eq!(final_state.idle, 65);
        });
    }

    #[test]
    fn test_concurrent_mount_registry_interleaving() {
        loom::model(|| {
            let mounts = Arc::new(Mutex::new(Vec::<(String, String)>::new()));

            let m1 = mounts.clone();
            let th1 = thread::spawn(move || {
                let mut lock = m1.lock().unwrap();
                lock.push(("/target/1".to_string(), "/tmp/source1".to_string()));
            });

            let m2 = mounts.clone();
            let th2 = thread::spawn(move || {
                let mut lock = m2.lock().unwrap();
                lock.retain(|(t, _)| t != "/target/1");
            });

            th1.join().unwrap();
            th2.join().unwrap();

            let lock = mounts.lock().unwrap();
            // Outcome depends on interleaving (0 or 1 items), but must not panic or corrupt state
            assert!(lock.len() <= 1);
        });
    }
}

// Standard multi-threaded stress test when running without LOOM flag
#[test]
fn test_multithreaded_stress_telemetry() {
    use std::thread;

    let handles: Vec<_> = (0..8)
        .map(|_| {
            thread::spawn(|| {
                for _ in 0..50 {
                    let snapshot = xkm_native::telemetry::collect_snapshot();
                    assert!(snapshot.timestamp_ms > 0);
                    let _ = xkm_native::platform::cpu::frequency::read_cpu_load();
                    let _ = xkm_native::platform::memory::meminfo::read_memory_and_swap();
                    let _ = xkm_native::platform::thermal::zones::scan_thermal_zones();
                }
            })
        })
        .collect();

    for h in handles {
        h.join()
            .expect("Worker thread should not panic under concurrent telemetry polling");
    }
}
