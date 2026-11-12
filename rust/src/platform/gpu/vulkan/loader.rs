use std::ffi::CStr;

use crate::platform::gpu::types::VulkanCapabilities;
use super::props::query_physical_device_capabilities;
use super::structs::*;

/// # Safety
/// Dynamically queries Vulkan driver capabilities via libvulkan.so.
pub fn query_vulkan_driver_live() -> VulkanCapabilities {
    let lib_paths = [
        c"/system/lib64/libvulkan.so".as_ptr(),
        c"libvulkan.so".as_ptr(),
        c"libvulkan.so.1".as_ptr(),
    ];

    let mut handle = std::ptr::null_mut();
    for path in lib_paths {
        // Safety: Path is a valid C string.
        let h = unsafe { libc::dlopen(path, libc::RTLD_NOW) };
        if !h.is_null() {
            handle = h;
            break;
        }
    }

    if handle.is_null() {
        return VulkanCapabilities::default();
    }

    // Safety: Load symbols from Vulkan driver
    unsafe {
        type FnVkCreateInstance = unsafe extern "system" fn(
            *const VkInstanceCreateInfo,
            *const std::ffi::c_void,
            *mut VkInstance,
        ) -> VkResult;

        type FnVkDestroyInstance =
            unsafe extern "system" fn(VkInstance, *const std::ffi::c_void);

        type FnVkEnumeratePhysicalDevices =
            unsafe extern "system" fn(VkInstance, *mut u32, *mut VkPhysicalDevice) -> VkResult;

        type FnVkEnumerateInstanceVersion = unsafe extern "system" fn(*mut u32) -> VkResult;

        type FnVkEnumerateInstanceExtensionProperties = unsafe extern "system" fn(
            *const std::ffi::c_char,
            *mut u32,
            *mut VkExtensionProperties,
        ) -> VkResult;

        let vk_create_instance: Option<FnVkCreateInstance> = std::mem::transmute::<
            *mut libc::c_void,
            Option<FnVkCreateInstance>,
        >(libc::dlsym(handle, c"vkCreateInstance".as_ptr()));

        let vk_destroy_instance: Option<FnVkDestroyInstance> = std::mem::transmute::<
            *mut libc::c_void,
            Option<FnVkDestroyInstance>,
        >(libc::dlsym(handle, c"vkDestroyInstance".as_ptr()));

        let vk_enumerate_physical_devices: Option<FnVkEnumeratePhysicalDevices> =
            std::mem::transmute::<*mut libc::c_void, Option<FnVkEnumeratePhysicalDevices>>(
                libc::dlsym(handle, c"vkEnumeratePhysicalDevices".as_ptr()),
            );

        let vk_enumerate_instance_version: Option<FnVkEnumerateInstanceVersion> =
            std::mem::transmute::<*mut libc::c_void, Option<FnVkEnumerateInstanceVersion>>(
                libc::dlsym(handle, c"vkEnumerateInstanceVersion".as_ptr()),
            );

        let vk_enumerate_instance_extension_properties: Option<
            FnVkEnumerateInstanceExtensionProperties,
        > = std::mem::transmute::<*mut libc::c_void, Option<FnVkEnumerateInstanceExtensionProperties>>(
            libc::dlsym(handle, c"vkEnumerateInstanceExtensionProperties".as_ptr()),
        );

        let mut instance_ver: u32 = 0x00400000; // Vulkan 1.0.0
        if let Some(get_ver) = vk_enumerate_instance_version {
            get_ver(&mut instance_ver);
        }

        let mut instance_ext_count: u32 = 0;
        let mut instance_ext_names = Vec::new();
        if let Some(get_ext) = vk_enumerate_instance_extension_properties {
            get_ext(
                std::ptr::null(),
                &mut instance_ext_count,
                std::ptr::null_mut(),
            );
            if instance_ext_count > 0 {
                let mut exts = vec![std::mem::zeroed(); instance_ext_count as usize];
                get_ext(
                    std::ptr::null(),
                    &mut instance_ext_count,
                    exts.as_mut_ptr(),
                );
                for e in exts {
                    let name = CStr::from_ptr(e.extension_name.as_ptr())
                        .to_string_lossy()
                        .into_owned();
                    instance_ext_names.push(name);
                }
            }
        }

        let app_name = c"XKM".as_ptr();
        let app_info = VkApplicationInfo {
            s_type: 0,
            p_next: std::ptr::null(),
            p_application_name: app_name,
            application_version: 1,
            p_engine_name: app_name,
            engine_version: 1,
            api_version: instance_ver,
        };

        let create_info = VkInstanceCreateInfo {
            s_type: 1,
            p_next: std::ptr::null(),
            flags: 0,
            p_application_info: &app_info,
            enabled_layer_count: 0,
            pp_enabled_layer_names: std::ptr::null(),
            enabled_extension_count: 0,
            pp_enabled_extension_names: std::ptr::null(),
        };

        let mut instance: VkInstance = std::ptr::null_mut();
        if let Some(create_fn) = vk_create_instance {
            let res = create_fn(&create_info, std::ptr::null(), &mut instance);
            if res != 0 || instance.is_null() {
                libc::dlclose(handle);
                return VulkanCapabilities::default();
            }
        } else {
            libc::dlclose(handle);
            return VulkanCapabilities::default();
        }

        let mut device_count: u32 = 0;
        let mut physical_device: VkPhysicalDevice = std::ptr::null_mut();

        if let Some(enum_fn) = vk_enumerate_physical_devices {
            enum_fn(instance, &mut device_count, std::ptr::null_mut());
            if device_count > 0 {
                let mut devices = vec![std::ptr::null_mut(); device_count as usize];
                enum_fn(instance, &mut device_count, devices.as_mut_ptr());
                physical_device = devices[0];
            }
        }

        if physical_device.is_null() {
            if let Some(destroy_fn) = vk_destroy_instance {
                destroy_fn(instance, std::ptr::null());
            }
            libc::dlclose(handle);
            return VulkanCapabilities::default();
        }

        let caps = query_physical_device_capabilities(
            handle,
            physical_device,
            instance_ver,
            instance_ext_count,
        );

        if let Some(destroy_fn) = vk_destroy_instance {
            destroy_fn(instance, std::ptr::null());
        }

        libc::dlclose(handle);
        caps
    }
}
