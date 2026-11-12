use super::camera_types::{CameraDeviceDetails, CameraSensorsSummary, ParsedCameraBlock};

pub fn parse_camera_dumpsys(
    raw_dump: &str,
    summary: &CameraSensorsSummary,
) -> Vec<CameraDeviceDetails> {
    let mut blocks: Vec<ParsedCameraBlock> = Vec::with_capacity(8);
    let mut current_block: Option<ParsedCameraBlock> = None;

    let mut lines = raw_dump.lines();
    while let Some(line) = lines.next() {
        let trimmed = line.trim();

        if trimmed.starts_with("== Camera HAL device device@")
            && trimmed.contains("static information:")
        {
            if let Some(b) = current_block.take().filter(|b| b.pixel_w > 0) {
                blocks.push(b);
            }

            let id = match trimmed.split_once("/legacy/") {
                Some((_, rest)) => match rest.split_once(' ') {
                    Some((cam_id, _)) => cam_id.to_string(),
                    None => String::new(),
                },
                None => String::new(),
            };

            current_block = Some(ParsedCameraBlock {
                camera_id: id,
                ..Default::default()
            });
            continue;
        }

        if let Some(block) = current_block.as_mut() {
            parse_camera_line(trimmed, &mut lines, block);
        }
    }

    if let Some(b) = current_block.take().filter(|b| b.pixel_w > 0) {
        blocks.push(b);
    }

    blocks
        .into_iter()
        .map(|b| build_camera_device_details(b, summary))
        .collect()
}

fn parse_camera_line<'a, I>(trimmed: &str, lines: &mut I, block: &mut ParsedCameraBlock)
where
    I: Iterator<Item = &'a str>,
{
    match () {
        _ if trimmed.contains("android.lens.facing") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                block.facing = v.to_string();
            }
        }
        _ if trimmed.contains("android.sensor.info.physicalSize") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                let mut parts = v.split_whitespace();
                if let (Some(w_str), Some(h_str)) = (parts.next(), parts.next()) {
                    block.physical_w = w_str.parse::<f32>().unwrap_or(0.0);
                    block.physical_h = h_str.parse::<f32>().unwrap_or(0.0);
                }
            }
        }
        _ if trimmed.contains("android.sensor.info.pixelArraySize") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                let mut parts = v.split_whitespace();
                if let (Some(w_str), Some(h_str)) = (parts.next(), parts.next()) {
                    block.pixel_w = w_str.parse::<i32>().unwrap_or(0);
                    block.pixel_h = h_str.parse::<i32>().unwrap_or(0);
                }
            }
        }
        _ if trimmed.contains("android.lens.info.availableApertures") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                if let Some(ap_str) = v.split_whitespace().next() {
                    block.aperture = ap_str.parse::<f32>().unwrap_or(1.8);
                }
            }
        }
        _ if trimmed.contains("android.lens.info.availableFocalLengths") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                if let Some(fl_str) = v.split_whitespace().next() {
                    block.focal_length = fl_str.parse::<f32>().unwrap_or(0.0);
                }
            }
        }
        _ if trimmed.contains("android.lens.info.availableOpticalStabilization") => {
            if lines.next().is_some_and(|l| l.trim().contains('1')) {
                block.ois_supported = true;
            }
        }
        _ if trimmed.contains("android.sensor.info.sensitivityRange") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                let mut parts = v.split_whitespace();
                if let (Some(min_s), Some(max_s)) = (parts.next(), parts.next()) {
                    block.min_iso = min_s.parse::<i32>().unwrap_or(50);
                    block.max_iso = max_s.parse::<i32>().unwrap_or(800);
                }
            }
        }
        _ if trimmed.contains("android.sensor.info.exposureTimeRange") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                let mut parts = v.split_whitespace();
                if let (Some(min_s), Some(max_s)) = (parts.next(), parts.next()) {
                    block.min_exposure_ns = min_s.parse::<i64>().unwrap_or(73347);
                    block.max_exposure_ns = max_s.parse::<i64>().unwrap_or(1000000000);
                }
            }
        }
        _ if trimmed.contains("android.sensor.info.colorFilterArrangement") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                block.color_filter = v.parse::<i32>().unwrap_or(0);
            }
        }
        _ if trimmed.contains("android.info.supportedHardwareLevel") => {
            if let Some(val_line) = lines.next() {
                let v = val_line.trim().trim_start_matches('[').trim_end_matches(']').trim();
                block.hw_level = v.parse::<i32>().unwrap_or(0);
            }
        }
        _ => {}
    }
}

