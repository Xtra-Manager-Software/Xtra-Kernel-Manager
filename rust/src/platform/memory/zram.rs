use anyhow::Result;
use serde::{Deserialize, Serialize};
use std::fs;

use crate::platform::common;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ZramStats {
    pub disksize: i64,
    pub orig_data_size: i64,
    pub compr_data_size: i64,
    pub mem_used_total: i64,
    pub compression_ratio: f32,
}

pub fn read_zram_stats() -> ZramStats {
    let disksize = common::read_sysfs_i64("/sys/block/zram0/disksize").unwrap_or(0);
    // mm_stat: orig_data_size compr_data_size mem_used_total ...
    let (orig_data_size, compr_data_size, mem_used_total) =
        match common::read_sysfs_trimmed("/sys/block/zram0/mm_stat") {
            Some(s) => {
                let parts: Vec<i64> = s
                    .split_whitespace()
                    .filter_map(|p| p.parse::<i64>().ok())
                    .collect();
                match parts.as_slice() {
                    [orig, compr, mem, ..] => (*orig, *compr, *mem),
                    _ => (0, 0, 0),
                }
            }
            None => (
                common::read_sysfs_i64("/sys/block/zram0/orig_data_size").unwrap_or(0),
                common::read_sysfs_i64("/sys/block/zram0/compr_data_size").unwrap_or(0),
                common::read_sysfs_i64("/sys/block/zram0/mem_used_total").unwrap_or(0),
            ),
        };

    let compression_ratio = match (compr_data_size > 0, orig_data_size > 0) {
        (true, true) => orig_data_size as f32 / compr_data_size as f32,
        _ => 0.0,
    };

    ZramStats {
        disksize,
        orig_data_size,
        compr_data_size,
        mem_used_total,
        compression_ratio,
    }
}

pub fn get_available_zram_algorithms() -> Vec<String> {
    match fs::read_to_string("/sys/block/zram0/comp_algorithm") {
        Ok(content) => content
            .split_whitespace()
            .map(|s| s.trim_matches(|c| c == '[' || c == ']').to_string())
            .collect(),
        Err(_) => vec!["lz4".to_string(), "zstd".to_string()],
    }
}

pub fn get_current_zram_algorithm() -> String {
    if let Ok(content) = fs::read_to_string("/sys/block/zram0/comp_algorithm") {
        for part in content.split_whitespace() {
            if part.starts_with('[') && part.ends_with(']') {
                return part[1..part.len() - 1].to_string();
            }
        }
    }
    "unknown".to_string()
}

pub fn get_swappiness() -> i32 {
    common::read_sysfs_i64("/proc/sys/vm/swappiness").unwrap_or(60) as i32
}

pub fn set_swappiness(val: i32) -> Result<()> {
    common::write_sysfs("/proc/sys/vm/swappiness", &val.to_string())
}
