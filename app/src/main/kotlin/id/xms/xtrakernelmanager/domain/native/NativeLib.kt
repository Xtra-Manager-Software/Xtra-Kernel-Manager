package id.xms.xtrakernelmanager.domain.native

import android.util.Log

/**
 * JNI boundary for XKM Native Rust Engine (`libxkm_native.so`).
 * Provides direct access to Linux kernel subsystems, hardware telemetry,
 * DRM/TEE security, Vulkan GPU capabilities, NPU, sensors, and eBPF.
 */
object NativeLib {
    private const val TAG = "NativeLib"

    val isLoaded: Boolean = try {
        System.loadLibrary("xkm_native")
        runCatching { Log.i(TAG, "Loaded libxkm_native.so successfully") }
        true
    } catch (t: Throwable) {
        runCatching { Log.e(TAG, "Failed to load libxkm_native.so: ${t.message}", t) }
        false
    }

    // --- Subsystem Snapshots (JSON payloads) ---

    @JvmStatic
    external fun getTelemetrySnapshotNative(): String

    @JvmStatic
    external fun getSystemInfoNative(): String

    @JvmStatic
    external fun getGpuCapabilitiesNative(): String

    @JvmStatic
    external fun getDrmSecurityInfoNative(): String

    @JvmStatic
    external fun getSensorInfoNative(): String

    @JvmStatic
    external fun getNpuInfoNative(): String

    @JvmStatic
    external fun getMediaCodecInfoNative(): String

    @JvmStatic
    external fun getNetworkInfoNative(): String

    @JvmStatic
    external fun listLoadedModulesNative(): String

    // --- FPS & Frame Timing ---

    @JvmStatic
    external fun readFpsNative(): Float

    @JvmStatic
    external fun readFpsInfoNative(): String

    @JvmStatic
    external fun isFpsEbpfAttachedNative(): Boolean

    @JvmStatic
    external fun getFpsDropCauseNative(): String

    // --- Native Logging ---

    @JvmStatic
    external fun getNativeLogsNative(limit: Int): String

    // --- Safe Fallback Wrappers (never crash even if .so is missing or JNI throws) ---

    fun safeGetTelemetry(): String = runCatching {
        if (isLoaded) getTelemetrySnapshotNative() else "{}"
    }.getOrDefault("{}")

    fun safeGetSystemInfo(): String = runCatching {
        if (isLoaded) getSystemInfoNative() else "{}"
    }.getOrDefault("{}")

    fun safeGetGpuCapabilities(): String = runCatching {
        if (isLoaded) getGpuCapabilitiesNative() else "{}"
    }.getOrDefault("{}")

    fun safeGetDrmSecurity(): String = runCatching {
        if (isLoaded) getDrmSecurityInfoNative() else "{}"
    }.getOrDefault("{}")

    fun safeGetSensorInfo(): String = runCatching {
        if (isLoaded) getSensorInfoNative() else "{}"
    }.getOrDefault("{}")

    fun safeGetNpuInfo(): String = runCatching {
        if (isLoaded) getNpuInfoNative() else "{}"
    }.getOrDefault("{}")

    fun safeGetMediaCodecInfo(): String = runCatching {
        if (isLoaded) getMediaCodecInfoNative() else "{}"
    }.getOrDefault("{}")

    fun safeGetNetworkInfo(): String = runCatching {
        if (isLoaded) getNetworkInfoNative() else "{}"
    }.getOrDefault("{}")

    fun safeListLoadedModules(): String = runCatching {
        if (isLoaded) listLoadedModulesNative() else "[]"
    }.getOrDefault("[]")

    fun safeReadFps(): Float = runCatching {
        if (isLoaded) readFpsNative() else -1.0f
    }.getOrDefault(-1.0f)

    fun safeReadFpsInfo(): String = runCatching {
        if (isLoaded) readFpsInfoNative() else "{}"
    }.getOrDefault("{}")

    fun safeIsFpsEbpfAttached(): Boolean = runCatching {
        if (isLoaded) isFpsEbpfAttachedNative() else false
    }.getOrDefault(false)

    fun safeGetFpsDropCause(): String = runCatching {
        if (isLoaded) getFpsDropCauseNative() else "eBPF probe not attached"
    }.getOrDefault("Unavailable")
}
