use crate::util;
use std::time::Instant;
use xkm_native::platform::modules;

pub fn run() {
    println!("\n[kernel_modules]");
    let t0 = Instant::now();
    let loaded_modules = modules::list_loaded_modules();
    let drivers = modules::list_kernel_drivers();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  total_loaded: {}", loaded_modules.len());
    for m in loaded_modules.iter().take(6) {
        println!(
            "    {:<20} size={:>8}B, used_by={:<2}, state={:<5}, protected={:<5}, params={}",
            m.name, m.size_bytes, m.instances, m.state, m.is_protected, m.has_parameters
        );
    }

    if let Some(mod_with_params) = loaded_modules.iter().find(|m| m.has_parameters) {
        let t_param = Instant::now();
        let params = modules::get_module_parameters(&mod_with_params.name);
        println!(
            "  sample_params ({}): {} parameters [took {}]",
            mod_with_params.name,
            params.len(),
            util::fmt_duration(t_param.elapsed())
        );
        for (k, v) in params.iter().take(4) {
            println!("    {} = {}", k, v);
        }
    }

    println!("  total_bus_drivers: {}", drivers.len());
    let mut bus_counts = std::collections::HashMap::new();
    for d in &drivers {
        *bus_counts.entry(d.bus.as_str()).or_insert(0usize) += 1;
    }
    println!("  drivers_by_bus: {:?}", bus_counts);
    for d in drivers
        .iter()
        .filter(|d| !d.bound_devices.is_empty())
        .take(4)
    {
        println!(
            "    [{}] {} (devices={:?})",
            d.bus,
            d.name,
            d.bound_devices.iter().take(2).collect::<Vec<_>>()
        );
    }
}
