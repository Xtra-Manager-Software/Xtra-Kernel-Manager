pub mod actuators;
pub mod client;

pub use actuators::{
    DndMode, MemoryTrimLevel, PowerBoost, PowerMode, ProcessItem, StandbyBucket, ThermalStatus,
    apply_dnd, apply_refresh_rate, collapse_panels, force_stop_package, freeze_display_rotation,
    get_active_display_refresh_rate, get_app_standby_bucket, get_charging_enabled,
    get_high_touch_polling_rate_enabled, get_running_process_list, get_screen_brightness,
    get_thermal_headroom, get_thermal_status, get_vibrator_capabilities,
    is_charging_control_supported, is_display_rotation_frozen,
    is_high_touch_polling_rate_supported, is_power_mode_supported, is_power_save_mode,
    is_process_frozen, is_statusbar_locked, is_wifi_low_latency_active,
    kill_all_background_processes, kill_background_processes, kill_pids,
    query_binder_supported_rates, set_app_standby_bucket, set_charging_enabled,
    set_high_touch_polling_rate_enabled, set_power_boost, set_power_mode,
    set_power_save_mode_enabled, set_process_memory_trim_level, set_screen_brightness,
    set_statusbar_lock, set_wifi_low_latency_lock, silence_vibrator, thaw_display_rotation,
};
pub use client::{ForegroundAppInfo, get_foreground_app};

/// Ensure Binder ProcessState is initialized on Android platforms.
pub fn ensure_initialized() -> bool {
    #[cfg(target_os = "android")]
    {
        if !rsbinder::ProcessState::is_initialized()
            && rsbinder::ProcessState::init_default().is_err()
        {
            return false;
        }
    }
    true
}
