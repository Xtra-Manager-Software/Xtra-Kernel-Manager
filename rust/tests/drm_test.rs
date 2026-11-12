use xkm_native::platform::drm;

#[test]
fn test_scan_drm_security_subsystem() {
    let info = drm::scan_drm_security_subsystem();
    // On host or Android, it should not panic and return well-formed struct
    assert!(!info.widevine_security_level.is_empty());
}
