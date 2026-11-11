use anyhow::{Context, Result};
use std::fs;
use std::path::Path;

#[inline]
pub fn read_sysfs_trimmed(path: &str) -> Option<String> {
    fs::read_to_string(path).ok().map(|s| s.trim().to_string())
}

#[inline]
pub fn read_sysfs_i64(path: &str) -> Option<i64> {
    read_sysfs_trimmed(path)?.parse::<i64>().ok()
}

#[inline]
pub fn read_sysfs_u64(path: &str) -> Option<u64> {
    read_sysfs_trimmed(path)?.parse::<u64>().ok()
}

#[inline]
pub fn read_sysfs_f32(path: &str) -> Option<f32> {
    read_sysfs_trimmed(path)?.parse::<f32>().ok()
}

#[inline]
pub fn write_sysfs(path: &str, value: &str) -> Result<()> {
    fs::write(path, value).with_context(|| format!("Failed to write '{value}' to {path}"))
}

#[inline]
pub fn get_system_property(key: &str) -> Option<String> {
    #[cfg(target_os = "android")]
    {
        android_properties::getprop(key).value()
    }
    #[cfg(not(target_os = "android"))]
    {
        let _ = key;
        None
    }
}

#[inline]
pub fn path_exists(path: &str) -> bool {
    Path::new(path).exists()
}
