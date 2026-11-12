pub type VkResult = i32;
pub type VkInstance = *mut std::ffi::c_void;
pub type VkPhysicalDevice = *mut std::ffi::c_void;

#[repr(C)]
pub struct VkApplicationInfo {
    pub s_type: u32,
    pub p_next: *const std::ffi::c_void,
    pub p_application_name: *const std::ffi::c_char,
    pub application_version: u32,
    pub p_engine_name: *const std::ffi::c_char,
    pub engine_version: u32,
    pub api_version: u32,
}

#[repr(C)]
pub struct VkInstanceCreateInfo {
    pub s_type: u32,
    pub p_next: *const std::ffi::c_void,
    pub flags: u32,
    pub p_application_info: *const VkApplicationInfo,
    pub enabled_layer_count: u32,
    pub pp_enabled_layer_names: *const *const std::ffi::c_char,
    pub enabled_extension_count: u32,
    pub pp_enabled_extension_names: *const *const std::ffi::c_char,
}

#[repr(C)]
pub struct VkPhysicalDeviceLimits {
    pub max_image_dimension_1d: u32,
    pub max_image_dimension_2d: u32,
    pub max_image_dimension_3d: u32,
    pub max_image_dimension_cube: u32,
    pub max_image_array_layers: u32,
    pub max_texel_buffer_elements: u32,
    pub max_uniform_buffer_range: u32,
    pub max_storage_buffer_range: u32,
    pub max_push_constants_size: u32,
    pub max_memory_allocation_count: u32,
    pub max_sampler_allocation_count: u32,
    pub buffer_image_granularity: u64,
    pub sparse_address_space_size: u64,
    pub max_bound_descriptor_sets: u32,
    pub max_per_stage_descriptor_samplers: u32,
    pub max_per_stage_descriptor_uniform_buffers: u32,
    pub max_per_stage_descriptor_storage_buffers: u32,
    pub max_per_stage_descriptor_sampled_images: u32,
    pub max_per_stage_descriptor_storage_images: u32,
    pub max_per_stage_descriptor_input_attachments: u32,
    pub max_per_stage_resources: u32,
    pub max_descriptor_set_samplers: u32,
    pub max_descriptor_set_uniform_buffers: u32,
    pub max_descriptor_set_uniform_buffers_dynamic: u32,
    pub max_descriptor_set_storage_buffers: u32,
    pub max_descriptor_set_storage_buffers_dynamic: u32,
    pub max_descriptor_set_sampled_images: u32,
    pub max_descriptor_set_storage_images: u32,
    pub max_descriptor_set_input_attachments: u32,
    pub max_vertex_input_attributes: u32,
    pub max_vertex_input_bindings: u32,
    pub max_vertex_input_attribute_offset: u32,
    pub max_vertex_input_binding_stride: u32,
    pub max_vertex_output_components: u32,
    pub max_tessellation_generation_level: u32,
    pub max_tessellation_patch_size: u32,
    pub max_tessellation_control_per_vertex_input_components: u32,
    pub max_tessellation_control_per_vertex_output_components: u32,
    pub max_tessellation_control_per_patch_output_components: u32,
    pub max_tessellation_control_total_output_components: u32,
    pub max_tessellation_evaluation_input_components: u32,
    pub max_tessellation_evaluation_output_components: u32,
    pub max_geometry_shader_invocations: u32,
    pub max_geometry_input_components: u32,
    pub max_geometry_output_components: u32,
    pub max_geometry_output_vertices: u32,
    pub max_geometry_total_output_components: u32,
    pub max_fragment_input_components: u32,
    pub max_fragment_output_attachments: u32,
    pub max_fragment_dual_src_attachments: u32,
    pub max_fragment_combined_output_resources: u32,
    pub max_compute_shared_memory_size: u32,
    pub max_compute_work_group_count: [u32; 3],
    pub max_compute_work_group_invocations: u32,
    pub max_compute_work_group_size: [u32; 3],
    pub sub_pixel_precision_bits: u32,
    pub sub_texel_precision_bits: u32,
    pub mipmap_precision_bits: u32,
    pub max_draw_indexed_index_value: u32,
    pub max_draw_indirect_count: u32,
    pub max_sampler_lod_bias: f32,
    pub max_sampler_anisotropy: f32,
    pub max_viewports: u32,
    pub max_viewport_dimensions: [u32; 2],
    pub viewport_bounds_range: [f32; 2],
    pub viewport_sub_pixel_bits: u32,
    pub min_memory_map_alignment: usize,
    pub min_texel_buffer_offset_alignment: u64,
    pub min_uniform_buffer_offset_alignment: u64,
    pub min_storage_buffer_offset_alignment: u64,
    pub min_texel_offset: i32,
    pub max_texel_offset: u32,
    pub min_texel_gather_offset: i32,
    pub max_texel_gather_offset: u32,
    pub min_interpolation_offset: f32,
    pub max_interpolation_offset: f32,
    pub sub_pixel_interpolation_offset_bits: u32,
    pub max_framebuffer_width: u32,
    pub max_framebuffer_height: u32,
    pub max_framebuffer_layers: u32,
    pub framebuffer_color_sample_counts: u32,
    pub framebuffer_depth_sample_counts: u32,
    pub framebuffer_stencil_sample_counts: u32,
    pub framebuffer_no_attachments_sample_counts: u32,
    pub max_color_attachments: u32,
    pub sampled_image_color_sample_counts: u32,
    pub sampled_image_integer_sample_counts: u32,
    pub sampled_image_depth_sample_counts: u32,
    pub sampled_image_stencil_sample_counts: u32,
    pub storage_image_sample_counts: u32,
    pub max_sample_mask_words: u32,
    pub timestamp_compute_and_graphics: u32,
    pub timestamp_period: f32,
    pub max_clip_distances: u32,
    pub max_cull_distances: u32,
    pub max_combined_clip_and_cull_distances: u32,
    pub discrete_queue_priorities: u32,
    pub point_size_range: [f32; 2],
    pub line_width_range: [f32; 2],
    pub point_size_granularity: f32,
    pub line_width_granularity: f32,
    pub strict_lines: u32,
    pub standard_sample_locations: u32,
    pub optimal_buffer_copy_offset_alignment: u64,
    pub optimal_buffer_copy_row_pitch_alignment: u64,
    pub non_coherent_atom_size: u64,
}

