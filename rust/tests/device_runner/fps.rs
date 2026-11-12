use crate::util;
use std::time::Instant;
use xkm_native::platform::{binder, fps};

pub fn run() {
    println!("\n[fps]");
    let t0 = Instant::now();
    let fg_app = binder::get_foreground_app();
    let sysfs_node = fps::SysfsFpsReader::detect_sysfs();
    let init_attached = fps::is_ebpf_attached();
    let init_fps = fps::read_fps();
    let probe_time = t0.elapsed();

    println!("  probe_time: {}", util::fmt_duration(probe_time));
    println!("  foreground_target: {:?}", fg_app);
    println!("  sysfs_node: {:?}", sysfs_node);
    println!("  passive_attached: {}", init_attached);
    println!("  baseline_fps: {:?}", init_fps);

    // Target app monitoring
    if let Some(ref fg) = fg_app {
        fps::set_target_package(Some(fg.package_name.clone()));
        let t_attach = Instant::now();
        let _ = fps::read_fps();
        let attach_time = t_attach.elapsed();
        println!(
            "  target_attached: {} (pid={:?}) [took {}]",
            fps::is_ebpf_attached(),
            fps::get_attached_pid(),
            util::fmt_duration(attach_time)
        );
        for i in 1..=5 {
            std::thread::sleep(std::time::Duration::from_millis(150));
            let t_sample = Instant::now();
            if let Some(r) = fps::read_fps() {
                let sample_dur = t_sample.elapsed();
                println!(
                    "    sample {}: fps={:.1}, frametime={:.2}ms, avg={:.1}, 1%low={:.1}, jank={}({:.1}%), cause={}({:?}) [sample_time: {}]",
                    i,
                    r.fps,
                    r.frametime_ms,
                    r.avg_fps,
                    r.low_1pct_fps,
                    r.jank_count,
                    r.jank_percent,
                    r.drop_cause.as_str(),
                    r.source,
                    util::fmt_duration(sample_dur)
                );
            }
        }
    }

    // Detach
    let t_detach = Instant::now();
    let detached_ok = fps::detach_fps_pid();
    std::thread::sleep(std::time::Duration::from_millis(50));
    println!(
        "  detach_result: {} [took {}]",
        detached_ok,
        util::fmt_duration(t_detach.elapsed())
    );
    println!("  attached_after_detach: {}", fps::is_ebpf_attached());

    // Isolation check
    fps::set_target_package(Some("com.fake.nonexistent.app".to_string()));
    let _ = fps::read_fps();
    println!("  isolation_check_attached: {}", fps::is_ebpf_attached());
    fps::clear_fps();
}
