use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct VulkanQueueInfo {
    pub queue_id: u32,
    pub flags: Vec<String>,
    pub count: u32,
    pub timestamp_bits: u32,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct VulkanCapabilities {
    pub name: String,
    pub vendor: String,
    pub device_id: String,
    pub device_type: String,
    pub instance_api: String,
    pub device_api: String,
    pub driver: String,
    pub driver_info: String,
    pub driver_version: String,
    pub driver_conformance: String,
    pub device_extensions_count: u32,
    pub instance_extensions_count: u32,

    pub ray_tracing: bool,
    pub mesh_shaders: bool,
    pub variable_rate_shading: bool,
    pub cooperative_matrix: bool,
    pub rt_pipeline: bool,
    pub ray_query: bool,
    pub acceleration_structure: bool,
    pub portability_subset: bool,

    pub device_uuid: String,
    pub driver_uuid: String,
    pub pipeline_cache_uuid: String,

    pub memory_device_local: String,
    pub memory_heap_0: String,
    pub memory_heap_1: String,

    pub max_image_1d: u32,
    pub max_image_2d: String,
    pub max_image_3d: String,
    pub max_image_cube: String,
    pub max_array_layers: u32,
    pub uniform_buffer_range: String,
    pub storage_buffer_range: String,
    pub max_anisotropy: u32,
    pub framebuffer_color_max_samples: u32,
    pub framebuffer_depth_max_samples: u32,

    pub subgroup_size: u32,
    pub max_compute_invocations: u32,
    pub supported_subgroup_ops: Vec<String>,
    pub supported_subgroup_stages: Vec<String>,
    pub graphics_subgroups: bool,

    pub robust_buffer_access: bool,
    pub sampler_anisotropy: bool,
    pub shader_int16: bool,
    pub shader_float64: bool,

    pub protected_memory_vulkan_1_1: bool,
    pub timeline_semaphore_vulkan_1_2: bool,
    pub dynamic_rendering_vulkan_1_3: bool,
    pub sync2_vulkan_1_3: bool,
    pub index_type_uint8: bool,
    pub line_rasterization: bool,
    pub push_descriptors_vulkan_1_4: String,
    pub global_priority_query: bool,
    pub shader_float_controls_2: bool,
    pub max_push_descriptors: u32,

    pub maintenance5: bool,
    pub maintenance6: bool,
    pub maintenance7: bool,
    pub descriptor_buffer: bool,
    pub graphics_pipeline_library: bool,
    pub calibrated_timestamps: bool,

    pub queues: Vec<VulkanQueueInfo>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct FullGpuCapabilities {
    pub vendor: String,
    pub renderer: String,
    pub chip: String,
    pub on_chip_memory: String,
    pub compute_units: u32,
    pub vulkan_api_version: String,
    pub opengl_es_version: String,
    pub vulkan: VulkanCapabilities,
}
