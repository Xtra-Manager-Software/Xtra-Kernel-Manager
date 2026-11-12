mod battery;
mod benchmarks;
mod binder;
mod cpu;
mod crash;
mod drm;
mod fps;
mod gpu;
mod memory;
mod modules;
mod npu;
mod platform;
mod storage;
mod thermal;
pub mod util;

fn main() {
    println!("xkm-native device_runner");

    xkm_native::logging::init();

    platform::run();
    cpu::run();
    gpu::run();
    thermal::run();
    memory::run();
    battery::run();
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
