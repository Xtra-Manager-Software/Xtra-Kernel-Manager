pub mod charging;
pub mod display;
pub mod dnd;
pub mod power;
pub mod power_hal;
pub mod statusbar;
pub mod task_manager;
pub mod thermal;
pub mod touch;
pub mod vibrator;
pub mod wifi;
pub mod window;

pub use charging::{get_charging_enabled, is_charging_control_supported, set_charging_enabled};
pub use display::{
    apply_refresh_rate, get_active_display_refresh_rate, get_screen_brightness,
    query_binder_supported_rates, set_screen_brightness,
};
pub use dnd::{DndMode, apply_dnd};
pub use power::{is_power_save_mode, set_power_save_mode_enabled};
pub use power_hal::{
    PowerBoost, PowerMode, is_power_mode_supported, set_power_boost, set_power_mode,
};
pub use statusbar::{collapse_panels, is_statusbar_locked, set_statusbar_lock};
pub use task_manager::{
    MemoryTrimLevel, ProcessItem, StandbyBucket, force_stop_package, get_app_standby_bucket,
    get_running_process_list, is_process_frozen, kill_all_background_processes,
    kill_background_processes, kill_pids, set_app_standby_bucket, set_process_memory_trim_level,
};
pub use thermal::{ThermalStatus, get_thermal_headroom, get_thermal_status};
pub use touch::{
    get_high_touch_polling_rate_enabled, is_high_touch_polling_rate_supported,
    set_high_touch_polling_rate_enabled,
};
pub use vibrator::{get_vibrator_capabilities, silence_vibrator};
pub use wifi::{is_wifi_low_latency_active, set_wifi_low_latency_lock};
pub use window::{freeze_display_rotation, is_display_rotation_frozen, thaw_display_rotation};