#[repr(C)]
pub struct VkPhysicalDeviceProperties {
    pub api_version: u32,
    pub driver_version: u32,
    pub vendor_id: u32,
    pub device_id: u32,
    pub device_type: u32,
    pub device_name: [std::ffi::c_char; 256],
    pub pipeline_cache_uuid: [u8; 16],
    pub limits: VkPhysicalDeviceLimits,
    pub sparse_properties: [u32; 5],
}

#[repr(C)]
pub struct VkPhysicalDeviceProperties2 {
    pub s_type: u32,
    pub p_next: *mut std::ffi::c_void,
    pub properties: VkPhysicalDeviceProperties,
}

#[repr(C)]
pub struct VkPhysicalDeviceIDProperties {
    pub s_type: u32,
    pub p_next: *mut std::ffi::c_void,
    pub device_uuid: [u8; 16],
    pub driver_uuid: [u8; 16],
    pub device_luid: [u8; 8],
    pub device_node_mask: u32,
    pub device_luid_valid: u32,
}

#[repr(C)]
pub struct VkConformanceVersion {
    pub major: u8,
    pub minor: u8,
    pub subminor: u8,
    pub patch: u8,
}

#[repr(C)]
pub struct VkPhysicalDeviceDriverProperties {
    pub s_type: u32,
    pub p_next: *mut std::ffi::c_void,
    pub driver_id: u32,
    pub driver_name: [std::ffi::c_char; 256],
    pub driver_info: [std::ffi::c_char; 256],
    pub conformance_version: VkConformanceVersion,
}

