use std::ffi::CStr;
use super::structs::{
    VkPhysicalDeviceDriverProperties, VkPhysicalDeviceMemoryProperties,
    VkPhysicalDeviceSubgroupProperties,
};

pub fn extract_driver_info(
    has_props2: bool,
    props: &VkPhysicalDeviceDriverProperties,
) -> (String, String, String) {
    if has_props2 && props.driver_name[0] != 0 {
        // Safety: driver_name and driver_info are null-terminated C strings
        let name = unsafe {
            CStr::from_ptr(props.driver_name.as_ptr())
                .to_string_lossy()
                .into_owned()
        };
        let info = if props.driver_info[0] != 0 {
            unsafe {
                CStr::from_ptr(props.driver_info.as_ptr())
                    .to_string_lossy()
                    .into_owned()
            }
        } else {
            String::new()
        };
        let conf = format!(
            "{}.{}.{}.{}",
            props.conformance_version.major,
            props.conformance_version.minor,
            props.conformance_version.subminor,
            props.conformance_version.patch
        );
        (name, info, conf)
    } else {
        (String::new(), String::new(), String::new())
    }
}

pub fn calculate_memory_heaps(mem: &VkPhysicalDeviceMemoryProperties) -> (String, u64, u64) {
    let mut total_local: u64 = 0;
    let mut heap0_mb: u64 = 0;
    let mut heap1_mb: u64 = 0;
    for i in 0..mem.memory_heap_count as usize {
        let h = &mem.memory_heaps[i];
        let mb = h.size / (1024 * 1024);
        match i {
            0 => heap0_mb = mb,
            1 => heap1_mb = mb,
            _ => {}
        }
        if h.flags & 0x1 != 0 {
            total_local += h.size;
        }
    }
    if total_local == 0 {
        total_local = (heap0_mb + heap1_mb) * 1024 * 1024;
    }
    let local_gb = total_local as f64 / (1024.0 * 1024.0 * 1024.0);
    let dev_local_str = if local_gb >= 1.0 {
        format!("{} GB", local_gb.round() as u64)
    } else {
        format!("{} MB", total_local / (1024 * 1024))
    };
    (dev_local_str, heap0_mb, heap1_mb)
}

pub fn extract_subgroups(
    has_props2: bool,
    props: &VkPhysicalDeviceSubgroupProperties,
) -> (u32, Vec<String>, Vec<String>) {
    if has_props2 && props.subgroup_size > 0 {
        let mut ops = Vec::new();
        if props.supported_operations & 0x1 != 0 {
            ops.push("basic".to_string());
        }
        if props.supported_operations & 0x2 != 0 {
            ops.push("vote".to_string());
        }
        if props.supported_operations & 0x4 != 0 {
            ops.push("arithmetic".to_string());
        }
        if props.supported_operations & 0x8 != 0 {
            ops.push("ballot".to_string());
        }
        if props.supported_operations & 0x10 != 0 {
            ops.push("shuffle".to_string());
        }
        if props.supported_operations & 0x20 != 0 {
            ops.push("shuffle_relative".to_string());
        }
        if props.supported_operations & 0x40 != 0 {
            ops.push("clustered".to_string());
        }
        if props.supported_operations & 0x80 != 0 {
            ops.push("quad".to_string());
        }

        let mut stages = Vec::new();
        if props.supported_stages & 0x1 != 0 {
            stages.push("vertex".to_string());
        }
        if props.supported_stages & 0x10 != 0 {
            stages.push("fragment".to_string());
        }
        if props.supported_stages & 0x20 != 0 {
            stages.push("compute".to_string());
        }

        (props.subgroup_size, ops, stages)
    } else {
        (0, Vec::new(), Vec::new())
    }
}
