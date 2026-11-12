use xkm_native::platform::sensors;
use xkm_native::platform::sensors::camera::{self, CameraSensorsSummary};
use xkm_native::platform::sensors::hardware;

const DUMPSYS_SENSORSERVICE_FIXTURE: &str = r#"
Captured at: 14:24:53.915
Sensor Device:
Total 57 h/w sensors, 57 running 2 disabled clients:
Sensor List:
0x0000000b) bmi26x Accelerometer Non-wakeup | BOSCH           | ver: 65813 | type: android.sensor.accelerometer(1) | perm: n/a | flags: 0x00000980
	continuous | minRate=1.00Hz | maxRate=400.00Hz | FIFO (max,reserved) = (10000, 3000) events | non-wakeUp | 
0x00000015) qmc630x Magnetometer Non-wakeup | QST             | ver: 131329 | type: android.sensor.magnetic_field(2) | perm: n/a | flags: 0x00000880
	continuous | minRate=1.00Hz | maxRate=100.00Hz | FIFO (max,reserved) = (10000, 600) events | non-wakeUp | 
0x0000001f) orientation  Non-wakeup   | xiaomi          | ver: 1 | type: android.sensor.orientation(3) | perm: n/a | flags: 0x00000000
	continuous | minRate=1.00Hz | maxRate=200.00Hz | FIFO (max,reserved) = (10000, 300) events | non-wakeUp | 
0x00000029) bmi26x Gyroscope Non-wakeup | BOSCH           | ver: 65813 | type: android.sensor.gyroscope(4) | perm: n/a | flags: 0x000009c0
	continuous | minRate=1.00Hz | maxRate=400.00Hz | FIFO (max,reserved) = (10000, 0) events | non-wakeUp | 
0x00000033) tcs3701 Ambient Light Sensor Non-wakeup | ams AG          | ver: 256 | type: android.sensor.light(5) | perm: n/a | flags: 0x00000002
	on-change | maxDelay=0us | minDelay=0us | FIFO (max,reserved) = (10000, 0) events | non-wakeUp | 
0x00000051) proximity Proximity Sensor Non-wakeup | XiaoMi(V1.1)    | ver: 1 | type: android.sensor.proximity(8) | perm: n/a | flags: 0x00000002
	on-change | maxDelay=0us | minDelay=0us | FIFO (max,reserved) = (10000, 300) events | non-wakeUp | 
"#;

const DUMPSYS_CAMERA_FIXTURE: &str = r#"
== Camera HAL device device@3.7/legacy/0 (v3.7) static information: ==
      android.lens.facing (80005): byte[1]
        [BACK ]
      android.lens.info.availableApertures (90000): float[1]
        [1.79999995 ]
      android.lens.info.availableFocalLengths (90002): float[1]
        [4.67999983 ]
      android.lens.info.availableOpticalStabilization (90003): byte[2]
        [0 1 ]
      android.sensor.info.physicalSize (f0005): float[2]
        [6.49209595 4.87468815 ]
      android.sensor.info.pixelArraySize (f0006): int32[2]
        [4624 3472 ]
      android.info.supportedHardwareLevel (150000): byte[1]
        [3 ]
"#;

#[test]
fn test_sensorservice_parser() {
    let (summary, list) = hardware::parse_sensorservice_output(DUMPSYS_SENSORSERVICE_FIXTURE);
    assert_eq!(list.len(), 6);
    assert_eq!(summary.accelerometer.as_deref(), Some("Bosch BMI26X"));
    assert_eq!(summary.magnetic_field.as_deref(), Some("QST QMC630X"));
    assert_eq!(summary.orientation.as_deref(), Some("xiaomi Orientation"));
    assert_eq!(summary.gyroscope.as_deref(), Some("Bosch BMI26X"));
    assert_eq!(summary.light.as_deref(), Some("AMS AG TCS3701 AMBIENT"));
    assert_eq!(
        summary.proximity.as_deref(),
        Some("XiaoMi(V1.1) Proximity Proximity Sensor")
    );

    let acc = &list[0];
    assert_eq!(acc.name, "bmi26x Accelerometer");
    assert_eq!(acc.vendor, "BOSCH");
    assert_eq!(acc.type_id, 1);
    assert_eq!(acc.version, 65813);
    assert_eq!(acc.reporting_mode, "continuous");
    assert_eq!(acc.min_rate_hz, Some(1.0));
    assert_eq!(acc.max_rate_hz, Some(400.0));
    assert_eq!(acc.fifo_max_events, Some(10000));
    assert_eq!(acc.fifo_reserved_events, Some(3000));
}

#[test]
fn test_camera_dumpsys_parser() {
    let summary = CameraSensorsSummary {
        rear_camera: "OmniVision OV64B40".to_string(),
        ultra_wide_lens: "Sony IMX355".to_string(),
        front_camera: "Samsung S5K3P9SP04".to_string(),
        macro_lens: "GalaxyCore GC02M1".to_string(),
    };
    let devices = camera::parse_camera_dumpsys(DUMPSYS_CAMERA_FIXTURE, &summary);
    assert_eq!(devices.len(), 1);
    assert_eq!(devices[0].role, "Rear camera 1");
    assert_eq!(devices[0].sensor_model, "OmniVision OV64B40");
    assert_eq!(devices[0].megapixels, 64.0);
    assert_eq!(devices[0].effective_megapixels, 16.1);
    assert_eq!(devices[0].resolution, "4624x3472");
    assert_eq!(devices[0].aperture, "f/1.8");
    assert_eq!(devices[0].focal_length_mm, 4.68);
    assert!(devices[0].ois);
    assert_eq!(devices[0].camera2_api_level, "Level 3");
}

#[test]
fn test_full_sensor_scan_serialization() {
    let full = sensors::scan_sensor_subsystem();
    let json = serde_json::to_string(&full).expect("Serialize full sensor info");
    assert!(json.contains("camera_sensors"));
    assert!(json.contains("hardware_sensors"));
    assert!(json.contains("input_devices"));
}
