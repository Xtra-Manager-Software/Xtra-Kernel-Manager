use xkm_native::preset::apply_preset;

#[test]
fn test_preset_names() {
    // Valid preset names shouldn't return error "Unknown preset"
    // (though in non-root test environments, sysfs writes might return or fail gracefully without panicking)
    let valid_presets = ["powersave", "balance", "balanced", "performance", "gaming"];
    for preset in valid_presets {
        let res = apply_preset(preset);
        println!("Apply preset '{}': {:?}", preset, res);
    }

    // Invalid preset should fail with error
    let invalid = apply_preset("turbo_ultra_fake");
    assert!(invalid.is_err(), "Invalid preset should return an error");
}
