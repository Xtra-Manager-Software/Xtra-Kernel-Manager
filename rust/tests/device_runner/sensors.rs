use crate::util;
use std::time::Instant;
use xkm_native::platform::sensors;

pub fn run() {
    println!("\n[sensors]");
    let t0 = Instant::now();
    let sensor_info = sensors::scan_sensor_subsystem();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!(
        "  total_sensors: {}",
        sensor_info.summary.total_sensors_count
    );
    if let Some(ref acc) = sensor_info.summary.accelerometer {
        println!("  accelerometer: {}", acc);
    }
    if let Some(ref mag) = sensor_info.summary.magnetic_field {
        println!("  magnetic_field: {}", mag);
    }
    if let Some(ref gyro) = sensor_info.summary.gyroscope {
        println!("  gyroscope: {}", gyro);
    }
    if let Some(ref ori) = sensor_info.summary.orientation {
        println!("  orientation: {}", ori);
    }
    if let Some(ref light) = sensor_info.summary.light {
        println!("  light: {}", light);
    }
    if let Some(ref prox) = sensor_info.summary.proximity {
        println!("  proximity: {}", prox);
    }

    println!("  input_devices: {} found", sensor_info.input_devices.len());
    for dev in sensor_info.input_devices.iter().take(4) {
        println!(
            "    - {} ({}) -> {}",
            dev.name, dev.device_type, dev.handlers
        );
    }

    println!("  camera_sensors:");
    println!("    rear: {}", sensor_info.camera_sensors.rear_camera);
    println!(
        "    ultra_wide: {}",
        sensor_info.camera_sensors.ultra_wide_lens
    );
    println!("    front: {}", sensor_info.camera_sensors.front_camera);
    println!("    macro: {}", sensor_info.camera_sensors.macro_lens);

    if let Some(rear) = sensor_info.camera_devices.first() {
        println!(
            "  rear_camera_specs: {}MP ({:.1}MP eff), {}, f/1.8, OIS: {}, EIS: {}, API: {}",
            rear.megapixels,
            rear.effective_megapixels,
            rear.resolution,
            rear.ois,
            rear.eis,
            rear.camera2_api_level
        );
    }
}
