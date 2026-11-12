use std::ffi::CStr;

pub type ClPlatformId = *mut std::ffi::c_void;
pub type ClDeviceId = *mut std::ffi::c_void;
pub type ClUint = u32;
pub type ClInt = i32;
pub type ClDeviceInfo = u32;

pub const CL_DEVICE_TYPE_GPU: u64 = 1 << 2;
pub const CL_DEVICE_TYPE_ALL: u64 = 0xFFFFFFFF;

pub const CL_DEVICE_MAX_COMPUTE_UNITS: ClDeviceInfo = 0x1002;
pub const CL_DEVICE_GLOBAL_MEM_CACHE_SIZE: ClDeviceInfo = 0x101E;
pub const CL_DEVICE_LOCAL_MEM_SIZE: ClDeviceInfo = 0x1023;
pub const CL_DEVICE_NAME: ClDeviceInfo = 0x102B;

type FnClGetPlatformIDs = unsafe extern "C" fn(
    num_entries: ClUint,
    platforms: *mut ClPlatformId,
    num_platforms: *mut ClUint,
) -> ClInt;

type FnClGetDeviceIDs = unsafe extern "C" fn(
    platform: ClPlatformId,
    device_type: u64,
    num_entries: ClUint,
    devices: *mut ClDeviceId,
    num_devices: *mut ClUint,
) -> ClInt;

type FnClGetDeviceInfo = unsafe extern "C" fn(
    device: ClDeviceId,
    param_name: ClDeviceInfo,
    param_value_size: usize,
    param_value: *mut std::ffi::c_void,
    param_value_size_ret: *mut usize,
) -> ClInt;

#[derive(Debug, Clone, Default)]
pub struct OpenClGpuHardware {
    pub device_name: String,
    pub compute_units: u32,
    pub on_chip_memory: String,
}

/// # Safety
/// Dynamically probes OpenCL hardware capabilities directly via libOpenCL.so.
/// All pointers are validated, checked for null, and closed safely.
pub fn probe_opencl_hardware() -> Option<OpenClGpuHardware> {
    let lib_paths = [
        c"/vendor/lib64/libOpenCL.so".as_ptr(),
        c"/system/vendor/lib64/libOpenCL.so".as_ptr(),
        c"/system/lib64/libOpenCL.so".as_ptr(),
        c"libOpenCL.so".as_ptr(),
    ];

    let mut handle = std::ptr::null_mut();
    for path in lib_paths {
        // Safety: Path is a valid null-terminated C string pointer.
        let h = unsafe { libc::dlopen(path, libc::RTLD_NOW) };
        if !h.is_null() {
            handle = h;
            break;
        }
    }

    if handle.is_null() {
        return None;
    }

    // Safety: Load function pointers from dynamic library
    unsafe {
        let sym_get_platforms = libc::dlsym(handle, c"clGetPlatformIDs".as_ptr());
        let sym_get_devices = libc::dlsym(handle, c"clGetDeviceIDs".as_ptr());
        let sym_get_info = libc::dlsym(handle, c"clGetDeviceInfo".as_ptr());

        if sym_get_platforms.is_null() || sym_get_devices.is_null() || sym_get_info.is_null() {
            libc::dlclose(handle);
            return None;
        }

        let cl_get_platforms: FnClGetPlatformIDs =
            std::mem::transmute::<*mut libc::c_void, FnClGetPlatformIDs>(sym_get_platforms);
        let cl_get_devices: FnClGetDeviceIDs =
            std::mem::transmute::<*mut libc::c_void, FnClGetDeviceIDs>(sym_get_devices);
        let cl_get_info: FnClGetDeviceInfo =
            std::mem::transmute::<*mut libc::c_void, FnClGetDeviceInfo>(sym_get_info);

        let mut num_platforms: ClUint = 0;
        let mut platform: ClPlatformId = std::ptr::null_mut();
        if cl_get_platforms(1, &mut platform, &mut num_platforms) != 0 || num_platforms == 0 {
            libc::dlclose(handle);
            return None;
        }

        let mut num_devices: ClUint = 0;
        let mut device: ClDeviceId = std::ptr::null_mut();
        let mut res = cl_get_devices(
            platform,
            CL_DEVICE_TYPE_GPU,
            1,
            &mut device,
            &mut num_devices,
        );
        if res != 0 || num_devices == 0 {
            // Fallback to all devices if GPU flag wasn't explicitly flagged
            res = cl_get_devices(
                platform,
                CL_DEVICE_TYPE_ALL,
                1,
                &mut device,
                &mut num_devices,
            );
            if res != 0 || num_devices == 0 {
                libc::dlclose(handle);
                return None;
            }
        }

        let mut cu: u32 = 0;
        let cu_res = cl_get_info(
            device,
            CL_DEVICE_MAX_COMPUTE_UNITS,
            std::mem::size_of::<u32>(),
            &mut cu as *mut _ as *mut _,
            std::ptr::null_mut(),
        );

        let mut local_mem_bytes: u64 = 0;
        let mem_res = cl_get_info(
            device,
            CL_DEVICE_LOCAL_MEM_SIZE,
            std::mem::size_of::<u64>(),
            &mut local_mem_bytes as *mut _ as *mut _,
            std::ptr::null_mut(),
        );

        let mut cache_bytes: u64 = 0;
        let cache_res = cl_get_info(
            device,
            CL_DEVICE_GLOBAL_MEM_CACHE_SIZE,
            std::mem::size_of::<u64>(),
            &mut cache_bytes as *mut _ as *mut _,
            std::ptr::null_mut(),
        );

        let mut name_buf = [0u8; 256];
        let name_res = cl_get_info(
            device,
            CL_DEVICE_NAME,
            name_buf.len(),
            name_buf.as_mut_ptr() as *mut _,
            std::ptr::null_mut(),
        );

        libc::dlclose(handle);

        let compute_units = if cu_res == 0 { cu } else { 0 };

        let mem_bytes_effective = match () {
            _ if cache_res == 0 && cache_bytes > 0 => cache_bytes,
            _ if mem_res == 0 && local_mem_bytes > 0 => local_mem_bytes,
            _ => 0,
        };

        let on_chip_memory = match mem_bytes_effective {
            b if b >= 1024 * 1024 => format!("{:.2} MB", (b as f64) / (1024.0 * 1024.0)),
            b if b > 0 => format!("{} KB", b / 1024),
            _ => String::new(),
        };

        let device_name = if name_res == 0 {
            CStr::from_ptr(name_buf.as_ptr() as *const _)
                .to_string_lossy()
                .into_owned()
        } else {
            String::new()
        };

        Some(OpenClGpuHardware {
            device_name,
            compute_units,
            on_chip_memory,
        })
    }
}
