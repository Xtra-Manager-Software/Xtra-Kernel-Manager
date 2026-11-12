use xkm_native::platform::binder::{
    MemoryTrimLevel, ProcessItem, StandbyBucket, ThermalStatus, force_stop_package,
    get_app_standby_bucket, get_charging_enabled, get_high_touch_polling_rate_enabled,
    get_running_process_list, get_thermal_headroom, get_thermal_status,
    is_charging_control_supported, is_high_touch_polling_rate_supported, is_process_frozen,
    kill_all_background_processes, kill_background_processes, kill_pids, set_app_standby_bucket,
    set_charging_enabled, set_high_touch_polling_rate_enabled, set_process_memory_trim_level,
};

#[test]
fn test_standby_bucket_conversion() {
    assert_eq!(StandbyBucket::from_i32(10), StandbyBucket::Active);
    assert_eq!(StandbyBucket::from_i32(20), StandbyBucket::WorkingSet);
    assert_eq!(StandbyBucket::from_i32(30), StandbyBucket::Frequent);
    assert_eq!(StandbyBucket::from_i32(40), StandbyBucket::Rare);
    assert_eq!(StandbyBucket::from_i32(45), StandbyBucket::Restricted);
    assert_eq!(StandbyBucket::from_i32(50), StandbyBucket::Never);
    assert_eq!(StandbyBucket::from_i32(999), StandbyBucket::Active);
}

#[test]
fn test_thermal_status_throttling() {
    assert!(!ThermalStatus::None.is_throttling());
    assert!(!ThermalStatus::Light.is_throttling());
    assert!(!ThermalStatus::Moderate.is_throttling());
    assert!(ThermalStatus::Severe.is_throttling());
    assert!(ThermalStatus::Critical.is_throttling());
    assert!(ThermalStatus::Emergency.is_throttling());
    assert!(ThermalStatus::Shutdown.is_throttling());

    assert_eq!(ThermalStatus::from_i32(0), ThermalStatus::None);
    assert_eq!(ThermalStatus::from_i32(3), ThermalStatus::Severe);
    assert_eq!(ThermalStatus::from_i32(4), ThermalStatus::Critical);
}

#[test]
fn test_running_process_list_native() {
    let list = get_running_process_list();
    assert!(
        list.is_ok(),
        "Failed to read running process list: {:?}",
        list.err()
    );
    let processes = list.unwrap();
    assert!(
        !processes.is_empty(),
        "Process list should not be empty on Linux/Android"
    );

    // Verify ordering by RSS descending
    for window in processes.windows(2) {
        assert!(
            window[0].rss_kb >= window[1].rss_kb,
            "Processes should be sorted by RSS descending: {} vs {}",
            window[0].rss_kb,
            window[1].rss_kb
        );
    }

    // Verify serialization
    let json = serde_json::to_string(&processes);
    assert!(json.is_ok());
    let deserialized: Result<Vec<ProcessItem>, _> = serde_json::from_str(&json.unwrap());
    assert!(deserialized.is_ok());
}

#[test]
fn test_task_manager_actuators_lifecycle() {
    // Basic calls should succeed without panicking
    assert!(kill_background_processes("com.test.app", None).is_ok());
    assert!(kill_all_background_processes().is_ok());
    assert!(force_stop_package("com.test.app", None).is_ok());
    assert!(kill_pids(&[99999], "test").is_ok());
    assert!(!is_process_frozen(1).unwrap_or(true));
    assert!(
        set_process_memory_trim_level("com.test.app", 1000, MemoryTrimLevel::RunningCritical)
            .is_ok()
    );
    assert_eq!(
        get_app_standby_bucket("com.test.app", None).unwrap(),
        StandbyBucket::Active
    );
    assert!(set_app_standby_bucket("com.test.app", StandbyBucket::Restricted, None).is_ok());
}

#[test]
fn test_touch_and_charging_actuators() {
    assert!(is_high_touch_polling_rate_supported());
    assert!(set_high_touch_polling_rate_enabled(true).is_ok());
    assert!(get_high_touch_polling_rate_enabled().unwrap());
    assert!(set_high_touch_polling_rate_enabled(false).is_ok());
    assert!(!get_high_touch_polling_rate_enabled().unwrap());

    assert!(is_charging_control_supported());
    assert!(set_charging_enabled(false).is_ok()); // Bypass mode
    assert!(!get_charging_enabled().unwrap());
    assert!(set_charging_enabled(true).is_ok());
    assert!(get_charging_enabled().unwrap());

    assert_eq!(get_thermal_status().unwrap(), ThermalStatus::None);
    assert!(get_thermal_headroom(0).unwrap() >= 0.0);
}
