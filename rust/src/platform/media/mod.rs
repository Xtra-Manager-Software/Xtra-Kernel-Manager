pub mod audio;
pub mod types;
pub mod video;
pub mod video_updater;

pub use audio::{classify_audio_codec, query_live_audio_specs};
pub use types::{AudioSpecs, MediaCodecInfo, VideoCodecCapability};
pub use video::{parse_media_codecs, scan_media_codecs_from_files};

use std::process::Command;

pub fn scan_media_codecs_subsystem() -> MediaCodecInfo {
    let output = match Command::new("dumpsys").arg("media.player").output() {
        Ok(out) => match out.status.success() {
            true => String::from_utf8_lossy(&out.stdout).into_owned(),
            false => String::new(),
        },
        Err(_) => String::new(),
    };

    match output.is_empty() {
        false => parse_media_codecs(&output),
        true => scan_media_codecs_from_files(),
    }
}
