use std::process::Command;

use super::types::AudioSpecs;

pub fn query_live_audio_specs(audio_codecs: Vec<String>) -> AudioSpecs {
    let mut low_latency_audio = false;
    let mut pro_audio_support = false;
    let mut midi_support = false;
    let mut unprocessed_audio_source = false;

    if let Ok(out) = Command::new("pm").args(["list", "features"]).output() {
        let text = String::from_utf8_lossy(&out.stdout);
        for line in text.lines() {
            let t = line.trim();
            match t {
                "feature:android.hardware.audio.low_latency" => low_latency_audio = true,
                "feature:android.hardware.audio.pro" => pro_audio_support = true,
                "feature:android.software.midi" => midi_support = true,
                "feature:android.hardware.microphone.unprocessed" => {
                    unprocessed_audio_source = true
                }
                _ => {}
            }
        }
    }

    let mut sample_rate = String::new();
    let mut buffer_size = String::new();
    let mut bit_depth = String::new();
    let mut output_routes = String::new();

    if let Ok(out) = Command::new("dumpsys").arg("media.audio_flinger").output() {
        let dump = String::from_utf8_lossy(&out.stdout);
        for line in dump.lines() {
            let t = line.trim();
            match () {
                _ if t.starts_with("Sample rate:") && sample_rate.is_empty() => {
                    let rate_raw = t.trim_start_matches("Sample rate:").trim();
                    if let Ok(num) = rate_raw.trim_end_matches("Hz").trim().parse::<u32>() {
                        sample_rate = format!("{} kHz", num / 1000);
                    } else {
                        sample_rate = rate_raw.to_string();
                    }
                }
                _ if t.starts_with("HAL frame count:") && buffer_size.is_empty() => {
                    let frames = t.trim_start_matches("HAL frame count:").trim();
                    buffer_size = format!("{} Frames", frames);
                }
                _ if t.starts_with("HAL format:") && bit_depth.is_empty() => {
                    bit_depth = match () {
                        _ if t.contains("24_BIT") => "24-bit".to_string(),
                        _ if t.contains("32_BIT") => "32-bit".to_string(),
                        _ if t.contains("16_BIT") => "16-bit".to_string(),
                        _ if t.contains("FLOAT") => "32-bit float".to_string(),
                        _ => String::new(),
                    };
                }
                _ if (t.starts_with("Output devices:") || t.starts_with("Output device:"))
                    && output_routes.is_empty() =>
                {
                    output_routes = match () {
                        _ if t.contains("SPEAKER") => "Speaker".to_string(),
                        _ if t.contains("WIRED_HEADSET") || t.contains("HEADPHONES") => {
                            "Headphones".to_string()
                        }
                        _ if t.contains("BLUETOOTH") || t.contains("BLE") => {
                            "Bluetooth".to_string()
                        }
                        _ if t.contains("USB") => "USB Audio".to_string(),
                        _ => {
                            let dev_raw = t
                                .trim_start_matches("Output devices:")
                                .trim_start_matches("Output device:")
                                .trim();
                            dev_raw.to_string()
                        }
                    };
                }
                _ => {}
            }
        }
    }

    AudioSpecs {
        low_latency_audio,
        pro_audio_support,
        midi_support,
        unprocessed_audio_source,
        sample_rate,
        buffer_size,
        bit_depth,
        output_routes,
        audio_codecs,
    }
}

pub fn classify_audio_codec(mtype: &str, codecs: &mut Vec<String>) {
    let name = match mtype {
        "audio/ac3" => "AC3",
        "audio/eac3" => "E-AC3",
        "audio/mp4a-latm" => "AAC",
        "audio/flac" => "FLAC",
        "audio/g711-alaw" => "PCM A-law",
        "audio/g711-mlaw" => "PCM µ-law",
        "audio/iamf" => "IAMF",
        "audio/mpeg" => "MP3",
        "audio/opus" => "Opus",
        "audio/raw" => "Raw PCM",
        "audio/vorbis" => "Vorbis",
        "audio/x-ape" => "APE",
        "audio/evrc" => "EVRC",
        "audio/qcelp" => "QCELP",
        "audio/amr-wb" => "AMR-WB",
        _ => return,
    };

    if !codecs.iter().any(|c| c == name) {
        codecs.push(name.to_string());
    }
}
