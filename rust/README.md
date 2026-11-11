<p align="center">
  <img src="https://img.shields.io/badge/Rust-2024_Edition-000000?style=for-the-badge&logo=rust&logoColor=white" alt="Rust 2024">
  <img src="https://img.shields.io/badge/Architecture-AArch64_%28ARM64--v8a%29-FF5722?style=for-the-badge&logo=arm&logoColor=white" alt="ARM64">
  <img src="https://img.shields.io/badge/Android_API-26+-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android API 26+">
  <img src="https://img.shields.io/badge/IPC-Native_Binder_NDK-007ACC?style=for-the-badge&logo=cplusplus&logoColor=white" alt="Native Binder">
  <img src="https://img.shields.io/badge/Engine-eBPF_Uprobe-7952B3?style=for-the-badge&logo=linux&logoColor=white" alt="eBPF Engine">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPLv3-white?style=for-the-badge&logo=gnu&logoColor=white&labelColor=222" alt="License"></a>
</p>

## About XKM Native

**XKM Native** is the native library powering **Xtra Kernel Manager (XKM)**. Written in Rust (Edition 2024), it runs in-process via JNI (`libxkm_native.so`) to handle system metrics collection, eBPF frame timing, and Android Binder IPC.

It reads `/proc`, `/sys`, and `/dev/binder` directly instead of spawning shell commands (`su -c`), keeping telemetry and actuators within the app process.

---

## Architecture Overview

```mermaid
flowchart TD
    subgraph UI["Kotlin & Jetpack Compose UI (app)"]
        VM["ViewModels & UI Screens"] <-->|"StateFlow / JNI"| NL["NativeLib.kt (JNI Surface)"]
    end

    subgraph CORE["Rust Native Engine (libxkm_native.so)"]
        JNI["JNI Bridge (bridge/)"] <--> ACT["Actuator & Telemetry Hub"]
        
        ACT <--> BINDER["Native Binder Client (rsbinder)"]
        ACT <--> EBPF["eBPF Meter Engine (kala-ebpf)"]
        ACT <--> SYSFS["Sysfs & Procfs Adapters (platform/)"]
    end

    subgraph OS["Android Framework & Linux Kernel"]
        BINDER <-->|"/dev/binder"| SF["SurfaceFlinger & ActivityManager"]
        BINDER <-->|"/dev/binder"| HAL["Qualcomm PowerHAL & Vendor AIDL"]
        EBPF <-->|"Uprobe RingBuffer"| LG["libgui.so (Surface::hook_queueBuffer)"]
        SYSFS <-->|Direct I/O| KRN["/proc & /sys (CPU, GPU, Thermal, Battery)"]
    end
```

---

## Key Subsystems & Capabilities

