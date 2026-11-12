use serde::{Deserialize, Serialize};
use std::fs;
use std::path::Path;
use std::sync::OnceLock;

use crate::platform::common;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CpuCluster {
    pub cluster_number: i32,
    pub cores: Vec<i32>,
    pub max_freq: i32,
    pub min_freq: i32,
    #[serde(rename = "current_min_freq")]
    pub cur_min_freq: i32,
    #[serde(rename = "current_max_freq")]
    pub cur_max_freq: i32,
    pub governor: String,
    pub available_governors: Vec<String>,
    pub policy_path: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CoreInfo {
    #[serde(rename = "core")]
    pub core_number: i32,
    pub online: bool,
    #[serde(rename = "freq")]
    pub current_freq: i32,
    pub min_freq: i32,
    pub max_freq: i32,
    pub governor: String,
}

static CPU_MODEL: OnceLock<String> = OnceLock::new();

pub fn get_cpu_model() -> &'static str {
    CPU_MODEL
        .get_or_init(|| {
            // 1. Try soc/chip properties first
            for prop in ["ro.soc.model", "ro.chipname"] {
                if let Some(val) = common::get_system_property(prop) {
                    let trimmed = val.trim();
                    if !trimmed.is_empty() {
                        return trimmed.to_uppercase();
                    }
                }
            }

            // 2. Try /proc/cpuinfo Hardware line
            if let Ok(content) = fs::read_to_string("/proc/cpuinfo") {
                for line in content.lines() {
                    if (line.starts_with("Hardware") || line.starts_with("model name"))
                        && let Some((_, val)) = line.split_once(':')
                    {
                        let trimmed = val.trim();
                        if !trimmed.is_empty() && !trimmed.eq_ignore_ascii_case("qcom") {
                            return trimmed.to_string();
                        }
                    }
                }
            }

            // 3. Fallback properties
            for prop in ["ro.board.platform", "ro.hardware"] {
                if let Some(val) = common::get_system_property(prop) {
                    let trimmed = val.trim();
                    if !trimmed.is_empty() {
                        return trimmed.to_string();
                    }
                }
            }

            "Unknown".to_string()
        })
        .as_str()
}

pub fn parse_online_cores() -> Vec<i32> {
    let content = match fs::read_to_string("/sys/devices/system/cpu/online") {
        Ok(c) => c,
        Err(_) => return (0..8).collect(),
    };

    let mut cores = Vec::new();
    for part in content.trim().split(',') {
        match part.split_once('-') {
            Some((start_str, end_str)) => {
                if let (Ok(start), Ok(end)) = (start_str.parse::<i32>(), end_str.parse::<i32>()) {
                    cores.extend(start..=end);
                }
            }
            None => {
                if let Ok(c) = part.parse::<i32>() {
                    cores.push(c);
                }
            }
        }
    }
    cores
}

pub fn detect_cpu_clusters() -> Vec<CpuCluster> {
    use std::collections::BTreeMap;

    // Scan policies policy0..policy7 first, as modern kernels expose cpufreq policies directly
    let mut policy_clusters: Vec<CpuCluster> = Vec::new();
    for i in 0..8 {
        let policy_dir = format!("/sys/devices/system/cpu/cpufreq/policy{}", i);
        if !Path::new(&policy_dir).exists() {
            continue;
        }

        let affected = fs::read_to_string(format!("{}/affected_cpus", policy_dir))
            .or_else(|_| fs::read_to_string(format!("{}/related_cpus", policy_dir)))
            .unwrap_or_default();

        let cores: Vec<i32> = affected
            .split_whitespace()
            .filter_map(|s| s.parse::<i32>().ok())
            .collect();

        let min_freq =
            common::read_sysfs_i64(&format!("{}/cpuinfo_min_freq", policy_dir)).unwrap_or(0) as i32;
        let max_freq =
            common::read_sysfs_i64(&format!("{}/cpuinfo_max_freq", policy_dir)).unwrap_or(0) as i32;
        let cur_min = common::read_sysfs_i64(&format!("{}/scaling_min_freq", policy_dir))
            .unwrap_or(min_freq as i64) as i32;
        let cur_max = common::read_sysfs_i64(&format!("{}/scaling_max_freq", policy_dir))
            .unwrap_or(max_freq as i64) as i32;
        let governor = common::read_sysfs_trimmed(&format!("{}/scaling_governor", policy_dir))
            .unwrap_or_else(|| "unknown".to_string());

        let available_governors =
            common::read_sysfs_trimmed(&format!("{}/scaling_available_governors", policy_dir))
                .map(|s| s.split_whitespace().map(|g| g.to_string()).collect())
                .unwrap_or_else(|| {
                    vec![
                        "schedutil".to_string(),
                        "performance".to_string(),
                        "powersave".to_string(),
                    ]
                });

        policy_clusters.push(CpuCluster {
            cluster_number: i,
            cores,
            max_freq,
            min_freq,
            cur_min_freq: cur_min,
            cur_max_freq: cur_max,
            governor,
            available_governors,
            policy_path: policy_dir,
        });
    }

    if !policy_clusters.is_empty() {
        return policy_clusters;
    }

    // Fallback: Group by (min_freq, max_freq) across cpu0..cpu15
    let mut groups: BTreeMap<(i32, i32), Vec<i32>> = BTreeMap::new();
    for cpu in 0..16 {
        let cpu_dir = format!("/sys/devices/system/cpu/cpu{}/cpufreq", cpu);
        if !Path::new(&cpu_dir).exists() {
            break;
        }

        let min_f =
            common::read_sysfs_i64(&format!("{}/cpuinfo_min_freq", cpu_dir)).unwrap_or(0) as i32;
        let max_f =
            common::read_sysfs_i64(&format!("{}/cpuinfo_max_freq", cpu_dir)).unwrap_or(0) as i32;

        if min_f > 0 && max_f > 0 {
            groups.entry((min_f, max_f)).or_default().push(cpu);
        }
    }

    groups
        .into_iter()
        .enumerate()
        .map(|(idx, ((min, max), cores))| {
            let first = cores.first().copied().unwrap_or(0);
            let dir = format!("/sys/devices/system/cpu/cpu{}/cpufreq", first);
            let cur_min = common::read_sysfs_i64(&format!("{}/scaling_min_freq", dir))
                .unwrap_or(min as i64) as i32;
            let cur_max = common::read_sysfs_i64(&format!("{}/scaling_max_freq", dir))
                .unwrap_or(max as i64) as i32;
            let governor = common::read_sysfs_trimmed(&format!("{}/scaling_governor", dir))
                .unwrap_or_else(|| "unknown".to_string());
            let available_governors =
                common::read_sysfs_trimmed(&format!("{}/scaling_available_governors", dir))
                    .map(|s| s.split_whitespace().map(|g| g.to_string()).collect())
                    .unwrap_or_else(|| vec!["schedutil".to_string(), "performance".to_string()]);

            CpuCluster {
                cluster_number: idx as i32,
                cores,
                max_freq: max,
                min_freq: min,
                cur_min_freq: cur_min,
                cur_max_freq: cur_max,
                governor,
                available_governors,
                policy_path: dir,
            }
        })
        .collect()
}
