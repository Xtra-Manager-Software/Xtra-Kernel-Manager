pub mod actuators;
pub mod client;

pub use actuators::{
    apply_dnd, apply_refresh_rate, collapse_panels, freeze_display_rotation,
    get_active_display_refresh_rate, get_screen_brightness, get_vibrator_capabilities,
    is_display_rotation_frozen, is_power_mode_supported, is_power_save_mode,
    is_statusbar_locked, is_wifi_low_latency_active, query_binder_supported_rates,
    set_power_boost, set_power_mode, set_power_save_mode_enabled, set_screen_brightness,
    set_statusbar_lock, set_wifi_low_latency_lock, silence_vibrator, thaw_display_rotation,
    DndMode, PowerBoost, PowerMode,
};
pub use client::{get_foreground_app, ForegroundAppInfo};

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
