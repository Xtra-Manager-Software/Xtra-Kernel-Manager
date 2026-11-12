use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq, Default)]
pub struct CameraSensorsSummary {
    pub rear_camera: String,
    pub ultra_wide_lens: String,
    pub front_camera: String,
    pub macro_lens: String,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct CameraDeviceDetails {
    pub camera_id: String,
    pub role: String,
    pub sensor_model: String,
    pub megapixels: f32,
    pub effective_megapixels: f32,
    pub resolution: String,
    pub sensor_size: String,
    pub sensor_physical_size_mm: String,
    pub pixel_size_um: f32,
    pub filter_color_arrangement: String,
    pub aperture: String,
    pub focal_length_mm: f32,
    pub focal_length_35mm: f32,
    pub crop_factor: f32,
    pub field_of_view: String,
    pub shutter_speed_range: String,
    pub iso_range: String,
    pub ois: bool,
    pub eis: bool,
    pub flash: bool,
    pub ae_lock: bool,
    pub wb_lock: bool,
    pub camera2_api_level: String,
}

#[derive(Default)]
pub struct ParsedCameraBlock {
    pub camera_id: String,
    pub facing: String,
    pub physical_w: f32,
    pub physical_h: f32,
    pub pixel_w: i32,
    pub pixel_h: i32,
    pub aperture: f32,
    pub focal_length: f32,
    pub ois_supported: bool,
    pub min_iso: i32,
    pub max_iso: i32,
    pub min_exposure_ns: i64,
    pub max_exposure_ns: i64,
    pub color_filter: i32,
    pub hw_level: i32,
}
