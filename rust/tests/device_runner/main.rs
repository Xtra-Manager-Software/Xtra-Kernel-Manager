mod battery;
mod benchmarks;
mod binder;
mod cpu;
mod crash;
mod drm;
mod fps;
mod gpu;
mod media;
mod memory;
mod modules;
mod network;
mod npu;
mod platform;
mod sensors;
mod storage;
mod system;
mod thermal;
pub mod util;

fn main() {
    println!("xkm-native device_runner");

    xkm_native::logging::init();

    platform::run();
    system::run();
    cpu::run();
    gpu::run();
    thermal::run();
    memory::run();
    battery::run();
    sensors::run();
    media::run();
    network::run();
    fps::run();
    modules::run();
    npu::run();
    binder::run();
    storage::run();
    drm::run();
    crash::run();
    benchmarks::run();

    println!("\ndone");
}
