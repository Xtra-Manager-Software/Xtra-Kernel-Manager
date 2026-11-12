use xkm_native::platform::binder::{
    check_recent_kernel_crashes, get_storage_life_time, reboot_system, run_fstrim,
};

#[test]
fn test_storage_and_crash_apis_host() {
    // Calling fstrim should succeed on host mock without panicking
    assert!(run_fstrim().is_ok());

    // Checking crash entries should succeed without panicking
    let crashes = check_recent_kernel_crashes().unwrap();
    println!("Found {} crash entries on test system", crashes.len());

    // Host reboot mock should succeed without error
    assert!(reboot_system("userspace").is_ok());

    // Storage lifetime on non-android without hardware nodes returns error gracefully
    let _ = get_storage_life_time();
}
