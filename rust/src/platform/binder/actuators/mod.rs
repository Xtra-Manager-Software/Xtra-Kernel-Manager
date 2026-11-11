pub mod display;
pub mod dnd;
pub mod power;
pub mod power_hal;
pub mod statusbar;
pub mod vibrator;
pub mod wifi;
pub mod window;

pub use display::{
    apply_refresh_rate, get_active_display_refresh_rate, get_screen_brightness,
    query_binder_supported_rates, set_screen_brightness,
};
pub use dnd::{apply_dnd, DndMode};
pub use power::{is_power_save_mode, set_power_save_mode_enabled};
pub use power_hal::{
    is_power_mode_supported, set_power_boost, set_power_mode, PowerBoost, PowerMode,
};
pub use statusbar::{collapse_panels, is_statusbar_locked, set_statusbar_lock};
pub use vibrator::{get_vibrator_capabilities, silence_vibrator};
pub use wifi::{is_wifi_low_latency_active, set_wifi_low_latency_lock};
pub use window::{
    freeze_display_rotation, is_display_rotation_frozen, thaw_display_rotation,
};
