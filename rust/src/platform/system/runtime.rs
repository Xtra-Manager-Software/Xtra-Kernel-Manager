use std::ffi::CStr;
use std::process::Command;

pub fn query_live_jvm_version() -> String {
    if let Ok(out) = Command::new("dalvikvm").arg("-version").output() {
        let text = String::from_utf8_lossy(&out.stdout);
        for line in text.lines() {
            let trimmed = line.trim();
            if trimmed.contains("version") {
                return trimmed.to_string();
            }
        }
    }
    String::new()
}

pub fn query_live_android_runtime() -> String {
    if let Ok(out) = Command::new("dalvikvm").arg("-showversion").output() {
        let text = String::from_utf8_lossy(&out.stdout);
        for line in text.lines() {
            let trimmed = line.trim();
            if trimmed.starts_with("ART") || trimmed.contains("Android Runtime") {
                return trimmed.to_string();
            }
        }
    }
    String::new()
}

pub fn query_live_webview_info() -> (String, String) {
    if let Ok(out) = Command::new("dumpsys").arg("webviewupdate").output() {
        let dump = String::from_utf8_lossy(&out.stdout);
        let mut pkg = String::new();
        let mut ver = String::new();

        for line in dump.lines() {
            let trimmed = line.trim();
            if trimmed.starts_with("Current WebView package") {
                if let Some(open) = trimmed.find('(') {
                    let close_opt = trimmed.find(')').filter(|&c| open < c);
                    if let Some(close) = close_opt {
                        pkg = trimmed[open + 1..close].trim().to_string();
                    }
                }
                if let Some(pos) = trimmed.find(", version ") {
                    ver = trimmed[pos + 10..].trim().trim_end_matches(')').to_string();
                }
            }
        }
        if !pkg.is_empty() {
            return (pkg, ver);
        }
    }
    (String::new(), String::new())
}

pub fn query_live_sqlite_version() -> String {
    let out_res = Command::new("sqlite3").arg(":memory:").arg("SELECT sqlite_version();").output();
    match out_res {
        Ok(out) if out.status.success() => String::from_utf8_lossy(&out.stdout).trim().to_string(),
        _ => String::new(),
    }
}

pub fn query_live_openssl_version() -> String {
    let lib_paths = [
        c"/system/lib64/libcrypto.so".as_ptr(),
        c"/apex/com.android.conscrypt/lib64/libcrypto.so".as_ptr(),
        c"libcrypto.so".as_ptr(),
    ];

    for path in lib_paths {
        let handle = unsafe { libc::dlopen(path, libc::RTLD_NOW) };
        if !handle.is_null() {
            let symbol = unsafe { libc::dlsym(handle, c"OpenSSL_version".as_ptr()) };
            if !symbol.is_null() {
                type FnOpenSslVersion = unsafe extern "C" fn(i32) -> *const std::ffi::c_char;
                let get_ver: FnOpenSslVersion = unsafe {
                    std::mem::transmute::<*mut libc::c_void, FnOpenSslVersion>(symbol)
                };
                let ptr = unsafe { get_ver(0) };
                if !ptr.is_null() {
                    let s = unsafe { CStr::from_ptr(ptr) }
                        .to_string_lossy()
                        .into_owned();
                    unsafe { libc::dlclose(handle) };
                    return s;
                }
            }
            unsafe { libc::dlclose(handle) };
        }
    }
    String::new()
}

pub fn query_live_toybox_version() -> String {
    if let Ok(out) = Command::new("toybox").arg("--version").output() {
        if out.status.success() {
            String::from_utf8_lossy(&out.stdout).trim().to_string()
        } else {
            String::new()
        }
    } else {
        String::new()
    }
}

pub fn query_live_gms_version() -> String {
    if let Ok(out) = Command::new("dumpsys")
        .args(["package", "com.google.android.gms"])
        .output()
    {
        let dump = String::from_utf8_lossy(&out.stdout);
        for line in dump.lines() {
            let trimmed = line.trim();
            if trimmed.starts_with("versionName=") {
                return trimmed.trim_start_matches("versionName=").to_string();
            }
        }
    }
    String::new()
}

pub fn query_live_art_version() -> String {
    query_live_android_runtime()
}

pub fn query_live_spatial_audio_and_head_tracking() -> (bool, bool) {
    let spatial_audio_supported = match Command::new("dumpsys").arg("audio").output() {
        Ok(out) => {
            let text = String::from_utf8_lossy(&out.stdout);
            text.contains("Spatializer supported: true") || text.contains("Spatializer: isAvailable: true")
        }
        Err(_) => false,
    };

    (spatial_audio_supported, false)
}

pub fn query_developer_options() -> bool {
    match Command::new("settings")
        .args(["get", "global", "development_settings_enabled"])
        .output()
    {
        Ok(out) => String::from_utf8_lossy(&out.stdout).trim() == "1",
        Err(_) => false,
    }
}
