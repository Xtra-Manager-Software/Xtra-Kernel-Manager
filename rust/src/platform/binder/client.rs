use std::fs;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ForegroundAppInfo {
    pub package_name: String,
    pub pid: Option<i32>,
}

#[cfg(target_os = "android")]
fn get_foreground_app_binder() -> Option<ForegroundAppInfo> {
    if !crate::platform::binder::ensure_initialized() {
        return None;
    }
    let atm = rsbinder::hub::check_service("activity_task")?;
    let proxy = atm.as_proxy()?;
    let data = proxy.prepare_transact(true).ok()?;
    // Transaction 30: getFocusedRootTaskInfo
    let mut reply = proxy.submit_transact(30, &data, 0).ok()??;
    if reply.data_size() < 0x28 {
        return None;
    }
    if reply.read_i32().ok()? != 0 || reply.read_i32().ok()? != 1 {
        return None;
    }
    reply.set_data_position(0x24);
    if reply.read_i32().ok()? != 1 {
        return None;
    }
    let comp_str = reply.read::<Option<String>>().ok()??;
    let pkg = comp_str.split('/').next().unwrap_or(&comp_str).trim();
    if !pkg.is_empty() && !pkg.contains("systemui") && !pkg.contains("launcher") {
        let pid = find_pid_by_package(pkg);
        return Some(ForegroundAppInfo {
            package_name: pkg.to_string(),
            pid,
        });
    }
    None
}

fn get_foreground_app_cgroups() -> Option<ForegroundAppInfo> {
    let cgroup_paths = [
        "/dev/cpuset/top-app/cgroup.procs",
        "/sys/fs/cgroup/top-app/cgroup.procs",
        "/sys/fs/cgroup/uid_0/cgroup.procs",
    ];

    for path in cgroup_paths {
        let Ok(content) = fs::read_to_string(path) else {
            continue;
        };
        for line in content.lines().rev() {
            let Ok(pid) = line.trim().parse::<i32>() else {
                continue;
            };
            if pid <= 2000 {
                continue; // Skip system_server and init
            }

            if let Some(pkg) = get_pkg_from_cmdline(pid) {
                let is_ignored = pkg.is_empty()
                    || pkg.starts_with("/system")
                    || pkg.starts_with("sh")
                    || pkg.contains("inputmethod")
                    || pkg.contains("systemui");
                if !is_ignored {
                    return Some(ForegroundAppInfo {
                        package_name: pkg,
                        pid: Some(pid),
                    });
                }
            }
        }
    }
    None
}

/// Query foreground package using Native Binder `activity_task` IPC with Linux cgroup top-app fallback
pub fn get_foreground_app() -> Option<ForegroundAppInfo> {
    #[cfg(target_os = "android")]
    {
        if let Some(info) = get_foreground_app_binder() {
            return Some(info);
        }
    }

    get_foreground_app_cgroups()
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
