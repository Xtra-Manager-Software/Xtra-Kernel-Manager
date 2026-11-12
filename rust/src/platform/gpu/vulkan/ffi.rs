pub use super::structs::*;

pub fn format_uuid(bytes: &[u8; 16]) -> String {
    format!(
        "{:02x}{:02x}{:02x}{:02x}-{:02x}{:02x}-{:02x}{:02x}-{:02x}{:02x}-{:02x}{:02x}{:02x}{:02x}{:02x}{:02x}",
        bytes[0],
        bytes[1],
        bytes[2],
        bytes[3],
        bytes[4],
        bytes[5],
        bytes[6],
        bytes[7],
        bytes[8],
        bytes[9],
        bytes[10],
        bytes[11],
        bytes[12],
        bytes[13],
        bytes[14],
        bytes[15]
    )
}

pub fn max_sample_count(flags: u32) -> u32 {
    match () {
        _ if flags & 0x40 != 0 => 64,
        _ if flags & 0x20 != 0 => 32,
        _ if flags & 0x10 != 0 => 16,
        _ if flags & 0x08 != 0 => 8,
        _ if flags & 0x04 != 0 => 4,
        _ if flags & 0x02 != 0 => 2,
        _ => 1,
    }
}
