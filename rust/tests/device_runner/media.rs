use crate::util;
use std::time::Instant;
use xkm_native::platform::media;

pub fn run() {
    println!("\n[media]");
    let t0 = Instant::now();
    let media_info = media::scan_media_codecs_subsystem();
    let fetch_time = t0.elapsed();

    println!("  fetch_time: {}", util::fmt_duration(fetch_time));
    println!(
        "  audio_output: {} @ {} (buffer: {}, bit_depth: {})",
        media_info.audio.output_routes,
        media_info.audio.sample_rate,
        media_info.audio.buffer_size,
        media_info.audio.bit_depth
    );
    println!(
        "  low_latency_audio: {}",
        media_info.audio.low_latency_audio
    );
    println!("  pro_audio: {}", media_info.audio.pro_audio_support);
    println!("  midi: {}", media_info.audio.midi_support);
    println!(
        "  audio_codecs: {}",
        media_info.audio.audio_codecs.join(", ")
    );
    println!("  video_codecs:");
    for vc in &media_info.video_codecs {
        println!(
            "    {}: Decode {}, Encode {}",
            vc.name, vc.decode, vc.encode
        );
    }
    println!(
        "  hw_decoders: {} supported",
        media_info.supported_hardware_decoders.len()
    );
}
