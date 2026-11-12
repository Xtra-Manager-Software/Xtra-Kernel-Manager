use std::sync::Arc;
use parking_lot::Mutex;

use super::diagnose::diagnose_drop_cause;
use super::types::{FpsReading, FpsSource, FrameDropCause};
use super::worker::{EBPF_TIMEOUT, SharedFrames};

pub fn calculate_ebpf_stats(shared: &Arc<Mutex<SharedFrames>>) -> Option<FpsReading> {
    let guard = shared.lock();
    if guard.frametimes.is_empty() || guard.last_frame_instant.elapsed() >= EBPF_TIMEOUT {
        return None;
    }

    let mut ms_list: Vec<f64> = guard
        .frametimes
        .iter()
        .map(|d| d.as_secs_f64() * 1000.0)
        .filter(|&ms| ms > 1.0 && ms < 1000.0)
        .collect();

    if ms_list.is_empty() {
        return None;
    }

    let valid_count = ms_list.len();
    let current_frametime_ms = (ms_list[0] * 100.0).round() / 100.0;
    let current_fps = if current_frametime_ms > 0.0 {
        let f = 1000.0 / current_frametime_ms;
        (f * 10.0).round() / 10.0
    } else {
        0.0
    };

    let sum_ms: f64 = ms_list.iter().sum();
    let avg_ms = sum_ms / valid_count as f64;
    let avg_fps = if avg_ms > 0.0 {
        let f = 1000.0 / avg_ms;
        (f * 10.0).round() / 10.0
    } else {
        0.0
    };

    // Standard Deviation and Coefficient of Variation (cv)
    let variance = ms_list
        .iter()
        .map(|&ms| {
            let diff = ms - avg_ms;
            diff * diff
        })
        .sum::<f64>()
        / valid_count as f64;
    let std_dev_ms = (variance.sqrt() * 100.0).round() / 100.0;
    let cv = if avg_ms > 0.0 {
        let raw_cv = std_dev_ms / avg_ms;
        (raw_cv * 1000.0).round() / 1000.0
    } else {
        0.0
    };

    // Determine target FPS: user configured or auto-detected bounded by active display refresh rate from Binder
    let max_device_rr = crate::platform::binder::get_active_display_refresh_rate();
    let target_fps = match guard.target_fps {
        Some(t) => {
            if max_device_rr > 0.0 {
                t.min(max_device_rr)
            } else {
                t
            }
        }
        None => {
            let candidate: f64 = match avg_fps {
                f if f > 130.0 => 144.0,
                f if f > 105.0 => 120.0,
                f if f > 75.0 => 90.0,
                _ => 60.0,
            };
            if max_device_rr > 0.0 {
                candidate.min(max_device_rr)
            } else {
                candidate
            }
        }
    };

    // Jank threshold: frame took > 1.35x expected deadline
    let deadline_ms = 1000.0 / target_fps;
    let jank_threshold = deadline_ms * 1.35;
    let jank_count = ms_list.iter().filter(|&&ms| ms >= jank_threshold).count() as u32;
    let jank_percent =
        ((jank_count as f64 / valid_count as f64) * 100.0 * 10.0).round() / 10.0;

    // 1% Low and 0.1% Low calculation
    ms_list.sort_by(|a, b| a.partial_cmp(b).unwrap_or(std::cmp::Ordering::Equal));
    let p99_idx = (((valid_count - 1) as f64 * 0.99).floor() as usize).min(valid_count - 1);
    let p99_ms = ms_list[p99_idx];
    let low_1pct_fps = if p99_ms > 0.0 {
        (1000.0 / p99_ms * 10.0).round() / 10.0
    } else {
        0.0
    };

    let p999_idx = (((valid_count - 1) as f64 * 0.999).floor() as usize).min(valid_count - 1);
    let p999_ms = ms_list[p999_idx];
    let low_01pct_fps = if p999_ms > 0.0 {
        (1000.0 / p999_ms * 10.0).round() / 10.0
    } else {
        0.0
    };

    let drop_cause = if jank_count > 0 || cv >= 0.10 {
        diagnose_drop_cause()
    } else {
        FrameDropCause::Smooth
    };

    drop(guard);

    Some(FpsReading {
        fps: current_fps,
        source: FpsSource::Ebpf,
        frametime_ms: current_frametime_ms,
        avg_fps,
        low_1pct_fps,
        low_01pct_fps,
        std_dev_ms,
        cv,
        jank_count,
        jank_percent,
        drop_cause,
    })
}
