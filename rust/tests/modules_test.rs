use xkm_native::platform::modules::{is_module_protected, list_kernel_drivers, parse_proc_modules};

#[test]
fn test_parse_proc_modules() {
    let sample = r#"
wlan 13189120 0 - Live 0x0000000000000000
qca_cld3 5439488 1 wlan, Live 0x0000000000000000
kernelsu 122880 0 - Live 0x0000000000000000
zram 45056 2 - Live 0x0000000000000000
    "#;

    let modules = parse_proc_modules(sample);
    assert_eq!(modules.len(), 4);

    let wlan = modules.iter().find(|m| m.name == "wlan").unwrap();
    assert_eq!(wlan.size_bytes, 13189120);
    assert_eq!(wlan.instances, 0);
    assert_eq!(wlan.state, "Live");
    assert!(!wlan.is_protected);

    let ksu = modules.iter().find(|m| m.name == "kernelsu").unwrap();
    assert!(ksu.is_protected);

    let qca = modules.iter().find(|m| m.name == "qca_cld3").unwrap();
    assert_eq!(qca.used_by, vec!["wlan"]);
}

#[test]
fn test_is_module_protected() {
    assert!(is_module_protected("kernelsu"));
    assert!(is_module_protected("KernelSU"));
    assert!(is_module_protected("ksud"));
    assert!(is_module_protected("magisk"));
    assert!(is_module_protected("apatch"));
    assert!(is_module_protected("kgsl"));
    assert!(is_module_protected("adreno"));
    assert!(!is_module_protected("wireguard"));
    assert!(!is_module_protected("ntfs3"));
}

#[test]
fn test_list_kernel_drivers() {
    let drivers = list_kernel_drivers();
    // On Linux hosts, this should scan without panicking
    println!("Scanned {} kernel drivers on current system", drivers.len());
}
