use crate::util;
use std::time::Instant;
use xkm_native::platform::system;

pub fn run() {
    println!("\n[system]");
    let t0 = Instant::now();
    let sys = system::scan_system_subsystem();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!(
        "  os: {} ({}) [API {}]",
        sys.os_name, sys.os_codename, sys.api_level
    );
    println!("  soc: {} {}", sys.soc_manufacturer, sys.soc_model);
    println!("  build: {} ({})", sys.build_id, sys.build_type);
    println!("  fingerprint: {}", sys.fingerprint);
    println!("  kernel: {}", sys.kernel_version);
    println!(
        "  treble: {}, mainline: {}, dynamic_partitions: {}",
        sys.project_treble, sys.project_mainline, sys.dynamic_partitions
    );
    println!(
        "  active_slot: {}, avb: {} ({}), dm_verity: {}, bootloader: {}",
        sys.active_slot,
        sys.verified_boot_version,
        sys.verified_boot_state,
        sys.dm_verity,
        sys.bootloader_status
    );
    println!(
        "  runtime: {}, {}, {}",
        sys.google_play_services, sys.toybox_version, sys.java_vm
    );
}
