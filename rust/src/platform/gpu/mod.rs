pub mod detect;
pub mod frequency;
pub mod load;
pub mod opencl;
pub mod power;
pub mod types;
pub mod vulkan;

pub use detect::{get_gpu_info, GpuVendor};
pub use frequency::{get_available_frequencies, read_gpu_freq_mhz};
pub use load::{read_gpu_busy_pct, reset_gpu_busy_stats};
pub use power::{set_gpu_performance_mode, set_gpu_powersave_mode};
pub use types::{FullGpuCapabilities, VulkanCapabilities, VulkanQueueInfo};
pub use vulkan::scan_full_gpu_capabilities;
