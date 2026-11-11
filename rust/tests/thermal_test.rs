use xkm_native::platform::thermal::sconfig::XiaomiThermalPreset;
use xkm_native::platform::thermal::zones::{read_cpu_temperature, scan_thermal_zones};

#[test]
fn test_xiaomi_thermal_preset_mapping() {
    assert_eq!(XiaomiThermalPreset::Class0.index(), 11);
    assert_eq!(XiaomiThermalPreset::Extreme.index(), 2);
    assert_eq!(XiaomiThermalPreset::Dynamic.index(), 10);
    assert_eq!(XiaomiThermalPreset::Incalls.index(), 8);
    assert_eq!(XiaomiThermalPreset::Thermal20.index(), 20);
}

#[test]
fn test_scan_thermal_zones() {
    let summary = scan_thermal_zones();
    println!("Scanned {} thermal zones", summary.zones.len());
    if let Some(cpu_t) = summary.cpu_temp_c {
        println!("CPU Temp: {} C", cpu_t);
        assert!((0.0..120.0).contains(&cpu_t));
    }
    if let Some(gpu_t) = summary.gpu_temp_c {
        println!("GPU Temp: {} C", gpu_t);
        assert!((0.0..120.0).contains(&gpu_t));
    }
}

#[test]
fn test_read_cpu_temperature() {
    let temp = read_cpu_temperature();
    assert!(temp >= 0.0);
}
