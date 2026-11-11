use std::fs;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ForegroundAppInfo {
    pub package_name: String,
    pub pid: Option<i32>,
}

/// Query foreground package using Linux cgroup top-app & /proc without Accessibility permissions
pub fn get_foreground_app() -> Option<ForegroundAppInfo> {
    // 1. Check top-app cgroup paths (Android cgroup v1 and v2)
    let cgroup_paths = [
        "/dev/cpuset/top-app/cgroup.procs",
        "/sys/fs/cgroup/top-app/cgroup.procs",
        "/sys/fs/cgroup/uid_0/cgroup.procs",
    ];

    for path in cgroup_paths {
        if let Ok(content) = fs::read_to_string(path) {
            // Find non-system, non-shell user apps in top-app
            for line in content.lines().rev() {
                if let Ok(pid) = line.trim().parse::<i32>() {
                    if pid <= 2000 {
                        continue; // Skip system_server (usually PID ~1000..2000) and init
                    }

                    if let Some(pkg) = get_pkg_from_cmdline(pid) {
                        // Filter out common system daemons/keyboard
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
    if pkg.is_empty() {
        None
    } else {
        Some(pkg.to_string())
    }
}
