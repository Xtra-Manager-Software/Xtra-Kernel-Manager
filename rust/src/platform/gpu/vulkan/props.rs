use std::ffi::CStr;

use crate::platform::gpu::types::{VulkanCapabilities, VulkanQueueInfo};
use super::caps::{RawPhysicalDeviceData, build_vulkan_capabilities};
use super::ffi::*;

/// # Safety
/// Queries properties, features, limits, and extensions from a physical device handle.
pub unsafe fn query_physical_device_capabilities(
    handle: *mut std::ffi::c_void,
    physical_device: VkPhysicalDevice,
    instance_ver: u32,
    instance_ext_count: u32,
) -> VulkanCapabilities {
    unsafe {
        type FnVkGetPhysicalDeviceProperties =
            unsafe extern "system" fn(VkPhysicalDevice, *mut VkPhysicalDeviceProperties);

        type FnVkGetPhysicalDeviceProperties2 =
            unsafe extern "system" fn(VkPhysicalDevice, *mut VkPhysicalDeviceProperties2);

        type FnVkGetPhysicalDeviceFeatures =
            unsafe extern "system" fn(VkPhysicalDevice, *mut VkPhysicalDeviceFeatures);

        type FnVkGetPhysicalDeviceMemoryProperties =
            unsafe extern "system" fn(VkPhysicalDevice, *mut VkPhysicalDeviceMemoryProperties);

        type FnVkGetPhysicalDeviceQueueFamilyProperties =
            unsafe extern "system" fn(VkPhysicalDevice, *mut u32, *mut VkQueueFamilyProperties);

        type FnVkEnumerateDeviceExtensionProperties = unsafe extern "system" fn(
            VkPhysicalDevice,
            *const std::ffi::c_char,
            *mut u32,
            *mut VkExtensionProperties,
        ) -> VkResult;

        let vk_get_physical_device_properties: Option<FnVkGetPhysicalDeviceProperties> =
            std::mem::transmute::<*mut libc::c_void, Option<FnVkGetPhysicalDeviceProperties>>(
                libc::dlsym(handle, c"vkGetPhysicalDeviceProperties".as_ptr()),
            );

        let vk_get_physical_device_properties_2: Option<FnVkGetPhysicalDeviceProperties2> =
            std::mem::transmute::<*mut libc::c_void, Option<FnVkGetPhysicalDeviceProperties2>>(
                libc::dlsym(handle, c"vkGetPhysicalDeviceProperties2".as_ptr()),
            );

        let vk_get_physical_device_features: Option<FnVkGetPhysicalDeviceFeatures> =
            std::mem::transmute::<*mut libc::c_void, Option<FnVkGetPhysicalDeviceFeatures>>(
                libc::dlsym(handle, c"vkGetPhysicalDeviceFeatures".as_ptr()),
            );

        let vk_get_physical_device_memory_properties: Option<
            FnVkGetPhysicalDeviceMemoryProperties,
        > = std::mem::transmute::<*mut libc::c_void, Option<FnVkGetPhysicalDeviceMemoryProperties>>(
            libc::dlsym(handle, c"vkGetPhysicalDeviceMemoryProperties".as_ptr()),
        );

        let vk_get_physical_device_queue_family_properties: Option<
            FnVkGetPhysicalDeviceQueueFamilyProperties,
        > = std::mem::transmute::<
            *mut libc::c_void,
            Option<FnVkGetPhysicalDeviceQueueFamilyProperties>,
        >(libc::dlsym(
            handle,
            c"vkGetPhysicalDeviceQueueFamilyProperties".as_ptr(),
        ));

        let vk_enumerate_device_extension_properties: Option<
            FnVkEnumerateDeviceExtensionProperties,
        > = std::mem::transmute::<*mut libc::c_void, Option<FnVkEnumerateDeviceExtensionProperties>>(
            libc::dlsym(handle, c"vkEnumerateDeviceExtensionProperties".as_ptr()),
        );

        let mut props: VkPhysicalDeviceProperties = std::mem::zeroed();
        let mut id_props: VkPhysicalDeviceIDProperties = std::mem::zeroed();
        id_props.s_type = 1000071004;

        let mut driver_props: VkPhysicalDeviceDriverProperties = std::mem::zeroed();
        driver_props.s_type = 1000196000;

        let mut subgroup_props: VkPhysicalDeviceSubgroupProperties = std::mem::zeroed();
        subgroup_props.s_type = 1000094000;

        let mut push_desc_props: VkPhysicalDevicePushDescriptorPropertiesKHR = std::mem::zeroed();
        push_desc_props.s_type = 1000080000;

        let mut has_props2 = false;
        match vk_get_physical_device_properties_2 {
            Some(get_props2) => {
                push_desc_props.p_next = std::ptr::null_mut();
                driver_props.p_next = &mut push_desc_props as *mut _ as *mut std::ffi::c_void;
                subgroup_props.p_next = &mut driver_props as *mut _ as *mut std::ffi::c_void;
                id_props.p_next = &mut subgroup_props as *mut _ as *mut std::ffi::c_void;

                let mut props2: VkPhysicalDeviceProperties2 = std::mem::zeroed();
                props2.s_type = 1000059000;
                props2.p_next = &mut id_props as *mut _ as *mut std::ffi::c_void;

                get_props2(physical_device, &mut props2);
                props = props2.properties;
                has_props2 = true;
            }
            None => {
                if let Some(get_props) = vk_get_physical_device_properties {
                    get_props(physical_device, &mut props);
                }
            }
        }

        let mut features: VkPhysicalDeviceFeatures = std::mem::zeroed();
        if let Some(get_feat) = vk_get_physical_device_features {
            get_feat(physical_device, &mut features);
        }

        let mut mem_props: VkPhysicalDeviceMemoryProperties = std::mem::zeroed();
        if let Some(get_mem) = vk_get_physical_device_memory_properties {
            get_mem(physical_device, &mut mem_props);
        }

        let mut dev_ext_count: u32 = 0;
        let mut ext_names = Vec::new();
        if let Some(get_ext) = vk_enumerate_device_extension_properties {
            get_ext(
                physical_device,
                std::ptr::null(),
                &mut dev_ext_count,
                std::ptr::null_mut(),
            );
            if dev_ext_count > 0 {
                let mut exts = vec![std::mem::zeroed(); dev_ext_count as usize];
                get_ext(
                    physical_device,
                    std::ptr::null(),
                    &mut dev_ext_count,
                    exts.as_mut_ptr(),
                );
                for e in exts {
                    let name = CStr::from_ptr(e.extension_name.as_ptr())
                        .to_string_lossy()
                        .into_owned();
                    ext_names.push(name);
                }
            }
        }

        let mut queue_count: u32 = 0;
        let mut queues = Vec::new();
        if let Some(get_queues) = vk_get_physical_device_queue_family_properties {
            get_queues(physical_device, &mut queue_count, std::ptr::null_mut());
            if queue_count > 0 {
                let mut q_props = vec![std::mem::zeroed(); queue_count as usize];
                get_queues(physical_device, &mut queue_count, q_props.as_mut_ptr());
                for (idx, q) in q_props.iter().enumerate() {
                    let mut flags = Vec::new();
                    if q.queue_flags & 0x1 != 0 {
                        flags.push("graphics".to_string());
                    }
                    if q.queue_flags & 0x2 != 0 {
                        flags.push("compute".to_string());
                    }
                    if q.queue_flags & 0x4 != 0 {
                        flags.push("transfer".to_string());
                    }
                    if q.queue_flags & 0x8 != 0 {
                        flags.push("sparse".to_string());
                    }
                    if q.queue_flags & 0x10 != 0 {
                        flags.push("protected".to_string());
                    }
                    if q.queue_flags & 0x40 != 0 {
                        flags.push("optical_flow".to_string());
                    }

                    queues.push(VulkanQueueInfo {
                        queue_id: idx as u32,
                        flags,
                        count: q.queue_count,
                        timestamp_bits: q.timestamp_valid_bits,
                    });
                }
            }
        }

        let raw_data = RawPhysicalDeviceData {
            props,
            id_props,
            driver_props,
            subgroup_props,
            push_desc_props,
            features,
            mem_props,
            has_props2,
            ext_names,
            queues,
        };

        build_vulkan_capabilities(raw_data, instance_ver, instance_ext_count)
    }
}
