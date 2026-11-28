#!/usr/bin/env bash
set -Eeuo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly RUST_TARGET="aarch64-linux-android"
readonly ANDROID_ABI="arm64-v8a"
readonly ANDROID_API="31"
readonly DEFAULT_NDK_VERSION="26.1.10909125"

die() {
    printf 'Error: %s\n' "$*" >&2
    exit 1
}

info() {
    printf '\n==> %s\n' "$*"
}

read_local_property() {
    local key="$1"
    local properties_file="$SCRIPT_DIR/local.properties"

    [[ -f "$properties_file" ]] || return 0

    awk -F= -v key="$key" '
        /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
        $1 == key {
            sub(/^[^=]*=/, "")
            sub(/^[[:space:]]+/, "")
            sub(/[[:space:]]+$/, "")
            print
            exit
        }
    ' "$properties_file"
}

[[ "$(uname -s)" == "Linux" ]] || die "This script supports Linux only."

command -v java >/dev/null 2>&1 || die "Java is missing. Install JDK 21 and set JAVA_HOME."
command -v cargo >/dev/null 2>&1 || die "Cargo is missing. Install Rust with rustup first."
command -v rustup >/dev/null 2>&1 || die "rustup is missing. Install Rust with rustup first."

export CARGO_HOME="${CARGO_HOME:-$HOME/.cargo}"
export PATH="$CARGO_HOME/bin:$PATH"

LOCAL_SDK_DIR="$(read_local_property sdk.dir)"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-${LOCAL_SDK_DIR:-$HOME/Android/Sdk}}}"
if [[ "$SDK_ROOT" == "~/"* ]]; then
    SDK_ROOT="$HOME/${SDK_ROOT#~/}"
fi
if [[ "$SDK_ROOT" != /* ]]; then
    SDK_ROOT="$SCRIPT_DIR/$SDK_ROOT"
fi
[[ -d "$SDK_ROOT" ]] || die "Android SDK not found at '$SDK_ROOT'. Set ANDROID_SDK_ROOT or sdk.dir in local.properties."

LOCAL_NDK_DIR="$(read_local_property ndk.dir)"
NDK_ROOT="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-${LOCAL_NDK_DIR:-}}}"
if [[ -z "$NDK_ROOT" ]]; then
    mapfile -t INSTALLED_NDKS < <(find "$SDK_ROOT/ndk" -mindepth 1 -maxdepth 1 -type d -print 2>/dev/null | sort -V)
    if (( ${#INSTALLED_NDKS[@]} > 0 )); then
        NDK_ROOT="${INSTALLED_NDKS[${#INSTALLED_NDKS[@]}-1]}"
    fi
fi
[[ -n "$NDK_ROOT" && -d "$NDK_ROOT" ]] || die "Android NDK not found. Install it with sdkmanager --sdk_root='$SDK_ROOT' --install 'ndk;$DEFAULT_NDK_VERSION' or set ANDROID_NDK_HOME."

case "$(uname -m)" in
    x86_64) NDK_HOST="linux-x86_64" ;;
    aarch64|arm64) NDK_HOST="linux-aarch64" ;;
    *) die "Unsupported Linux architecture for Android NDK: $(uname -m)" ;;
esac
[[ -x "$NDK_ROOT/toolchains/llvm/prebuilt/$NDK_HOST/bin/clang" ]] || die "NDK at '$NDK_ROOT' is missing its $NDK_HOST toolchain."

export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_HOME="$SDK_ROOT"
export ANDROID_NDK_HOME="$NDK_ROOT"
export ANDROID_NDK_ROOT="$NDK_ROOT"

if ! rustup target list --installed | grep -Fxq "$RUST_TARGET"; then
    info "Adding Rust target $RUST_TARGET"
    rustup target add "$RUST_TARGET"
fi

if ! command -v cargo-ndk >/dev/null 2>&1; then
    info "Installing cargo-ndk"
    cargo install cargo-ndk
fi
command -v cargo-ndk >/dev/null 2>&1 || die "cargo-ndk installation did not provide the cargo-ndk executable on PATH."

info "Building Rust native library for $ANDROID_ABI (Android API $ANDROID_API)"
(
    cd "$SCRIPT_DIR/rust"
    cargo ndk -t "$ANDROID_ABI" -P "$ANDROID_API" -o "$SCRIPT_DIR/app/src/main/jniLibs" build --release --locked
)

NATIVE_LIBRARY="$SCRIPT_DIR/app/src/main/jniLibs/$ANDROID_ABI/libxkm_native.so"
[[ -s "$NATIVE_LIBRARY" ]] || die "Cargo build completed but '$NATIVE_LIBRARY' was not produced."

info "Building Android debug APK"
"$SCRIPT_DIR/gradlew" :app:assembleDebug --no-daemon

APK_PATH="$(find "$SCRIPT_DIR/app/build/outputs/apk/debug" -maxdepth 1 -type f -name '*.apk' -print -quit 2>/dev/null || true)"
[[ -n "$APK_PATH" ]] || die "Gradle completed but no debug APK was found."

printf '\nBuild complete.\nRust library: %s\nDebug APK: %s\n' "$NATIVE_LIBRARY" "$APK_PATH"
./gradlew installDebug
