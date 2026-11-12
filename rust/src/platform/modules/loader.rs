#[cfg(all(target_os = "android", target_arch = "aarch64"))]
use anyhow::bail;
use anyhow::{Context, Result, ensure};
use std::ffi::CString;
use std::fs::File;
#[cfg(all(target_os = "android", target_arch = "aarch64"))]
use std::os::unix::io::AsRawFd;
use std::path::Path;

use super::inventory::is_module_protected;

/// Load a kernel module (.ko) from disk with optional boot parameters.
pub fn load_kernel_module(file_path: &str, parameters: &str) -> Result<()> {
    let path = Path::new(file_path);
    ensure!(path.exists(), "Module file does not exist: {}", file_path);
    ensure!(
        path.extension().and_then(|ext| ext.to_str()) == Some("ko"),
        "Only .ko kernel module files are supported"
    );

    let file =
        File::open(path).with_context(|| format!("Failed to open module file: {}", file_path))?;
    let param_c = CString::new(parameters).context("Invalid parameter string")?;

    #[cfg(all(target_os = "android", target_arch = "aarch64"))]
    {
        // 1. Try finit_module (Syscall 273 on aarch64)
        let fd = file.as_raw_fd();
        let res = unsafe {
            libc::syscall(
                libc::SYS_finit_module,
                fd,
                param_c.as_ptr(),
                0 as libc::c_int,
            )
        };

        if res == 0 {
            return Ok(());
        }

        let err = std::io::Error::last_os_error();
        // Fallback to init_module if finit_module is not supported (ENOSYS)
        if err.raw_os_error() == Some(libc::ENOSYS) {
            use std::io::Read;
            let mut buffer = Vec::new();
            let mut f = file;
            f.read_to_end(&mut buffer)?;

            let init_res = unsafe {
                libc::syscall(
                    libc::SYS_init_module,
                    buffer.as_ptr(),
                    buffer.len(),
                    param_c.as_ptr(),
                )
            };

            if init_res == 0 {
                return Ok(());
            }
            bail!("init_module failed: {}", std::io::Error::last_os_error());
        }

        bail!("finit_module failed: {}", err);
    }

    #[cfg(not(all(target_os = "android", target_arch = "aarch64")))]
    {
        let _ = (file, param_c);
        Ok(())
    }
}

/// Safely unload a loaded kernel module by name.
pub fn unload_kernel_module(name: &str) -> Result<()> {
    let trimmed = name.trim();
    ensure!(!trimmed.is_empty(), "Module name cannot be empty");
    ensure!(
        !is_module_protected(trimmed),
        "Module '{}' is protected and cannot be unloaded (root/system stability guard)",
        trimmed
    );

    let name_c = CString::new(trimmed).context("Invalid module name")?;

    #[cfg(all(target_os = "android", target_arch = "aarch64"))]
    {
        // delete_module (Syscall 106 on aarch64) with O_NONBLOCK (0x800)
        let res = unsafe {
            libc::syscall(
                libc::SYS_delete_module,
                name_c.as_ptr(),
                libc::c_long::from(libc::O_NONBLOCK),
            )
        };

        if res == 0 {
            return Ok(());
        }

        let err = std::io::Error::last_os_error();
        bail!("delete_module failed for '{}': {}", trimmed, err);
    }

    #[cfg(not(all(target_os = "android", target_arch = "aarch64")))]
    {
        let _ = name_c;
        Ok(())
    }
}
