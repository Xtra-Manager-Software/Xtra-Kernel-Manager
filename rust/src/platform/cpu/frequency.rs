use parking_lot::Mutex;
use std::fs;
use std::path::Path;
use std::sync::OnceLock;

use super::topology::CoreInfo;
use crate::platform::common;

struct CpuTimeSnapshot {
    total: u64,
    idle: u64,
}

static CPU_STAT_CACHE: OnceLock<Mutex<CpuTimeSnapshot>> = OnceLock::new();

pub fn read_core_data() -> Vec<CoreInfo> {
    let mut result = Vec::with_capacity(8);

    for cpu in 0..16 {
        let cpu_dir = format!("/sys/devices/system/cpu/cpu{}", cpu);
        if !Path::new(&cpu_dir).exists() {
            break;
        }

        let online = match cpu {
            0 => true, // cpu0 is usually always online or has no online node
            _ => common::read_sysfs_i64(&format!("{}/online", cpu_dir)).unwrap_or(1) == 1,
        };

        let freq_dir = format!("{}/cpufreq", cpu_dir);
        let cur_freq = match online {
            true => common::read_sysfs_i64(&format!("{}/scaling_cur_freq", freq_dir))
                .or_else(|| common::read_sysfs_i64(&format!("{}/cpuinfo_cur_freq", freq_dir)))
                .unwrap_or(0) as i32,
            false => 0,
        };

        let min_freq =
            common::read_sysfs_i64(&format!("{}/scaling_min_freq", freq_dir)).unwrap_or(0) as i32;
        let max_freq =
            common::read_sysfs_i64(&format!("{}/scaling_max_freq", freq_dir)).unwrap_or(0) as i32;
        let governor = common::read_sysfs_trimmed(&format!("{}/scaling_governor", freq_dir))
            .unwrap_or_else(|| "offline".to_string());

        result.push(CoreInfo {
            core_number: cpu,
            online,
            current_freq: cur_freq,
            min_freq,
            max_freq,
            governor,
        });
    }

    result
}

/// Parse total and idle times from /proc/stat content without heap allocation.
fn parse_cpu_stat_times(content: &str) -> Option<(u64, u64)> {
    let first_line = content.lines().next().filter(|l| l.starts_with("cpu "))?;
    let mut fields = first_line
        .split_whitespace()
        .skip(1)
        .filter_map(|s| s.parse::<u64>().ok());

    let user = fields.next()?;
    let nice = fields.next()?;
    let system = fields.next()?;
    let idle = fields.next()?;
    let iowait = fields.next().unwrap_or(0);
    let irq = fields.next().unwrap_or(0);
    let softirq = fields.next().unwrap_or(0);
    let steal = fields.next().unwrap_or(0);

    let idle_time = idle + iowait;
    let total_time = user + nice + system + idle + iowait + irq + softirq + steal;
    Some((total_time, idle_time))
}

#[inline]
fn calculate_cpu_load_pct(total_delta: u64, idle_delta: u64) -> f32 {
    if total_delta == 0 {
        return 0.0;
    }
    let active_delta = total_delta.saturating_sub(idle_delta);
    ((active_delta as f32 / total_delta as f32) * 100.0).clamp(0.0, 100.0)
}

pub fn read_cpu_load() -> f32 {
    let content = match fs::read_to_string("/proc/stat") {
        Ok(c) => c,
        Err(_) => return 0.0,
    };

    let (total_time, idle_time) = match parse_cpu_stat_times(&content) {
        Some(times) => times,
        None => return 0.0,
    };

    let cache = CPU_STAT_CACHE.get_or_init(|| Mutex::new(CpuTimeSnapshot { total: 0, idle: 0 }));

    let mut lock = cache.lock();
    if lock.total == 0 {
        lock.total = total_time;
        lock.idle = idle_time;
        // Sample brief 10ms delta on initial call so first reading is meaningful
        std::thread::sleep(std::time::Duration::from_millis(10));

        let Ok(c2) = fs::read_to_string("/proc/stat") else {
            return 0.0;
        };
        let Some((t2, i2)) = parse_cpu_stat_times(&c2) else {
            return 0.0;
        };

        let dt = t2.saturating_sub(total_time);
        let di = i2.saturating_sub(idle_time);
        lock.total = t2;
        lock.idle = i2;
        return calculate_cpu_load_pct(dt, di);
    }

    let total_delta = total_time.saturating_sub(lock.total);
    let idle_delta = idle_time.saturating_sub(lock.idle);

    lock.total = total_time;
    lock.idle = idle_time;

    calculate_cpu_load_pct(total_delta, idle_delta)
}