### 1. eBPF Frame Timing & FPS Meter
Frame timing pipeline based on [kala](https://github.com/pavelc4/kala) and patterns from [Auriya](https://github.com/pavelc4/auriya):
- **Uprobe Attachment**: Attaches to `libgui.so` `Surface::hook_queueBuffer` for the focused application process.
- **Lifecycle Control**: Attaches when target app is focused, detaches on switch or exit to avoid idle overhead.
- **Metrics Calculation**:
  - Rolling window with 1.35x deadline jank classification.
  - 1% Low and 0.1% Low frametime percentiles.
  - Variance and Coefficient of Variation (`cv`).
- **Bottleneck Classification**: Categorizes potential causes based on live telemetry:
  - `Smooth` — Rendering on time.
  - `CpuBound` — CPU load $\ge 90\%$.
  - `GpuBound` — GPU load $\ge 85\%$.
  - `ThermalThrottling` — Thermal zones exceeding severe thresholds.
  - `MemoryPressure` — Available RAM $< 350\text{ MB}$.
  - `AppStall` — UI thread or rendering stall.
- **Sysfs Fallback**: Reads vendor display CRTC nodes (`/sys/class/drm/sde-crtc-*/measured_fps`, etc.) when eBPF is unavailable.

### 2. Android Binder Actuators
Interacts directly with Android system services via `rsbinder`:

| Domain | Interface / Service | Functionality |
|---|---|---|
| **Display** | `SurfaceFlinger` (Codes 45/47)<br>`DisplayManager` (Code 48) | • Active refresh rate query and dynamic mode switching (60Hz, 90Hz, 120Hz).<br>• Direct backlight brightness query and setting. |
| **PowerHAL** | `android.hardware.power.IPower/default` | • Qualcomm vendor AIDL integration.<br>• Toggles `PowerMode::Game`, `SustainedPerformance`, and `ExpensiveRendering`. |
| **Gaming Locks** | `IWifiManager` (Codes 62/64)<br>`IStatusBarService` (Codes 2/4)<br>`IWindowManager` (Codes 59/60)<br>`IVibrator` (Code 2) | • **Wi-Fi Low-Latency Lock** (`WIFI_MODE_FULL_LOW_LATENCY = 4`) with token retention.<br>• **Status Bar / Gesture Lock** (disables notification shade pull-down & back gestures).<br>• **Screen Rotation Lock** (freeze orientation).<br>• **Haptic Silencing** (mute vibrator motor during gameplay). |
| **Touch Boost** | `vendor.lineage.touch.IHighTouchPollingRate`<br>Vendor Sysfs Nodes | • High touch sampling rate toggle (240Hz / 480Hz).<br>• Interacts with LineageOS Touch AIDL and vendor sysfs nodes (`bump_sample_rate`, `game_switch_enable`).<br>• Caches state to overcome write-only OEM kernel drivers. |
| **Task Manager** | `android.app.IActivityManager`<br>`android.app.usage.IUsageStatsManager` | • `/proc` process enumerator sorted by RSS memory with cgroup tags (`top-app`, `restricted`, `background`).<br>• **App Standby Buckets**: Move background apps to `RESTRICTED` or `RARE` while gaming.<br>• **Memory Trim**: Send `TRIM_MEMORY_RUNNING_CRITICAL` without killing apps.<br>• **Process Termination**: Kill background processes or force stop packages. |
| **Battery & Charging** | `vendor.lineage.health.IChargingControl` | • **Bypass Charging / Idle Mode**: Pause battery charging while maintaining device power during gaming.<br>• Fast charge toggle and charging limits. |
| **Thermal Telemetry** | `android.os.IThermalService` | • Global thermal state (`NONE`, `LIGHT`, `MODERATE`, `SEVERE`, `CRITICAL`).<br>• Thermal headroom forecasting before throttling occurs. |

---

## On-Device Diagnostic Runner (`device_runner`)

`device_runner` is an on-device console utility compiled for ARM64 that executes telemetry checks, eBPF probe lifecycles, and micro-benchmarks on physical hardware:

```bash
# 1. Cross-compile for ARM64 
cargo ndk -t arm64-v8a -P 26 build --release --bin device_runner

# 2. Push to device
adb push target/aarch64-linux-android/release/device_runner /data/local/tmp/

# 3. Run with root permissions
adb shell su -c "chmod 755 /data/local/tmp/device_runner && /data/local/tmp/device_runner"
```

### Diagnostic Output Sample (Snapdragon 7+ Gen 2 / `SM7475`)

```text
[1] PLATFORM IDENTIFICATION
  * SoC Vendor     : Qualcomm Snapdragon (SM7475)

[2] CPU TOPOLOGY & FREQUENCIES
  * Online Cores   : [0, 1, 2, 3, 4, 5, 6, 7]
  * Clusters       : 3 (Policy 0: 300-1804MHz | Policy 4: 633-2496MHz | Policy 7: 787-2918MHz)
  * Active Gov     : walt

[8] FPS METER LIFECYCLE
  * Target App     : com.mobile.legends (Unity NDK)
  * eBPF Probe     : Attached to PID 27377 (libgui.so Surface::hook_queueBuffer)
  * Frame Metrics  : 119.5 FPS | FT: 8.37ms | 1% Low: 75.0 FPS | Drop Cause: Smooth
  * Safe Detach    : Detached cleanly, reverted to Sysfs fallback

[13] NATIVE BINDER ACTUATORS 
  * Native Binder Init        : true
  * Active Display Refresh Hz : 120.0 Hz
  * Supported Refresh Rates   : [60, 90, 120]
  * Qualcomm Sustained Perf   : true
  * Qualcomm Expensive Render : true
  * Wi-Fi Low-Latency Lock    : verified

[14] NATIVE BINDER APP MONITOR, TOUCH, THERMAL & CHARGING
  * Total Running Processes   : 255 (Top 1: system_server 506 MB, gms 401 MB, systemui 391 MB)
  * MLBB Standby Bucket       : Ok(Active)
  * IThermalService Status    : Ok(None)
  * High Touch Polling Supp   : true (Set ON: Ok(true) | Restore OFF: Ok(false))
  * Charging Control Supp     : true (Bypass Mode: Ok(true))
```

---

## Development & Testing

### Host Unit Testing (Linux x86_64)
All parsers, data models, state machines, and mock fallbacks run on the host:
```bash
cargo test
```
*Current test suite: 47 passed across 14 test suites.*

### Building the Release Shared Library (`libxkm_native.so`)
To compile the release `.so` for bundling into the Android APK:
```bash
./gradlew buildRustNative
```
Output artifact: `app/src/main/jniLibs/arm64-v8a/libxkm_native.so` (size-optimized with fat LTO, symbol stripping, and `panic = "abort"`).

---

## Credits & References

Special thanks to the following open-source projects:

> *Huge gratitude to these incredible projects — honestly, this native library exists thanks to their groundbreaking work. I'm certainly not an idealist who insists on reinventing the wheel from scratch lol.*

- **[Auriya](https://github.com/pavelc4/auriya)** by [@pavelc4](https://github.com/pavelc4) — Reference architecture for Native Android Binder IPC (`rsbinder`), touchpanel boost game mode, system actuators, and zero-companion app designs.
- **[kala](https://github.com/pavelc4/kala)** by [@pavelc4](https://github.com/pavelc4) — eBPF uprobe engine and frame timing hooks on `libgui.so` (`Surface::hook_queueBuffer`) for FPS metering and jank diagnosis.
- **[rsbinder](https://github.com/jinserk/rsbinder)** — Pure Rust implementation of Android Binder IPC.
- **[Aya](https://github.com/aya-rs/aya)** — Pure Rust eBPF library for Linux.

---

## License

**XKM Native** is open-source software licensed under the **[GNU General Public License v3.0](LICENSE)**.
