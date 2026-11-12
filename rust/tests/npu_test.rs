use xkm_native::platform::npu;

#[test]
fn test_scan_npu_subsystem() {
    let info = npu::scan_npu_subsystem();
    // On host or Android, it should not panic and return well-formed struct
    assert!(!info.vendor_family.is_empty());
    assert!(!info.hardware_state.is_empty());
}
