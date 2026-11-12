use crate::util;
use std::time::Instant;
use xkm_native::platform::binder;

pub fn run() {
    println!("\n[storage]");
    let t0 = Instant::now();
    let health = binder::get_storage_life_time();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    match health {
        Ok(h) => {
            println!("  flash_wear_a_user: {}%", h.lifetime_type_a_pct);
            println!("  flash_wear_b_slc: {}%", h.lifetime_type_b_pct);
            println!("  eol_status: {}", h.eol_status);
        }
        Err(e) => println!("  wear_status: unavailable ({})", e),
    }

    let t_trim = Instant::now();
    let trim_res = binder::run_fstrim();
    println!(
        "  fstrim_result: {:?} [took {}]",
        trim_res,
        util::fmt_duration(t_trim.elapsed())
    );
}
