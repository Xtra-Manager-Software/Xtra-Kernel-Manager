use std::ffi::CStr;

use crate::platform::gpu::types::{VulkanCapabilities, VulkanQueueInfo};
use super::ffi::*;
use super::helpers::{calculate_memory_heaps, extract_driver_info, extract_subgroups};

pub struct RawPhysicalDeviceData {
    pub props: VkPhysicalDeviceProperties,
    pub id_props: VkPhysicalDeviceIDProperties,
    pub driver_props: VkPhysicalDeviceDriverProperties,
    pub subgroup_props: VkPhysicalDeviceSubgroupProperties,
    pub push_desc_props: VkPhysicalDevicePushDescriptorPropertiesKHR,
    pub features: VkPhysicalDeviceFeatures,
    pub mem_props: VkPhysicalDeviceMemoryProperties,
    pub has_props2: bool,
    pub ext_names: Vec<String>,
    pub queues: Vec<VulkanQueueInfo>,
}

pub fn build_vulkan_capabilities(
    data: RawPhysicalDeviceData,
    instance_ver: u32,
    instance_ext_count: u32,
) -> VulkanCapabilities {
    let RawPhysicalDeviceData {
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
    } = data;

    // Safety: device_name is null-terminated UTF-8 byte string inside props
    let device_name_str = unsafe {
        CStr::from_ptr(props.device_name.as_ptr())
            .to_string_lossy()
            .into_owned()
    };

    let vendor_name_str = match props.vendor_id {
        0x5143 => "Qualcomm".to_string(),
        0x13B5 => "ARM".to_string(),
        0x10DE => "NVIDIA".to_string(),
        0x8086 => "Intel".to_string(),
        0x1002 => "AMD".to_string(),
        0x1010 => "ImgTec".to_string(),
        0x14E4 => "Broadcom".to_string(),
        _ => format!("Vendor 0x{:04X}", props.vendor_id),
    };

    let (driver_name_str, driver_info_str, driver_conformance_str) =
        extract_driver_info(has_props2, &driver_props);

    let driver_ver_str = match props.vendor_id {
        0x5143 => format!(
            "{}.{}.{}",
            props.driver_version >> 22,
            (props.driver_version >> 12) & 0x3ff,
            props.driver_version & 0xfff
        ),
        _ => format!("{}", props.driver_version),
    };

    let dev_type_str = match props.device_type {
        1 => "Integrated GPU".to_string(),
        2 => "Discrete GPU".to_string(),
        3 => "Virtual GPU".to_string(),
        4 => "CPU".to_string(),
        _ => "Other".to_string(),
    };

    let device_uuid_str = match has_props2 && id_props.device_uuid.iter().any(|&b| b != 0) {
        true => format_uuid(&id_props.device_uuid),
        false => String::new(),
    };

    let driver_uuid_str = match has_props2 && id_props.driver_uuid.iter().any(|&b| b != 0) {
        true => format_uuid(&id_props.driver_uuid),
        false => String::new(),
    };

    let pipeline_cache_uuid_str = match props.pipeline_cache_uuid.iter().any(|&b| b != 0) {
        true => format_uuid(&props.pipeline_cache_uuid),
        false => String::new(),
    };

    let (dev_local_str, heap0_mb, heap1_mb) = calculate_memory_heaps(&mem_props);
    let (subgroup_size_val, subgroup_ops, subgroup_stages) =
        extract_subgroups(has_props2, &subgroup_props);

    let vrs_supported = ext_names
        .iter()
        .any(|e| e == "VK_KHR_fragment_shading_rate" || e == "VK_QCOM_shading_rate");
    let ray_tracing = ext_names
        .iter()
        .any(|e| e == "VK_KHR_ray_tracing_pipeline" || e == "VK_NV_ray_tracing");
    let mesh_shaders = ext_names
        .iter()
        .any(|e| e == "VK_EXT_mesh_shader" || e == "VK_NV_mesh_shader");
    let cooperative_matrix = ext_names
        .iter()
        .any(|e| e == "VK_KHR_cooperative_matrix" || e == "VK_NV_cooperative_matrix");
    let rt_pipeline = ext_names
        .iter()
        .any(|e| e == "VK_KHR_ray_tracing_pipeline");
    let ray_query = ext_names.iter().any(|e| e == "VK_KHR_ray_query");
    let acceleration_structure = ext_names
        .iter()
        .any(|e| e == "VK_KHR_acceleration_structure");
    let portability_subset = ext_names
        .iter()
        .any(|e| e == "VK_KHR_portability_subset");

    let protected_memory_vulkan_1_1 = ext_names.iter().any(|e| e == "VK_KHR_protected_memory")
        || (0..mem_props.memory_heap_count as usize)
            .any(|i| mem_props.memory_heaps[i].flags & 0x2 != 0)
        || (0..mem_props.memory_type_count as usize)
            .any(|i| mem_props.memory_types[i].property_flags & 0x20 != 0);

    let timeline_semaphore_vulkan_1_2 = ext_names.iter().any(|e| e == "VK_KHR_timeline_semaphore");
    let dynamic_rendering_vulkan_1_3 = ext_names.iter().any(|e| e == "VK_KHR_dynamic_rendering");
    let sync2_vulkan_1_3 = ext_names.iter().any(|e| e == "VK_KHR_synchronization2");

    let index_type_uint8 = ext_names.iter().any(|e| e == "VK_KHR_index_type_uint8");
    let line_rasterization = ext_names
        .iter()
        .any(|e| e == "VK_KHR_line_rasterization" || e == "VK_EXT_line_rasterization");
    let push_descriptors_vulkan_1_4 = match ext_names
        .iter()
        .any(|e| e == "VK_KHR_push_descriptor")
    {
        true => "Yes (extension)".to_string(),
        false => "No".to_string(),
    };
    let global_priority_query = ext_names
        .iter()
        .any(|e| e == "VK_KHR_global_priority" || e == "VK_EXT_global_priority_query");
    let shader_float_controls_2 = ext_names
        .iter()
        .any(|e| e == "VK_KHR_shader_float_controls2");

    let limits = &props.limits;
    let dev_ext_count = ext_names.len() as u32;

    VulkanCapabilities {
        name: device_name_str,
        vendor: vendor_name_str,
        device_id: format!("0x{:08X}", props.device_id),
        device_type: dev_type_str,
        instance_api: format!(
            "{}.{}.{}",
            instance_ver >> 22,
            (instance_ver >> 12) & 0x3ff,
            instance_ver & 0xfff
        ),
        device_api: format!(
            "{}.{}.{}",
            props.api_version >> 22,
            (props.api_version >> 12) & 0x3ff,
            props.api_version & 0xfff
        ),
        driver: driver_name_str,
        driver_info: driver_info_str,
        driver_version: driver_ver_str,
        driver_conformance: driver_conformance_str,
        device_extensions_count: dev_ext_count,
        instance_extensions_count: instance_ext_count,
        ray_tracing,
        mesh_shaders,
        variable_rate_shading: vrs_supported,
        cooperative_matrix,
        rt_pipeline,
        ray_query,
        acceleration_structure,
        portability_subset,
        device_uuid: device_uuid_str,
        driver_uuid: driver_uuid_str,
        pipeline_cache_uuid: pipeline_cache_uuid_str,
        memory_device_local: dev_local_str,
        memory_heap_0: format!("{:.2} GB", heap0_mb as f64 / 1024.0),
        memory_heap_1: format!("{:.2} MB", heap1_mb as f64),
        max_image_1d: limits.max_image_dimension_1d,
        max_image_2d: format!(
            "{} \u{00d7} {}",
            limits.max_image_dimension_2d, limits.max_image_dimension_2d
        ),
        max_image_3d: format!(
            "{} \u{00d7} {} \u{00d7} {}",
            limits.max_image_dimension_3d,
            limits.max_image_dimension_3d,
            limits.max_image_dimension_3d
        ),
        max_image_cube: format!(
            "{} \u{00d7} {}",
            limits.max_image_dimension_cube, limits.max_image_dimension_cube
        ),
        max_array_layers: limits.max_image_array_layers,
        uniform_buffer_range: format!(
            "{:.1} KB",
            limits.max_uniform_buffer_range as f64 / 1024.0
        ),
        storage_buffer_range: format!(
            "{:.1} MB",
            limits.max_storage_buffer_range as f64 / (1024.0 * 1024.0)
        ),
        max_anisotropy: limits.max_sampler_anisotropy as u32,
        framebuffer_color_max_samples: max_sample_count(limits.framebuffer_color_sample_counts),
        framebuffer_depth_max_samples: max_sample_count(limits.framebuffer_depth_sample_counts),
        subgroup_size: subgroup_size_val,
        max_compute_invocations: limits.max_compute_work_group_invocations,
        supported_subgroup_ops: subgroup_ops,
        supported_subgroup_stages: subgroup_stages,
        graphics_subgroups: has_props2 && subgroup_props.subgroup_size > 0,
        robust_buffer_access: features.robust_buffer_access != 0,
        sampler_anisotropy: features.sampler_anisotropy != 0,
        shader_int16: features.shader_int16 != 0,
        shader_float64: features.shader_float64 != 0,
        protected_memory_vulkan_1_1,
        timeline_semaphore_vulkan_1_2,
        dynamic_rendering_vulkan_1_3,
        sync2_vulkan_1_3,
        index_type_uint8,
        line_rasterization,
        push_descriptors_vulkan_1_4,
        global_priority_query,
        shader_float_controls_2,
        max_push_descriptors: push_desc_props.max_push_descriptors,
        maintenance5: ext_names.iter().any(|e| e == "VK_KHR_maintenance5"),
        maintenance6: ext_names.iter().any(|e| e == "VK_KHR_maintenance6"),
        maintenance7: ext_names.iter().any(|e| e == "VK_KHR_maintenance7"),
        descriptor_buffer: ext_names.iter().any(|e| e == "VK_EXT_descriptor_buffer"),
        graphics_pipeline_library: ext_names
            .iter()
            .any(|e| e == "VK_EXT_graphics_pipeline_library"),
        calibrated_timestamps: ext_names
            .iter()
            .any(|e| e == "VK_KHR_calibrated_timestamps" || e == "VK_EXT_calibrated_timestamps"),
        queues,
    }
}
