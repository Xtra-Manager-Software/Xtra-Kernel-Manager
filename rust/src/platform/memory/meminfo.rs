use serde::{Deserialize, Serialize};
use std::fs;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MemoryInfo {
    pub total_kb: i64,
    pub available_kb: i64,
    pub free_kb: i64,
    pub cached_kb: i64,
    pub buffers_kb: i64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SwapInfo {
    pub total_kb: i64,
    pub free_kb: i64,
    pub used_kb: i64,
    pub cached_kb: i64,
}

pub fn read_memory_and_swap() -> (MemoryInfo, SwapInfo) {
    let mut mem_total = 0i64;
    let mut mem_available = 0i64;
    let mut mem_free = 0i64;
    let mut mem_cached = 0i64;
    let mut mem_buffers = 0i64;

    let mut swap_total = 0i64;
    let mut swap_free = 0i64;
    let mut swap_cached = 0i64;

    if let Ok(content) = fs::read_to_string("/proc/meminfo") {
        for line in content.lines() {
            let mut parts = line.split_whitespace();
            let key = match parts.next() {
                Some(k) => k,
                None => continue,
            };
            let val = match parts.next().and_then(|v| v.parse::<i64>().ok()) {
                Some(v) => v,
                None => continue,
            };

            match key {
                "MemTotal:" => mem_total = val,
                "MemAvailable:" => mem_available = val,
                "MemFree:" => mem_free = val,
                "Cached:" => mem_cached = val,
                "Buffers:" => mem_buffers = val,
                "SwapTotal:" => swap_total = val,
                "SwapFree:" => swap_free = val,
                "SwapCached:" => swap_cached = val,
                _ => {}
            }
        }
    }

    let swap_used = swap_total.saturating_sub(swap_free);

    (
        MemoryInfo {
            total_kb: mem_total,
            available_kb: mem_available,
            free_kb: mem_free,
            cached_kb: mem_cached,
            buffers_kb: mem_buffers,
        },
        SwapInfo {
            total_kb: swap_total,
            free_kb: swap_free,
            used_kb: swap_used,
            cached_kb: swap_cached,
        },
    )
}

pub fn drop_caches() -> anyhow::Result<()> {
    crate::platform::common::write_sysfs("/proc/sys/vm/drop_caches", "3")
}
