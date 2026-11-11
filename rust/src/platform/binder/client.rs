use std::fs;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ForegroundAppInfo {
    pub package_name: String,
    pub pid: Option<i32>,
}

/// Query foreground package using Native Binder `activity_task` IPC with Linux cgroup top-app fallback
pub fn get_foreground_app() -> Option<ForegroundAppInfo> {
    // 1. Try Native Binder IPC via activity_task (getFocusedRootTaskInfo)
    #[cfg(target_os = "android")]
    {
        if crate::platform::binder::ensure_initialized() {
            if let Some(atm) = rsbinder::hub::check_service("activity_task") {
                if let Some(proxy) = atm.as_proxy() {
                    if let Ok(data) = proxy.prepare_transact(true) {
                        // Transaction 30: getFocusedRootTaskInfo
                        if let Ok(Some(mut reply)) = proxy.submit_transact(30, &data, 0) {
                            if reply.data_size() >= 0x28 {
                                if let Ok(0) = reply.read_i32() {
                                    if let Ok(1) = reply.read_i32() {
                                        reply.set_data_position(0x24);
                                        if let Ok(1) = reply.read_i32() {
                                            if let Ok(Some(comp_str)) = reply.read::<Option<String>>() {
                                                let pkg = comp_str.split('/').next().unwrap_or(&comp_str).trim();
                                                if !pkg.is_empty() && !pkg.contains("systemui") && !pkg.contains("launcher") {
                                                    let pid = find_pid_by_package(pkg);
                                                    return Some(ForegroundAppInfo {
                                                        package_name: pkg.to_string(),
                                                        pid,
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 2. Fall back to Linux cgroup top-app & /proc without permissions
    let cgroup_paths = [
        "/dev/cpuset/top-app/cgroup.procs",
        "/sys/fs/cgroup/top-app/cgroup.procs",
        "/sys/fs/cgroup/uid_0/cgroup.procs",
    ];

    for path in cgroup_paths {
        if let Ok(content) = fs::read_to_string(path) {
            for line in content.lines().rev() {
                if let Ok(pid) = line.trim().parse::<i32>() {
                    if pid <= 2000 {
                        continue; // Skip system_server and init
                    }

                    if let Some(pkg) = get_pkg_from_cmdline(pid) {
                        if !pkg.starts_with("/system")
                            && !pkg.starts_with("sh")
                            && !pkg.contains("inputmethod")
                            && !pkg.contains("systemui")
                            && !pkg.is_empty()
                        {
                            return Some(ForegroundAppInfo {
                                package_name: pkg,
                                pid: Some(pid),
                            });
                        }
                    }
                }
            }
        }
    }

    None
}

fn get_pkg_from_cmdline(pid: i32) -> Option<String> {
    let cmdline_path = format!("/proc/{}/cmdline", pid);
    let bytes = fs::read(cmdline_path).ok()?;
    let first_null = bytes.iter().position(|&b| b == 0).unwrap_or(bytes.len());
    let raw = std::str::from_utf8(&bytes[..first_null]).ok()?;
    let pkg = raw.split(':').next()?.trim();
    match pkg.is_empty() {
        true => None,
        false => Some(pkg.to_string()),
    }
}

#[cfg(target_os = "android")]
fn find_pid_by_package(package_name: &str) -> Option<i32> {
    let entries = fs::read_dir("/proc").ok()?;
    for entry in entries.flatten() {
        let name = entry.file_name();
        let name_str = name.to_str()?;
        if let Ok(pid) = name_str.parse::<i32>() {
            if let Some(cmd) = get_pkg_from_cmdline(pid) {
                if cmd == package_name {
                    return Some(pid);
                }
            }
        }
    }
    None
}
