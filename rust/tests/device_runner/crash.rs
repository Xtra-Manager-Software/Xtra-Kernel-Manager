use crate::util;
use std::time::Instant;
use xkm_native::platform::binder;

pub fn run() {
    println!("\n[crash_watchdog]");
    let t0 = Instant::now();
    let crashes = binder::check_recent_kernel_crashes();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    match crashes {
        Ok(entries) => {
            println!("  incidents_count: {}", entries.len());
            for (i, c) in entries.iter().take(5).enumerate() {
                println!(
                    "    [{}] ts={}, tag={}, src={}, summary={}",
                    i + 1,
                    c.timestamp_ms,
                    c.tag,
                    c.source,
                    c.summary
                );
            }
        }
        Err(e) => println!("  watchdog_error: {}", e),
    }
}
