use std::time::Instant;
use xkm_native::platform::binder;
use crate::util;

pub fn run() {
    println!("\n[binder_actuators]");
    let t0 = Instant::now();
    let binder_init = binder::ensure_initialized();
    let refresh_rate = binder::get_active_display_refresh_rate();
    let supported_rates = binder::query_binder_supported_rates();
    let brightness = binder::get_screen_brightness();
    let battery_saver = binder::is_power_save_mode().unwrap_or(false);
    let game_mode = binder::is_power_mode_supported(binder::PowerMode::Game);
    let sustained_perf = binder::is_power_mode_supported(binder::PowerMode::SustainedPerformance);
    let exp_render = binder::is_power_mode_supported(binder::PowerMode::ExpensiveRendering);
    let wifi_low_latency = binder::is_wifi_low_latency_active();
    let statusbar_locked = binder::is_statusbar_locked();
    let rotation_frozen = binder::is_display_rotation_frozen(0);
    let proc_list = binder::get_running_process_list().unwrap_or_default();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!("  init_success: {}", binder_init);
    println!("  active_refresh_rate: {:.1} Hz", refresh_rate);
    println!("  supported_refresh_rates: {:?}", supported_rates);
    println!("  screen_brightness: {:?}", brightness);
    println!("  battery_saver: {:?}", battery_saver);
    println!("  power_mode_game: {}", game_mode);
    println!("  power_mode_sustained_perf: {}", sustained_perf);
    println!("  power_mode_expensive_rendering: {}", exp_render);
    println!("  wifi_low_latency: {}", wifi_low_latency);
    println!("  statusbar_locked: {}", statusbar_locked);
    println!("  rotation_frozen: {}", rotation_frozen);

    println!("  running_processes: {}", proc_list.len());
    for p in proc_list.iter().take(4) {
        println!(
            "    pid {:<5} [{}] rss={}MB, state={}, cgroup={}",
            p.pid,
            p.package_name,
            p.rss_kb / 1024,
            p.state,
            p.cgroup
        );
    }

    let t_touch = Instant::now();
    let touch_supp = binder::is_high_touch_polling_rate_supported();
    let touch_state = binder::get_high_touch_polling_rate_enabled();
    let _ = binder::set_high_touch_polling_rate_enabled(true);
    let enabled_state = binder::get_high_touch_polling_rate_enabled();
    let _ = binder::set_high_touch_polling_rate_enabled(false);
    println!(
        "  touch_polling: supp={}, initial={:?}, set_on={:?} [took {}]",
        touch_supp,
        touch_state,
        enabled_state,
        util::fmt_duration(t_touch.elapsed())
    );

    let t_charge = Instant::now();
    let charging_supp = binder::is_charging_control_supported();
    let charging_enabled = binder::get_charging_enabled();
    println!(
        "  charging_control: supp={}, enabled={:?} [took {}]",
        charging_supp,
        charging_enabled,
        util::fmt_duration(t_charge.elapsed())
    );
}
