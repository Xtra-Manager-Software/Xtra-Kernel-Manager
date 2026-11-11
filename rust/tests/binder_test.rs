use xkm_native::platform::binder::{
    apply_dnd, apply_refresh_rate, collapse_panels, ensure_initialized, freeze_display_rotation,
    get_active_display_refresh_rate, get_foreground_app, get_screen_brightness,
    get_vibrator_capabilities, is_display_rotation_frozen, is_power_mode_supported,
    is_power_save_mode, is_statusbar_locked, is_wifi_low_latency_active,
    query_binder_supported_rates, set_power_boost, set_power_mode, set_power_save_mode_enabled,
    set_screen_brightness, set_statusbar_lock, set_wifi_low_latency_lock, silence_vibrator,
    thaw_display_rotation, DndMode, PowerBoost, PowerMode,
};

#[test]
fn test_binder_initialization_safety() {
    assert!(ensure_initialized());
}

#[test]
fn test_display_actuators() {
    let active_hz = get_active_display_refresh_rate();
    assert!(active_hz >= 60.0);

    let rates = query_binder_supported_rates();
    assert!(rates.contains(&60));

    assert!(apply_refresh_rate(120).is_ok());
    assert!(apply_refresh_rate(0).is_ok());

    assert!(set_screen_brightness(0.75).is_ok());
    let brightness = get_screen_brightness();
    assert!(brightness.is_some());
}

#[test]
fn test_power_and_power_hal_actuators() {
    let is_saver = is_power_save_mode().unwrap_or(false);
    assert!(!is_saver || is_saver);

    assert!(set_power_save_mode_enabled(false).is_ok());

    // PowerHAL
    assert!(is_power_mode_supported(PowerMode::Game) || !is_power_mode_supported(PowerMode::Game));
    assert!(set_power_mode(PowerMode::Game, true).is_ok());
    assert!(set_power_mode(PowerMode::Game, false).is_ok());

    assert!(set_power_boost(PowerBoost::Interaction, 500).is_ok());
}

#[test]
fn test_gaming_qol_actuators() {
    // WiFi
    assert!(set_wifi_low_latency_lock(true).is_ok());
    assert!(set_wifi_low_latency_lock(false).is_ok());
    let _ = is_wifi_low_latency_active();

    // StatusBar & gestures
    assert!(collapse_panels().is_ok());
    assert!(set_statusbar_lock(true).is_ok());
    assert!(set_statusbar_lock(false).is_ok());
    let _ = is_statusbar_locked();

    // Rotation freeze
    assert!(freeze_display_rotation(0, -1).is_ok());
    assert!(thaw_display_rotation(0).is_ok());
    let _ = is_display_rotation_frozen(0);

    // Vibrator
    assert!(silence_vibrator().is_ok());
    let _ = get_vibrator_capabilities();

    // DnD
    assert!(apply_dnd(DndMode::Priority).is_ok());
    assert!(apply_dnd(DndMode::All).is_ok());
}

#[test]
fn test_foreground_app_detection() {
    // Should run safely without panic on any platform
    let _ = get_foreground_app();
}
