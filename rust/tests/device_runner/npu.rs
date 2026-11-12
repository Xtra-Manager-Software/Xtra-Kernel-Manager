use std::time::Instant;
use xkm_native::platform::npu;
use crate::util;

pub fn run() {
    println!("\n[npu_ai_accelerator]");
    let t0 = Instant::now();
    let info = npu::scan_npu_subsystem();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  available: {}", info.is_available);
    println!("  vendor_family: {}", info.vendor_family);
    println!("  hardware_state: {}", info.hardware_state);

    if !info.coprocessors.is_empty() {
        println!("  coprocessors (remoteproc):");
        for c in &info.coprocessors {
            println!("    - {} [{}]: state={}", c.name, c.subsystem_type, c.state);
        }
    }

    if !info.dev_nodes.is_empty() {
        println!("  compute_dev_nodes: {:?}", info.dev_nodes);
    }

    if !info.active_daemons.is_empty() {
        println!("  active_daemons: {:?}", info.active_daemons);
    }

    if !info.ai_runtimes.is_empty() {
        println!("  ai_runtimes_detected: {:?}", info.ai_runtimes);
    }

    if !info.hal_services.is_empty() {
        println!("  hal_services: {:?}", info.hal_services);
    }
}
