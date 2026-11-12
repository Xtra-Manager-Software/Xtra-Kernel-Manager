use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct AudioSpecs {
    pub low_latency_audio: bool,
    pub pro_audio_support: bool,
    pub midi_support: bool,
    pub unprocessed_audio_source: bool,
    pub sample_rate: String,
    pub buffer_size: String,
    pub bit_depth: String,
    pub output_routes: String,
    pub audio_codecs: Vec<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct VideoCodecCapability {
    pub name: String,
    pub decode: String,
    pub encode: String,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct MediaCodecInfo {
    pub audio: AudioSpecs,
    pub video_codecs: Vec<VideoCodecCapability>,
    pub supported_hardware_decoders: Vec<String>,
}
