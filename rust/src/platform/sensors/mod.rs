pub mod camera;
pub mod camera_dumpsys;
pub mod camera_types;
pub mod hardware;
pub mod input;

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FullSensorInfo {
    pub summary: hardware::PhysicalSensorsSummary,
    pub hardware_sensors: Vec<hardware::PhysicalSensor>,
    pub input_devices: Vec<input::InputDevice>,
    pub camera_sensors: camera::CameraSensorsSummary,
    pub camera_devices: Vec<camera::CameraDeviceDetails>,
}

pub fn scan_sensor_subsystem() -> FullSensorInfo {
    let (summary, hardware_sensors) = hardware::scan_hardware_sensors();
    let input_devices = input::scan_input_devices();
    let (camera_sensors, camera_devices) = camera::scan_camera_subsystem();

    FullSensorInfo {
        summary,
        hardware_sensors,
        input_devices,
        camera_sensors,
        camera_devices,
    }
}