#[repr(C)]
pub struct VkPhysicalDevicePushDescriptorPropertiesKHR {
    pub s_type: u32,
    pub p_next: *mut std::ffi::c_void,
    pub max_push_descriptors: u32,
}

#[repr(C)]
pub struct VkPhysicalDeviceSubgroupProperties {
    pub s_type: u32,
    pub p_next: *mut std::ffi::c_void,
    pub subgroup_size: u32,
    pub supported_stages: u32,
    pub supported_operations: u32,
    pub quad_operations_in_all_stages: u32,
}

#[repr(C)]
pub struct VkMemoryType {
    pub property_flags: u32,
    pub heap_index: u32,
}

#[repr(C)]
pub struct VkMemoryHeap {
    pub size: u64,
    pub flags: u32,
}

#[repr(C)]
pub struct VkPhysicalDeviceMemoryProperties {
    pub memory_type_count: u32,
    pub memory_types: [VkMemoryType; 32],
    pub memory_heap_count: u32,
    pub memory_heaps: [VkMemoryHeap; 16],
}

#[repr(C)]
#[derive(Clone, Copy)]
pub struct VkQueueFamilyProperties {
    pub queue_flags: u32,
    pub queue_count: u32,
    pub timestamp_valid_bits: u32,
    pub min_image_transfer_granularity: [u32; 3],
}

#[repr(C)]
pub struct VkPhysicalDeviceFeatures {
    pub robust_buffer_access: u32,
    pub full_draw_index_uint32: u32,
    pub image_cube_array: u32,
    pub independent_blend: u32,
    pub geometry_shader: u32,
    pub tessellation_shader: u32,
    pub sample_rate_shading: u32,
    pub dual_src_blend: u32,
    pub logic_op: u32,
    pub multi_draw_indirect: u32,
    pub draw_indirect_first_instance: u32,
    pub depth_clamp: u32,
    pub depth_bias_clamp: u32,
    pub fill_mode_non_solid: u32,
    pub depth_bounds: u32,
    pub wide_lines: u32,
    pub large_points: u32,
    pub alpha_to_one: u32,
    pub multi_viewport: u32,
    pub sampler_anisotropy: u32,
    pub texture_compression_etc2: u32,
    pub texture_compression_astc_ldr: u32,
    pub texture_compression_bc: u32,
    pub occlusion_query_precise: u32,
    pub pipeline_statistics_query: u32,
    pub vertex_pipeline_stores_and_atomics: u32,
    pub fragment_stores_and_atomics: u32,
    pub shader_tessellation_and_geometry_point_size: u32,
    pub shader_image_gather_extended: u32,
    pub shader_storage_image_extended_formats: u32,
    pub shader_storage_image_multisample: u32,
    pub shader_storage_image_read_without_format: u32,
    pub shader_storage_image_write_without_format: u32,
    pub shader_uniform_buffer_array_dynamic_indexing: u32,
    pub shader_sampled_image_array_dynamic_indexing: u32,
    pub shader_storage_buffer_array_dynamic_indexing: u32,
    pub shader_storage_image_array_dynamic_indexing: u32,
    pub shader_clip_distance: u32,
    pub shader_cull_distance: u32,
    pub shader_float64: u32,
    pub shader_int64: u32,
    pub shader_int16: u32,
    pub shader_resource_residency: u32,
    pub shader_resource_min_lod: u32,
    pub sparse_binding: u32,
    pub sparse_residency_buffer: u32,
    pub sparse_residency_image_2d: u32,
    pub sparse_residency_image_3d: u32,
    pub sparse_residency_2_samples: u32,
    pub sparse_residency_4_samples: u32,
    pub sparse_residency_8_samples: u32,
    pub sparse_residency_16_samples: u32,
    pub sparse_residency_aliased: u32,
    pub variable_multisample_rate: u32,
    pub inherited_queries: u32,
}

#[repr(C)]
#[derive(Clone, Copy)]
pub struct VkExtensionProperties {
    pub extension_name: [std::ffi::c_char; 256],
    pub spec_version: u32,
}
