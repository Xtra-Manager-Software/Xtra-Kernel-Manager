<h1 align="center">
    Xtra Kernel Manager
</h1>

<p align="center">
    <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&colorA=363A4F&logo=kotlin&logoColor=D9E0EE">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-7F52FF?style=for-the-badge&colorA=363A4F&logo=jetpack-compose&logoColor=D9E0EE">
    <img src="https://img.shields.io/badge/Rust-000000?style=for-the-badge&colorA=363A4F&logo=rust&logoColor=D9E0EE">
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&colorA=363A4F&logo=android&logoColor=D9E0EE">
</p>

<p align="center">
    <a href="https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/releases">
        <img src="https://img.shields.io/github/v/release/Xtra-Manager-Software/Xtra-Kernel-Manager?style=for-the-badge&logo=github&colorA=363A4F&colorB=F2CDCD&logoColor=D9E0EE">
    </a>
</p>

---

## About

**Xtra Kernel Manager** is a modern, rooted Android application built with **Kotlin** and **Jetpack Compose**, designed for kernel enthusiasts and power users.

It provides **real-time visibility and precise control** over CPU performance, thermal behavior, and power efficiency on custom kernels, while prioritizing **clarity, responsiveness, and minimal system overhead**.
---

## Supported Root Managers

- [Magisk](https://github.com/topjohnwu/Magisk)
- [KernelSU](https://github.com/tiann/KernelSU)
- [APatch](https://github.com/bmax121/APatch)

---

## Development Setup

### Prerequisites
- Android Studio (latest version)
- Android SDK (API 31+)
- Rust toolchain
- NDK 26.1.10909125

### Local Development
1. Clone the repository:
   ```bash
   git clone https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager.git
   cd Xtra-Kernel-Manager
   ```

2. Setup configuration files:
   ```bash
   cp local.properties.template local.properties
   cp gradle.properties.template gradle.properties
   ```

3. Edit `local.properties` with your Android SDK path:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   ```

4. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

### Play Protect Compatibility
This project includes optimizations to reduce Play Protect detection:
- Aggressive code obfuscation with ProGuard/R8
- Legitimate app metadata and permissions
- Consistent signing with reproducible builds
- Open source transparency

### Security Notice
- `local.properties` and `gradle.properties` are gitignored for security
- Keystore files are never committed to the repository
- All builds are reproducible and verifiable

---

## Requirements

- **Root Access** – Mandatory for all core features
- **Android Version** – Android 8 Oreo (API 26) or above
- **Have a brain** – without understanding what you’re doing is a bad idea

---

## Documentation & Resources
- [How to Grant Root Access](docs/HOW_TO_ROOT.md)
- [Changelogs](docs/CHANGELOGS.md)
- [Privacy Policy](docs/PRIVACY.md)
- [Terms of Service](docs/TERMS.md)
- [Releases](https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/releases)
- [Issues](https://github.com/Xtra-Manager-Software/Xtra-Kernel-Manager/issues)

## Credits
- [YukiHookAPI](https://github.com/HighCapable/YukiHookAPI)
- [KavaRef](https://github.com/Kava-Ref)
- [Auriya](https://github.com/pavelc4/auriya) – Reference API and Native Binder architecture
- [kala](https://github.com/pavelc4/kala) –  engine for FPS metering 


## License

Xtra Kernel Manager is open-source software licensed under the **[GNU General Public License v3.0](LICENSE)**.
