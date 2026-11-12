use anyhow::Result;
use serde::Serialize;
use std::fs;

#[derive(Debug, Clone, Serialize)]
pub struct KernelCrashEntry {
    pub tag: String,
    pub timestamp_ms: u64,
    pub summary: String,
    pub source: String,
}

/// Check if the device previously experienced a kernel panic, watchdog reset, or unexpected reboot.
pub fn check_recent_kernel_crashes() -> Result<Vec<KernelCrashEntry>> {
    let mut entries = Vec::new();

    // 1. Check kernel pstore / ramoops
    if let Ok(dir_entries) = fs::read_dir("/sys/fs/pstore") {
        for entry in dir_entries.flatten() {
            let file_name = entry.file_name().to_string_lossy().to_string();
            if file_name.starts_with("console-ramoops") || file_name.starts_with("dmesg-ramoops") {
                let metadata = entry.metadata().ok();
                let mtime = metadata
                    .and_then(|m| m.modified().ok())
                    .and_then(|t| t.duration_since(std::time::UNIX_EPOCH).ok())
                    .map(|d| d.as_millis() as u64)
                    .unwrap_or(0);

                let snippet = fs::read_to_string(entry.path())
                    .ok()
                    .map(|s| {
                        s.lines()
                            .filter(|l| {
                                l.contains("panic") || l.contains("Oops") || l.contains("BUG:")
                            })
                            .take(3)
                            .collect::<Vec<&str>>()
                            .join(" | ")
                    })
                    .unwrap_or_default();

                entries.push(KernelCrashEntry {
                    tag: "KERNEL_PANIC_PSTORE".to_string(),
                    timestamp_ms: mtime,
                    summary: if snippet.is_empty() {
                        format!("Kernel ramoops log detected: {}", file_name)
                    } else {
                        snippet
                    },
                    source: "pstore".to_string(),
                });
            }
        }
    }

    // 2. Check /data/system/dropbox for panic markers
    if let Ok(dir_entries) = fs::read_dir("/data/system/dropbox") {
        for entry in dir_entries.flatten() {
            let name = entry.file_name().to_string_lossy().to_string();
            if name.contains("SYSTEM_PANIC")
                || name.contains("KERNEL_PANIC")
                || name.contains("SYSTEM_RESTART")
                || name.contains("watchdog")
            {
                let mtime = entry
                    .metadata()
                    .ok()
                    .and_then(|m| m.modified().ok())
                    .and_then(|t| t.duration_since(std::time::UNIX_EPOCH).ok())
                    .map(|d| d.as_millis() as u64)
                    .unwrap_or(0);

                entries.push(KernelCrashEntry {
                    tag: name.split('@').next().unwrap_or(&name).to_string(),
                    timestamp_ms: mtime,
                    summary: format!("Dropbox entry: {}", name),
                    source: "dropbox".to_string(),
                });
            }
        }
    }

    Ok(entries)
}
