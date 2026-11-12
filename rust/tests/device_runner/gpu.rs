use crate::util;
use std::time::Instant;
use xkm_native::platform::gpu;

pub fn run() {
    println!("\n[gpu]");
    let t0 = Instant::now();
    let (gpu_vendor, gpu_model) = gpu::get_gpu_info();
    let gpu_freq = gpu::read_gpu_freq_mhz();
    let gpu_busy = gpu::read_gpu_busy_pct();
    let gpu_avail_freqs = gpu::get_available_frequencies();
    let full_caps = gpu::scan_full_gpu_capabilities();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  vendor: {}", gpu_vendor);
    println!("  model: {}", gpu_model);
    println!("  current_freq: {} MHz", gpu_freq);
    println!("  load: {:.1}%", gpu_busy);
    println!("  available_freqs: {:?}", gpu_avail_freqs);
    println!(
        "  chip: {} ({} CUs)",
        full_caps.chip, full_caps.compute_units
    );
    println!("  on_chip_memory: {}", full_caps.on_chip_memory);
    println!("  opengl_es: {}", full_caps.opengl_es_version);
    println!("  vulkan_api: {}", full_caps.vulkan.device_api);
    println!("  vulkan_instance_api: {}", full_caps.vulkan.instance_api);
    println!(
        "  vulkan_driver: {} (v{})",
        full_caps.vulkan.driver_version, full_caps.vulkan.driver_conformance
    );
    println!(
        "  vulkan_extensions: {} device / {} instance",
        full_caps.vulkan.device_extensions_count, full_caps.vulkan.instance_extensions_count
    );
    println!(
        "  vrs_supported: {}",
        full_caps.vulkan.variable_rate_shading
    );
    println!("  uuids:");
    println!("    device_uuid: {}", full_caps.vulkan.device_uuid);
    println!("    driver_uuid: {}", full_caps.vulkan.driver_uuid);
    println!(
        "    pipeline_cache_uuid: {}",
        full_caps.vulkan.pipeline_cache_uuid
    );
    println!(
        "  memory_local: {} (Heap 0: {}, Heap 1: {})",
        full_caps.vulkan.memory_device_local,
        full_caps.vulkan.memory_heap_0,
        full_caps.vulkan.memory_heap_1
    );
    println!(
        "  subgroup: size={}, ops={:?}, stages={:?}",
        full_caps.vulkan.subgroup_size,
        full_caps.vulkan.supported_subgroup_ops,
        full_caps.vulkan.supported_subgroup_stages
    );
    println!(
        "  limits: max_1d={}, max_2d={}, max_3d={}, max_cube={}, uniform_buf={}, storage_buf={}",
        full_caps.vulkan.max_image_1d,
        full_caps.vulkan.max_image_2d,
        full_caps.vulkan.max_image_3d,
        full_caps.vulkan.max_image_cube,
        full_caps.vulkan.uniform_buffer_range,
        full_caps.vulkan.storage_buffer_range
    );
    println!(
        "  core_features: robust_buf={}, sampler_aniso={}, int16={}, float64={}",
        full_caps.vulkan.robust_buffer_access,
        full_caps.vulkan.sampler_anisotropy,
        full_caps.vulkan.shader_int16,
        full_caps.vulkan.shader_float64
    );
    println!(
        "  vulkan_1_1: protected_mem={}",
        full_caps.vulkan.protected_memory_vulkan_1_1
    );
    println!(
        "  vulkan_1_2: timeline_semaphore={}",
        full_caps.vulkan.timeline_semaphore_vulkan_1_2
    );
    println!(
        "  vulkan_1_3: dynamic_rendering={}, sync2={}",
        full_caps.vulkan.dynamic_rendering_vulkan_1_3, full_caps.vulkan.sync2_vulkan_1_3
    );
    println!(
        "  vulkan_1_4: uint8={}, line_raster={}, push_desc={} (max={}), global_priority={}, float_controls2={}",
        full_caps.vulkan.index_type_uint8,
        full_caps.vulkan.line_rasterization,
        full_caps.vulkan.push_descriptors_vulkan_1_4,
        full_caps.vulkan.max_push_descriptors,
        full_caps.vulkan.global_priority_query,
        full_caps.vulkan.shader_float_controls_2
    );
    println!(
        "  highlights: maint5={}, maint6={}, maint7={}, desc_buf={}, gpl={}, cal_ts={}",
        full_caps.vulkan.maintenance5,
        full_caps.vulkan.maintenance6,
        full_caps.vulkan.maintenance7,
        full_caps.vulkan.descriptor_buffer,
        full_caps.vulkan.graphics_pipeline_library,
        full_caps.vulkan.calibrated_timestamps
    );
    for q in &full_caps.vulkan.queues {
        println!(
            "  queue {}: flags={:?}, count={}, timestamp_bits={}",
            q.queue_id, q.flags, q.count, q.timestamp_bits
        );
    }
}
