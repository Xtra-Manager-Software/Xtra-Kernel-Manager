pub mod detect;
pub mod frequency;
pub mod load;
pub mod power;

pub use detect::{get_gpu_info, GpuVendor};
pub use frequency::{get_available_frequencies, read_gpu_freq_mhz};
pub use load::{read_gpu_busy_pct, reset_gpu_busy_stats};
pub use power::{set_gpu_performance_mode, set_gpu_powersave_mode};
