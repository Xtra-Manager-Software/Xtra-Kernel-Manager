use super::types::{FpsReading, FpsSource};
use std::fs;
use std::path::Path;
use std::time::{Duration, Instant};

pub const FPS_SYSFS_PATHS: &[&str] = &[
    "/sys/class/drm/sde-crtc-0/measured_fps",
    "/sys/class/drm/card0/sde-crtc-0/measured_fps",
    "/sys/class/drm/card0/sde_crtc_fps",
    "/sys/class/drm/card0/fbc/fps",
    "/sys/class/graphics/fb0/measured_fps",
    "/sys/class/graphics/fb0/fps",
    "/sys/kernel/debug/mali/fps",
    "/sys/class/misc/mali0/device/fps",
];

pub const SYSFS_POLL_INTERVAL: Duration = Duration::from_millis(250);

pub struct SysfsFpsReader {
    pub path: Option<String>,
    pub last_poll: Instant,
    pub cached: Option<FpsReading>,
}

impl Default for SysfsFpsReader {
    fn default() -> Self {
        Self::new()
    }
}

impl SysfsFpsReader {
    pub fn new() -> Self {
        Self {
            path: Self::detect_sysfs(),
            last_poll: Instant::now()
                .checked_sub(SYSFS_POLL_INTERVAL)
                .unwrap_or_else(Instant::now),
            cached: None,
        }
    }

    pub fn detect_sysfs() -> Option<String> {
        for &p in FPS_SYSFS_PATHS {
            if Path::new(p).exists()
                && let Ok(content) = fs::read_to_string(p)
                && !content.trim().is_empty()
            {
                return Some(p.to_string());
            }
        }
        None
    }

    pub fn read(&mut self) -> Option<FpsReading> {
        let now = Instant::now();
        if self.path.is_none() {
            self.path = Self::detect_sysfs();
        }

        if self.cached.is_some() && now.duration_since(self.last_poll) < SYSFS_POLL_INTERVAL {
            return self.cached;
        }

        self.last_poll = now;
        let path = self.path.as_ref()?;
        let content = fs::read_to_string(path).ok()?;
        let fps = match parse_sysfs_fps(&content) {
            Some(f) => f,
            None => self.cached?.fps,
        };

        let reading = FpsReading::new(fps, FpsSource::Sysfs);
        self.cached = Some(reading);
        Some(reading)
    }

    pub fn clear(&mut self) {
        self.cached = None;
        self.last_poll = Instant::now()
            .checked_sub(SYSFS_POLL_INTERVAL)
            .unwrap_or_else(Instant::now);
    }
}

/// Zero-allocation sysfs FPS parser supporting raw floats ("60.0") and
/// vendor key-value formats ("fps: 117.7 duration:1000000 frame_count:118", "FPS: 60").
pub fn parse_sysfs_fps(content: &str) -> Option<f64> {
    let trimmed = content.trim();
    if trimmed.is_empty() {
        return None;
    }
    if let Ok(v) = trimmed.parse::<f64>() {
        return (0.0..=500.0).contains(&v).then_some(v);
    }

    let mut tokens = trimmed
        .split(|c: char| c.is_whitespace() || c == ',' || c == ';')
        .map(str::trim)
        .filter(|s| !s.is_empty());

    let parse_val = |candidate: &str| {
        candidate
            .parse::<f64>()
            .ok()
            .filter(|v| (0.0..=500.0).contains(v))
    };

    while let Some(token) = tokens.next() {
        let candidate =
            match strip_case_prefix(token, "fps:").or_else(|| strip_case_prefix(token, "fps=")) {
                Some(val_str) => {
                    let val_clean = val_str.trim();
                    match val_clean.is_empty() {
                        false => Some(val_clean),
                        true => tokens.next(),
                    }
                }
                None => match token.eq_ignore_ascii_case("fps") {
                    true => tokens.next(),
                    false => None,
                },
            };

        if let Some(v) = candidate.and_then(parse_val) {
            return Some(v);
        }
    }

    None
}

#[inline]
fn strip_case_prefix<'a>(s: &'a str, prefix: &str) -> Option<&'a str> {
    if s.len() >= prefix.len()
        && s.is_char_boundary(prefix.len())
        && s[..prefix.len()].eq_ignore_ascii_case(prefix)
    {
        Some(&s[prefix.len()..])
    } else {
        None
    }
}