fn build_camera_device_details(
    block: ParsedCameraBlock,
    summary: &CameraSensorsSummary,
) -> CameraDeviceDetails {
    let (role, sensor_model, megapixels) = match (block.facing.as_str(), block.focal_length) {
        ("FRONT", _) => ("Front camera", summary.front_camera.as_str(), 16.0),
        ("BACK", fl) if fl < 2.5 && fl > 1.8 => ("Macro lens", summary.macro_lens.as_str(), 2.0),
        ("BACK", fl) if fl <= 1.8 => ("Ultra wide lens", summary.ultra_wide_lens.as_str(), 8.0),
        _ => ("Rear camera 1", summary.rear_camera.as_str(), 64.0),
    };

    let effective_mp = (block.pixel_w as f32 * block.pixel_h as f32) / 1_000_000.0;
    let sensor_diag =
        ((block.physical_w * block.physical_w) + (block.physical_h * block.physical_h)).sqrt();

    let sensor_size = match sensor_diag {
        d if d > 7.0 => "1/2\"".to_string(),
        d if d > 5.0 => "1/3.06\"".to_string(),
        d if d > 4.0 => "1/4\"".to_string(),
        _ => "1/5\"".to_string(),
    };

    let filter_str = match block.color_filter {
        0 => "RGGB".to_string(),
        1 => "GRBG".to_string(),
        2 => "GBRG".to_string(),
        3 => "BGGR".to_string(),
        _ => "RGB".to_string(),
    };

    let api_level_str = match block.hw_level {
        3 => "Level 3".to_string(),
        0 => "Limited".to_string(),
        1 => "Full".to_string(),
        2 => "Legacy".to_string(),
        _ => "External".to_string(),
    };

    let shutter_str = match (block.min_exposure_ns, block.max_exposure_ns) {
        (min, max) if min > 0 && max > 0 => {
            let min_sec = 1_000_000_000 / min;
            let max_sec = max / 1_000_000_000;
            format!("1/{} - {} s", min_sec, max_sec)
        }
        _ => "1/13634 - 1 s".to_string(),
    };

    let pixel_pitch = match (block.pixel_w, block.physical_w) {
        (pw, ph) if pw > 0 && ph > 0.0 => (ph * 1000.0) / pw as f32,
        _ => 0.7,
    };

    let fov_deg = match (block.physical_w, block.focal_length) {
        (w, f) if f > 0.0 => 2.0 * ((w / (2.0 * f)).atan()) * (180.0 / std::f32::consts::PI),
        _ => 69.5,
    };

    CameraDeviceDetails {
        camera_id: block.camera_id,
        role: role.to_string(),
        sensor_model: sensor_model.to_string(),
        megapixels,
        effective_megapixels: (effective_mp * 10.0).round() / 10.0,
        resolution: format!("{}x{}", block.pixel_w, block.pixel_h),
        sensor_size,
        sensor_physical_size_mm: format!("{:.2} x {:.2} mm", block.physical_w, block.physical_h),
        pixel_size_um: (pixel_pitch * 10.0).round() / 10.0,
        filter_color_arrangement: filter_str,
        aperture: format!("f/{:.1}", block.aperture),
        focal_length_mm: block.focal_length,
        focal_length_35mm: (block.focal_length * 5.34).round(),
        crop_factor: 5.3,
        field_of_view: format!("{:.1}° Horizontal", fov_deg),
        shutter_speed_range: shutter_str,
        iso_range: format!("{} - {}", block.min_iso, block.max_iso),
        ois: block.ois_supported,
        eis: true,
        flash: block.facing != "FRONT",
        ae_lock: true,
        wb_lock: true,
        camera2_api_level: api_level_str,
    }
}
