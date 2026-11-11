# Testing XKM Native on Android

This guide explains how to compile the Rust native diagnostics and run them directly on an Android device via ADB.

## Prerequisites

- **Rust Toolchain**: `stable` (edition 2024 recommended)
- **Target Architecture**: `aarch64-linux-android`
- **Android NDK**: API 26+ (pinned `26.1.10909125`)
- **cargo-ndk**: `cargo install cargo-ndk`
- **Android Device**: Rooted, connected via ADB
- **ADB**: Installed and in your PATH

## Build & Run Instructions

To compile the `device_runner` diagnostic binary and execute it on your device, run the following commands:

```bash
# 1. Build the binary for Android AArch64 (API 26)
cargo ndk -t arm64-v8a -P 26 build --release --bin device_runner

# 2. Push the binary to the device's temporary directory
adb push target/aarch64-linux-android/release/device_runner /data/local/tmp/

# 3. Execute the binary as root (su)
adb shell su -c "chmod 755 /data/local/tmp/device_runner && /data/local/tmp/device_runner"
```

> [!NOTE]
> Make sure your device screen is on and you grant Root access if prompted on the device screen.

## Troubleshooting

- **Target not found**: If `cargo` complains about the target, install it via `rustup target add aarch64-linux-android`.
- **Permission denied**: Ensure you are running `adb shell su -c ...` to execute as root, and that the file has execution permissions (`chmod 755 /data/local/tmp/device_runner`).

---

## Credits & References

Special thanks to:
- [Auriya](https://github.com/pavelc4/auriya) by [@pavelc4](https://github.com/pavelc4) – Reference architecture for Native Android Binder IPC (`rsbinder`), touchpanel boost game mode, system actuators, and zero-companion app designs.
- [kala](https://github.com/pavelc4/kala) by [@pavelc4](https://github.com/pavelc4) – eBPF uprobe engine and frame timing hooks on `libgui.so` (`Surface::hook_queueBuffer`) for zero-overhead FPS metering and jank diagnosis.
