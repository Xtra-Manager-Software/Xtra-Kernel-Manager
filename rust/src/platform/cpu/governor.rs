use anyhow::Result;
use std::fs;
use std::path::{Path, PathBuf};
use std::sync::OnceLock;

use crate::platform::common;

pub struct CpuGovernorPaths {
    pub policy_governors: Vec<PathBuf>,
    pub cpu_governors: Vec<PathBuf>,
}

static GOVERNOR_PATHS: OnceLock<CpuGovernorPaths> = OnceLock::new();
static AVAILABLE_GOVERNORS: OnceLock<Vec<String>> = OnceLock::new();

fn scan_governor_paths() -> CpuGovernorPaths {
    let mut policy_governors = Vec::with_capacity(8);
    for i in 0..8 {
        let p = PathBuf::from(format!(
            "/sys/devices/system/cpu/cpufreq/policy{}/scaling_governor",
            i
        ));
        if p.exists() {
            policy_governors.push(p);
        }
    }

    let mut cpu_governors = Vec::with_capacity(16);
    for i in 0..16 {
        let p = PathBuf::from(format!(
            "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
            i
        ));
        if p.exists() {
            cpu_governors.push(p);
        }
    }

    CpuGovernorPaths {
        policy_governors,
        cpu_governors,
    }
}

pub fn get_available_governors() -> &'static [String] {
    AVAILABLE_GOVERNORS.get_or_init(|| {
        let candidates = [
            "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_governors",
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors",
        ];
        for path in candidates {
            if let Ok(content) = fs::read_to_string(path) {
                let list: Vec<String> = content
                    .split_whitespace()
                    .filter(|s| !s.is_empty())
                    .map(|s| s.to_string())
                    .collect();
                if !list.is_empty() {
                    return list;
                }
            }
        }
        vec![
            "schedutil".to_string(),
            "performance".to_string(),
            "powersave".to_string(),
        ]
    })
}

pub fn set_governor_for_cluster(cluster_or_policy: i32, governor: &str) -> Result<()> {
    let policy_path = format!(
        "/sys/devices/system/cpu/cpufreq/policy{}/scaling_governor",
        cluster_or_policy
    );
    if Path::new(&policy_path).exists() {
        return common::write_sysfs(&policy_path, governor);
    }

    let cpu_path = format!(
        "/sys/devices/system/cpu/cpu{}/cpufreq/scaling_governor",
        cluster_or_policy
    );
    common::write_sysfs(&cpu_path, governor)
}

pub fn set_all_governors(governor: &str) -> Result<()> {
    let paths = GOVERNOR_PATHS.get_or_init(scan_governor_paths);

    let targets = match paths.policy_governors.is_empty() {
        false => &paths.policy_governors,
        true => &paths.cpu_governors,
    };
    for path in targets {
        let _ = fs::write(path, governor);
    }
    Ok(())
}

pub fn online_all_cores() {
    for i in 1..16 {
        let path = format!("/sys/devices/system/cpu/cpu{}/online", i);
        if Path::new(&path).exists() {
            let _ = fs::write(&path, "1");
        }
    }
}
