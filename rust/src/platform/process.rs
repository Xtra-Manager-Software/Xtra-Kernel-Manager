use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use std::fs;
use std::path::PathBuf;

#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct ProcessItem {
    pub pid: i32,
    pub uid: u32,
    pub process_name: String,
    pub package_name: String,
    pub rss_kb: u64,
    pub state: char,
    pub cgroup: String,
}

/// Scan running processes natively from /proc without spawning subshells or companion APKs.
pub fn get_running_process_list() -> Result<Vec<ProcessItem>> {
    let mut list = Vec::with_capacity(128);
    let proc_dir = fs::read_dir("/proc").context("Cannot read /proc")?;

    let mut path_buf = PathBuf::with_capacity(64);

    for entry in proc_dir.flatten() {
        let file_name = entry.file_name();
        let Some(name_str) = file_name.to_str() else {
            continue;
        };

        let Ok(pid) = name_str.parse::<i32>() else {
            continue;
        };

        if pid <= 100 {
            continue; // Skip kernel threads
        }

        path_buf.clear();
        path_buf.push("/proc");
        path_buf.push(name_str);

        // 1. Process / Package Name from cmdline
        path_buf.push("cmdline");
        let cmdline_bytes = fs::read(&path_buf).unwrap_or_default();
        path_buf.pop();

        let first_null = cmdline_bytes
            .iter()
            .position(|&b| b == 0)
            .unwrap_or(cmdline_bytes.len());
        let raw_name = std::str::from_utf8(&cmdline_bytes[..first_null]).unwrap_or("");
        if raw_name.is_empty() {
            continue;
        }

        let process_name = raw_name.to_string();
        let package_name = match raw_name.split_once(':') {
            Some((pkg, _)) => pkg.to_string(),
            None => raw_name.to_string(),
        };

        // 2. UID, State, RSS from status
        path_buf.push("status");
        let (mut uid, mut rss_kb, mut state) = (0u32, 0u64, 'S');

        if let Ok(status_str) = fs::read_to_string(&path_buf) {
            for line in status_str.lines() {
                match () {
                    _ if line.starts_with("Uid:") => {
                        if let Some(first_uid) = line["Uid:".len()..].split_whitespace().next() {
                            uid = first_uid.parse().unwrap_or(0);
                        }
                    }
                    _ if line.starts_with("State:") => {
                        if let Some(s) = line["State:".len()..].trim().chars().next() {
                            state = s;
                        }
                    }
                    _ if line.starts_with("VmRSS:") => {
                        if let Some(kb) = line["VmRSS:".len()..].split_whitespace().next() {
                            rss_kb = kb.parse().unwrap_or(0);
                        }
                    }
                    _ => {}
                }
            }
        }
        path_buf.pop();

        // 3. Cgroup (top-app, foreground, background)
        path_buf.push("cgroup");
        let mut cgroup = "default".to_string();
        if let Ok(cg_str) = fs::read_to_string(&path_buf) {
            for line in cg_str.lines() {
                if line.contains("cpuset") {
                    let mut parts = line.split(':');
                    if let Some(grp) = parts.nth(2) {
                        let trimmed = grp.trim_start_matches('/');
                        if !trimmed.is_empty() {
                            cgroup = trimmed.to_string();
                            break;
                        }
                    }
                }
            }
        }
        path_buf.pop();

        list.push(ProcessItem {
            pid,
            uid,
            process_name,
            package_name,
            rss_kb,
            state,
            cgroup,
        });
    }

    // Sort by RSS memory descending
    list.sort_unstable_by_key(|a| std::cmp::Reverse(a.rss_kb));
    Ok(list)
}
