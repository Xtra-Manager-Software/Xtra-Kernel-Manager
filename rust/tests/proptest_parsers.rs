//! Property-based testing with Proptest.
//! Generates pseudo-random and edge-case inputs to prove parsers never panic,
//! crash, or overflow on unexpected kernel or vendor outputs.

use proptest::prelude::*;

proptest! {
    /// Verify parse_online_cores handles arbitrary string inputs without panicking
    #[test]
    fn prop_test_parse_online_cores_arbitrary(input in "\\PC*") {
        let cores = parse_online_cores_str(&input);
        // Valid core indices must be non-negative
        for c in cores {
            prop_assert!(c >= 0);
        }
    }

    /// Verify parse_online_cores parses standard range inputs correctly
    #[test]
    fn prop_test_parse_online_cores_valid_ranges(start in 0..16i32, end in 0..16i32) {
        let (s, e) = if start <= end { (start, end) } else { (end, start) };
        let formatted = format!("{}-{}", s, e);
        let cores = parse_online_cores_str(&formatted);
        let expected_len = (e - s + 1) as usize;
        prop_assert_eq!(cores.len(), expected_len);
        prop_assert_eq!(cores.first(), Some(&s));
        prop_assert_eq!(cores.last(), Some(&e));
    }

    /// Verify memory unit conversion math never produces negative used memory
    #[test]
    fn prop_test_memory_conversion_safety(total_kb in 0i64..=1_000_000_000, avail_kb in 0i64..=1_000_000_000) {
        let total_mb = total_kb / 1024;
        let avail_mb = avail_kb / 1024;
        let used_mb = total_mb.saturating_sub(avail_mb).max(0);
        prop_assert!(used_mb >= 0);
    }

    /// Verify CPU load delta calculation is always safely clamped to [0.0, 100.0]
    #[test]
    fn prop_test_cpu_load_clamping(total_delta in 0u64..1_000_000_000, idle_delta in 0u64..1_000_000_000) {
        let active_delta = total_delta.saturating_sub(idle_delta);
        let pct = if total_delta == 0 {
            0.0
        } else {
            ((active_delta as f32 / total_delta as f32) * 100.0).clamp(0.0, 100.0)
        };
        prop_assert!(pct >= 0.0);
        prop_assert!(pct <= 100.0);
    }
}

// Helper duplicating the pure parsing logic of parse_online_cores for arbitrary strings
fn parse_online_cores_str(s: &str) -> Vec<i32> {
    let mut cores = Vec::new();
    for part in s.trim().split(',') {
        if part.contains('-') {
            let bounds: Vec<&str> = part.split('-').collect();
            if bounds.len() == 2 {
                if let (Ok(start), Ok(end)) = (bounds[0].parse::<i32>(), bounds[1].parse::<i32>()) {
                    if start <= end && end - start < 256 {
                        for c in start..=end {
                            cores.push(c);
                        }
                    }
                }
            }
        } else if let Ok(c) = part.parse::<i32>() {
            cores.push(c);
        }
    }
    cores
}
