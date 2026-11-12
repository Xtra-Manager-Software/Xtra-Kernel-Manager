pub mod telephony;
pub mod types;
pub mod wifi;

pub use telephony::scan_telephony;
pub use types::{NetworkSubsystemInfo, SimCardInfo, TelephonyInfo, WifiInfo};
pub use wifi::scan_wifi;

pub fn scan_network_subsystem() -> NetworkSubsystemInfo {
    let wifi = scan_wifi();
    let telephony = scan_telephony();

    NetworkSubsystemInfo { wifi, telephony }
}
