use super::types::VideoCodecCapability;

#[allow(clippy::too_many_arguments)]
pub fn update_video_hw(
    mtype: &str,
    is_decoder: bool,
    is_hw: bool,
    av1: &mut VideoCodecCapability,
    vp9: &mut VideoCodecCapability,
    vp8: &mut VideoCodecCapability,
    hevc: &mut VideoCodecCapability,
    avc: &mut VideoCodecCapability,
    _mpeg4: &mut VideoCodecCapability,
    _h263: &mut VideoCodecCapability,
    dolby_vision: &mut VideoCodecCapability,
) {
    let status = match is_hw {
        true => "HW".to_string(),
        false => "SW".to_string(),
    };

    match mtype {
        "video/av01" => match is_decoder {
            true => av1.decode = status,
            false => av1.encode = status,
        },
        "video/x-vnd.on2.vp9" => match is_decoder {
            true => vp9.decode = status,
            false => vp9.encode = status,
        },
        "video/x-vnd.on2.vp8" => match is_decoder {
            true => vp8.decode = status,
            false => vp8.encode = status,
        },
        "video/hevc" => match is_decoder {
            true => hevc.decode = status,
            false => hevc.encode = status,
        },
        "video/avc" => match is_decoder {
            true => avc.decode = status,
            false => avc.encode = status,
        },
        "video/dolby-vision" => match is_decoder {
            true => dolby_vision.decode = status,
            false => dolby_vision.encode = status,
        },
        _ => {}
    }
}

#[allow(clippy::too_many_arguments)]
pub fn update_video_sw(
    mtype: &str,
    is_decoder: bool,
    av1: &mut VideoCodecCapability,
    vp9: &mut VideoCodecCapability,
    vp8: &mut VideoCodecCapability,
    hevc: &mut VideoCodecCapability,
    avc: &mut VideoCodecCapability,
    mpeg4: &mut VideoCodecCapability,
    h263: &mut VideoCodecCapability,
    dolby_vision: &mut VideoCodecCapability,
) {
    update_video_hw(
        mtype,
        is_decoder,
        false,
        av1,
        vp9,
        vp8,
        hevc,
        avc,
        mpeg4,
        h263,
        dolby_vision,
    );
}
