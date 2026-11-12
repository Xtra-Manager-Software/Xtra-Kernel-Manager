use std::ffi::CStr;

use super::types::CdmInfo;

type AMediaDrm = std::ffi::c_void;
type AMediaStatus = i32;

type FnAMediaDrmCreateByUuid = unsafe extern "C" fn(uuid: *const u8) -> *mut AMediaDrm;
type FnAMediaDrmRelease = unsafe extern "C" fn(drm: *mut AMediaDrm);
type FnAMediaDrmGetPropertyString = unsafe extern "C" fn(
    drm: *mut AMediaDrm,
    property_name: *const std::ffi::c_char,
    property_value: *mut *const std::ffi::c_char,
) -> AMediaStatus;

/// Query Widevine and ClearKey CDM properties live using Android NDK AMediaDrm API.
pub fn query_mediadrm_cdms() -> (Option<CdmInfo>, Option<CdmInfo>) {
    let lib_paths = [
        c"/system/lib64/libmediandk.so".as_ptr(),
        c"libmediandk.so".as_ptr(),
    ];

    let mut lib = std::ptr::null_mut();
    for path in lib_paths {
        let h = unsafe { libc::dlopen(path, libc::RTLD_NOW) };
        if !h.is_null() {
            lib = h;
            break;
        }
    }

    if lib.is_null() {
        return (None, None);
    }

    let create_by_uuid: FnAMediaDrmCreateByUuid = unsafe {
        match libc::dlsym(lib, c"AMediaDrm_createByUUID".as_ptr()) {
            p if !p.is_null() => std::mem::transmute::<*mut libc::c_void, FnAMediaDrmCreateByUuid>(p),
            _ => {
                libc::dlclose(lib);
                return (None, None);
            }
        }
    };

    let release_drm: FnAMediaDrmRelease = unsafe {
        match libc::dlsym(lib, c"AMediaDrm_release".as_ptr()) {
            p if !p.is_null() => std::mem::transmute::<*mut libc::c_void, FnAMediaDrmRelease>(p),
            _ => {
                libc::dlclose(lib);
                return (None, None);
            }
        }
    };

    let get_prop_str: FnAMediaDrmGetPropertyString = unsafe {
        match libc::dlsym(lib, c"AMediaDrm_getPropertyString".as_ptr()) {
            p if !p.is_null() => {
                std::mem::transmute::<*mut libc::c_void, FnAMediaDrmGetPropertyString>(p)
            }
            _ => {
                libc::dlclose(lib);
                return (None, None);
            }
        }
    };

    let query_single_cdm = |name: &str, uuid: [u8; 16]| -> Option<CdmInfo> {
        let drm = unsafe { create_by_uuid(uuid.as_ptr()) };
        if drm.is_null() {
            None
        } else {
            let fetch_prop = |prop_name: &CStr| -> Option<String> {
                let mut val_ptr: *const std::ffi::c_char = std::ptr::null();
                let res = unsafe { get_prop_str(drm, prop_name.as_ptr(), &mut val_ptr) };
                if res == 0 && !val_ptr.is_null() {
                    unsafe { Some(CStr::from_ptr(val_ptr).to_string_lossy().into_owned()) }
                } else {
                    None
                }
            };

            let info = CdmInfo {
                name: name.to_string(),
                vendor: fetch_prop(c"vendor"),
                version: fetch_prop(c"version"),
                description: fetch_prop(c"description"),
                security_level: fetch_prop(c"securityLevel"),
                system_id: fetch_prop(c"systemId"),
                max_hdcp_level: fetch_prop(c"maxHdcpLevel"),
                current_hdcp_level: fetch_prop(c"hdcpLevel"),
                max_sessions: fetch_prop(c"maxNumberOfSessions"),
                oem_crypto_api_version: fetch_prop(c"oemCryptoApiVersion"),
            };

            unsafe { release_drm(drm) };
            Some(info)
        }
    };

    // Widevine
    let widevine_uuid: [u8; 16] = [
        0xed, 0xef, 0x8b, 0xa9, 0x79, 0xd6, 0x4a, 0xce, 0xa3, 0xc8, 0x27, 0xdc, 0xd5, 0x1d, 0x21,
        0xed,
    ];

    // ClearKey
    let clearkey_uuid: [u8; 16] = [
        0xe2, 0x71, 0x9d, 0x58, 0xa9, 0x85, 0xb3, 0xc9, 0x78, 0x1a, 0xb0, 0x30, 0xe4, 0xd3, 0x0d,
        0x06,
    ];

    let wv = query_single_cdm("Widevine CDM", widevine_uuid);
    let ck = query_single_cdm("ClearKey CDM", clearkey_uuid);

    unsafe { libc::dlclose(lib) };
    (wv, ck)
}
