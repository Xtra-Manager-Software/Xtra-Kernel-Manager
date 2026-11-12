pub mod runtime;
pub mod types;

pub use runtime::*;
pub use types::SystemSubsystemInfo;

use std::fs;

use crate::platform::common;

pub fn scan_system_subsystem() -> SystemSubsystemInfo {
    let kernel_version = match fs::read_to_string("/proc/version") {
        Ok(v) => v.trim().to_string(),
        Err(_) => String::new(),
    };

    let brand = common::get_system_property("ro.product.brand")
        .or_else(|| common::get_system_property("ro.product.vendor.brand"))
        .unwrap_or_default();

    let hardware = common::get_system_property("ro.hardware")
        .or_else(|| common::get_system_property("ro.boot.hardware"))
        .unwrap_or_default();

    let soc_manufacturer = common::get_system_property("ro.soc.manufacturer").unwrap_or_default();

    let soc_model = common::get_system_property("ro.soc.model")
        .or_else(|| common::get_system_property("ro.board.platform"))
        .unwrap_or_default();

    let release_str = common::get_system_property("ro.build.version.release").unwrap_or_default();

    let sdk_level = common::get_system_property("ro.build.version.sdk")
        .and_then(|s| s.parse::<u32>().ok())
        .unwrap_or_default();

    let os_name = match release_str.is_empty() {
        false => format!("Android {}", release_str),
        true => String::new(),
    };

    let os_codename = match release_str.as_str() {
        "17" => "Cinnamon Bun".to_string(),
        "16" => "Baklava".to_string(),
        "15" => "Vanilla Ice Cream".to_string(),
        "14" => "Upside Down Cake".to_string(),
        "13" => "Tiramisu".to_string(),
        _ => String::new(),
    };

    let api_level_full = (sdk_level as u64) * 100_000;

    let miui_incremental =
        common::get_system_property("ro.build.version.incremental").unwrap_or_default();

    let fingerprint = common::get_system_property("ro.build.fingerprint").unwrap_or_default();

    let os_version_full = match fingerprint.split(':').nth(1) {
        Some(part) => match part.split('/').nth(1) {
            Some(v) => v.to_string(),
            None => common::get_system_property("ro.mi.os.version.name").unwrap_or_default(),
        },
        None => common::get_system_property("ro.mi.os.version.name").unwrap_or_default(),
    };

    let security_patch =
        common::get_system_property("ro.build.version.security_patch").unwrap_or_default();

    let build_id = common::get_system_property("ro.build.id").unwrap_or_default();

    let build_type = common::get_system_property("ro.build.type").unwrap_or_default();

    let build_date = match common::get_system_property("ro.build.date.utc") {
        Some(utc) => match utc.parse::<i64>() {
            Ok(ts) => ts.to_string(),
            Err(_) => common::get_system_property("ro.build.date").unwrap_or_default(),
        },
        None => common::get_system_property("ro.build.date").unwrap_or_default(),
    };

    let instruction_sets = match common::get_system_property("ro.product.cpu.abilist") {
        Some(list) => list.split(',').map(|s| s.trim().to_string()).collect(),
        None => vec!["arm64-v8a".to_string()],
    };

    let project_treble = match common::get_system_property("ro.treble.enabled") {
        Some(v) => v == "true" || v == "1",
        None => true,
    };

    let active_slot = common::get_system_property("ro.boot.slot_suffix")
        .or_else(|| common::get_system_property("ro.boot.slot"))
        .unwrap_or_default();

    let verified_boot_version = common::get_system_property("ro.boot.avb_version")
        .or_else(|| common::get_system_property("ro.boot.vbmeta.avb_version"))
        .unwrap_or_default();

    let verified_boot_state = match common::get_system_property("ro.boot.verifiedbootstate") {
        Some(s) => s,
        None => match common::get_system_property("ro.boot.flash.locked") {
            Some(f) if f == "1" => "green (locked)".to_string(),
            Some(_) => "orange (unlocked)".to_string(),
            None => String::new(),
        },
    };

    let dm_verity = common::get_system_property("ro.boot.veritymode")
        .or_else(|| common::get_system_property("ro.boot.vbmeta.device_state"))
        .unwrap_or_default();

    let bootloader = match common::get_system_property("ro.boot.flash.locked") {
        Some(f) if f == "1" => "Locked".to_string(),
        Some(f) if f == "0" => "Unlocked".to_string(),
        _ => match common::get_system_property("ro.secure") {
            Some(s) if s == "0" => "Unlocked (Insecure)".to_string(),
            _ => "Unlocked".to_string(),
        },
    };

    let dynamic_partitions = match common::get_system_property("ro.boot.dynamic_partitions") {
        Some(v) => v == "true" || v == "1",
        None => false,
    };

    let toybox_version = query_live_toybox_version();
    let java_vm = query_live_art_version();
    let ssl_version = query_live_openssl_version();
    let gms_version = query_live_gms_version();

    let timezone = common::get_system_property("persist.sys.timezone")
        .or_else(|| common::get_system_property("ro.boot.timezone"))
        .unwrap_or_default();

    let language = common::get_system_property("persist.sys.locale").unwrap_or_default();

    let usb_debugging = match common::get_system_property("persist.sys.usb.config") {
        Some(c) => c.contains("adb"),
        None => false,
    };

    let (spatial_audio, head_tracking) = query_live_spatial_audio_and_head_tracking();

    let project_mainline = match common::get_system_property("ro.apex.updatable") {
        Some(v) => v == "true" || v == "1",
        None => std::path::Path::new("/apex").exists(),
    };

    let seamless_updates = match common::get_system_property("ro.build.ab_update") {
        Some(v) => v == "true" || v == "1",
        None => !active_slot.is_empty(),
    };

    let developer_options = query_developer_options();

    SystemSubsystemInfo {
        brand,
        hardware,
        soc_manufacturer,
        soc_model,
        os_name,
        os_codename,
        api_level: sdk_level,
        api_level_full,
        release_or_codename: release_str,
        miui_version: miui_incremental,
        os_version_full,
        security_patch,
        build_id,
        build_type,
        build_date,
        fingerprint,
        kernel_version,
        instruction_sets,
        spatial_audio,
        head_tracking,
        project_treble,
        project_mainline,
        dynamic_partitions,
        seamless_updates,
        active_slot,
        verified_boot_version,
        verified_boot_state,
        dm_verity,
        bootloader_status: bootloader,
        google_play_services: gms_version,
        toybox_version,
        java_vm,
        ssl_version,
        language,
        timezone,
        usb_debugging,
        developer_options,
    }
}
