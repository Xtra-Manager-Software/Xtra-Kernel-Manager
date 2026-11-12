pub mod drivers;
pub mod inventory;
pub mod loader;
pub mod parameters;

pub use drivers::{KernelDriverInfo, list_kernel_drivers};
pub use inventory::{
    KernelModuleInfo, is_module_protected, list_loaded_modules, parse_proc_modules,
};
pub use loader::{load_kernel_module, unload_kernel_module};
pub use parameters::{get_module_parameters, set_module_parameter};
