mod battery;
mod benchmarks;
mod binder;
mod cpu;
mod crash;
mod fps;
mod gpu;
mod memory;
mod modules;
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
    binder::run();
    storage::run();
    crash::run();
    benchmarks::run();

    println!("\ndone");
}
