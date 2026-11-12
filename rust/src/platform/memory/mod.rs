pub mod meminfo;
pub mod zram;

pub use meminfo::{MemoryInfo, SwapInfo, drop_caches, read_memory_and_swap};
pub use zram::{
    ZramStats, get_available_zram_algorithms, get_current_zram_algorithm, get_swappiness,
    read_zram_stats, set_swappiness,
};
