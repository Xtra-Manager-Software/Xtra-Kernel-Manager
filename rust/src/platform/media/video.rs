use std::fs;

use super::audio::{classify_audio_codec, query_live_audio_specs};
use super::types::{MediaCodecInfo, VideoCodecCapability};
use super::video_updater::{update_video_hw, update_video_sw};

pub fn parse_media_codecs(dump: &str) -> MediaCodecInfo {
    let mut audio_codecs = Vec::with_capacity(16);
    let mut hw_decoders = Vec::with_capacity(16);

    let mut av1 = VideoCodecCapability {
        name: "AV1".to_string(),
        decode: "SW".to_string(),
        encode: "SW".to_string(),
    };
    let mut vp9 = VideoCodecCapability {
        name: "VP9".to_string(),
        decode: "SW".to_string(),
        encode: "SW".to_string(),
    };
    let mut vp8 = VideoCodecCapability {
        name: "VP8".to_string(),
        decode: "SW".to_string(),
        encode: "SW".to_string(),
    };
    let mut hevc = VideoCodecCapability {
        name: "H.265 / HEVC".to_string(),
        decode: "SW".to_string(),
        encode: "SW".to_string(),
    };
    let mut avc = VideoCodecCapability {
        name: "H.264 / AVC".to_string(),
        decode: "SW".to_string(),
        encode: "SW".to_string(),
    };
    let mut mpeg4 = VideoCodecCapability {
        name: "MPEG-4 Visual".to_string(),
        decode: "SW".to_string(),
        encode: "SW".to_string(),
    };
    let mut h263 = VideoCodecCapability {
        name: "H.263".to_string(),
        decode: "SW".to_string(),
        encode: "SW".to_string(),
    };
    let mut dolby_vision = VideoCodecCapability {
        name: "Dolby Vision".to_string(),
        decode: "-".to_string(),
        encode: "-".to_string(),
    };

    let mut current_media_type = String::new();
    let mut in_decoder = false;
    let mut in_encoder = false;

    for line in dump.lines() {
        let trimmed = line.trim();

        if trimmed.starts_with("Media type '") {
            let mtype = trimmed
                .trim_start_matches("Media type '")
                .trim_end_matches("':")
                .trim();
            current_media_type = mtype.to_string();
            in_decoder = false;
            in_encoder = false;

            classify_audio_codec(mtype, &mut audio_codecs);
        } else if trimmed.starts_with("Decoder \"") {
            in_decoder = true;
            in_encoder = false;

            if trimmed.contains(".qti.")
                || trimmed.contains(".hw.")
                || trimmed.contains(".dolby.")
            {
                if !hw_decoders.contains(&current_media_type) && !current_media_type.is_empty() {
                    hw_decoders.push(current_media_type.clone());
                }
                update_video_hw(
                    &current_media_type,
                    true,
                    true,
                    &mut av1,
                    &mut vp9,
                    &mut vp8,
                    &mut hevc,
                    &mut avc,
                    &mut mpeg4,
                    &mut h263,
                    &mut dolby_vision,
                );
            } else {
                update_video_sw(
                    &current_media_type,
                    true,
                    &mut av1,
                    &mut vp9,
                    &mut vp8,
                    &mut hevc,
                    &mut avc,
                    &mut mpeg4,
                    &mut h263,
                    &mut dolby_vision,
                );
            }
        } else if trimmed.starts_with("Encoder \"") {
            in_encoder = true;
            in_decoder = false;

            if trimmed.contains(".qti.")
                || trimmed.contains(".hw.")
                || trimmed.contains(".dolby.")
            {
                update_video_hw(
                    &current_media_type,
                    false,
                    true,
                    &mut av1,
                    &mut vp9,
                    &mut vp8,
                    &mut hevc,
                    &mut avc,
                    &mut mpeg4,
                    &mut h263,
                    &mut dolby_vision,
                );
            } else {
                update_video_sw(
                    &current_media_type,
                    false,
                    &mut av1,
                    &mut vp9,
                    &mut vp8,
                    &mut hevc,
                    &mut avc,
                    &mut mpeg4,
                    &mut h263,
                    &mut dolby_vision,
                );
            }
        } else if trimmed.contains("hw-accelerated: 1") {
            if in_decoder {
                if !hw_decoders.contains(&current_media_type) && !current_media_type.is_empty() {
                    hw_decoders.push(current_media_type.clone());
                }
                update_video_hw(
                    &current_media_type,
                    true,
                    true,
                    &mut av1,
                    &mut vp9,
                    &mut vp8,
                    &mut hevc,
                    &mut avc,
                    &mut mpeg4,
                    &mut h263,
                    &mut dolby_vision,
                );
            }
            if in_encoder {
                update_video_hw(
                    &current_media_type,
                    false,
                    true,
                    &mut av1,
                    &mut vp9,
                    &mut vp8,
                    &mut hevc,
                    &mut avc,
                    &mut mpeg4,
                    &mut h263,
                    &mut dolby_vision,
                );
            }
        }
    }

    let audio = query_live_audio_specs(audio_codecs);

    MediaCodecInfo {
        audio,
        video_codecs: vec![av1, vp9, vp8, hevc, avc, mpeg4, h263, dolby_vision],
        supported_hardware_decoders: hw_decoders,
    }
}

pub fn scan_media_codecs_from_files() -> MediaCodecInfo {
    let mut audio_codecs = Vec::new();
    let mut hw_decoders = Vec::new();

    let search_paths = [
        "/vendor/etc/media_codecs_ukee.xml",
        "/vendor/etc/media_codecs.xml",
        "/system/etc/media_codecs.xml",
    ];

    for path in search_paths {
        if let Ok(content) = fs::read_to_string(path) {
            for line in content.lines() {
                let trimmed = line.trim();
                if let Some(start) = trimmed.find("<Type name=\"") {
                    let rest = &trimmed[start + 12..];
                    if let Some(end) = rest.find('"') {
                        let mtype = &rest[..end];
                        if mtype.starts_with("audio/") {
                            classify_audio_codec(mtype, &mut audio_codecs);
                        }
                        if !hw_decoders.contains(&mtype.to_string()) {
                            hw_decoders.push(mtype.to_string());
                        }
                    }
                }
            }
        }
    }

    let audio = query_live_audio_specs(audio_codecs);

    MediaCodecInfo {
        audio,
        video_codecs: Vec::new(),
        supported_hardware_decoders: hw_decoders,
    }
}
