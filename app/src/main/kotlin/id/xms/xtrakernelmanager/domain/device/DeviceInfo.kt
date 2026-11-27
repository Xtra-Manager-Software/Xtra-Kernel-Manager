package id.xms.xtrakernelmanager.domain.device

import androidx.annotation.DrawableRes
import id.xms.xtrakernelmanager.R

/**
 * Pure mock specifications and deterministic data providers for UI prototyping.
 * Does not read /proc, /sys, or any hardware nodes.
 */

data class DeviceSpec(
    val manufacturer: String = "XIAOMI",
    val model: String = "23049PCD8G",
    val codename: String = "MARBLE",
    val board: String = "TARO",
    val commercialModel: String = "POCO F5",
    val kernelVersion: String = "5.10.269-Chandelier-prjkt+",
)

data class ProcessorSpec(
    val name: String = "SM7475",
    val platform: String = "Qualcomm",
    @get:DrawableRes val logoRes: Int = R.drawable.ic_snapdragon,
)

data class CpuClusterSpec(
    val clusterName: String,
    val coreCount: Int,
    val architecture: String,
    val maxFrequency: String,
    val minFrequency: String,
    val l2Cache: String,
)

data class ProcessorDetailSpec(
    val commercialName: String = "Snapdragon® 7+ Gen 2",
    val partNumber: String = "SM7475-AB",
    val vendor: String = "Qualcomm Technologies, Inc.",
    val fabrication: String = "4 nm TSMC (N4)",
    val instructionSet: String = "ARMv9-A (64-bit)",
    val cpuTopology: String = "Octa-Core (1 + 3 + 4)",
    val clusters: List<CpuClusterSpec> = listOf(
        CpuClusterSpec("Prime Core (Gold+)", 1, "Cortex-X2", "2.91 GHz", "844 MHz", "1024 KB L2"),
        CpuClusterSpec("Performance (Gold)", 3, "Cortex-A710", "2.49 GHz", "672 MHz", "512 KB L2 / core"),
        CpuClusterSpec("Efficiency (Silver)", 4, "Cortex-A510", "1.80 GHz", "300 MHz", "256 KB L2 / core"),
    ),
    val l1CacheText: String = "64 KB I$ + 64 KB D$ per core",
    val l2CacheTotal: String = "3.5 MB Dedicated L2",
    val l3SharedCache: String = "6 MB Unified System L3",
    val systemLevelCache: String = "4 MB SLC Memory Buffer",
    val activeGovernor: String = "walt",
    val availableGovernors: List<String> = listOf("walt", "schedutil", "performance", "powersave"),
    val energyModel: String = "Energy-Aware Scheduling (walt EAS)",
    val frequencyRange: String = "300 MHz - 2918 MHz",
    val idleStates: List<String> = listOf("C0 Active", "C1 Retention", "C2 Power Collapse"),
    val supportedAbis: String = "arm64-v8a (64-bit)",
    val vectorExtensions: List<String> = listOf("SVE2", "NEON", "FP16", "DotProd"),
    val cryptoExtensions: List<String> = listOf("AES-256", "SHA-512", "CRC32", "PMULL", "PAC / BTI"),
    @get:DrawableRes val logoRes: Int = R.drawable.ic_snapdragon,
)

data class DisplaySpec(
    val refreshRateText: String = "120 Hz",
    val gpuPillText: String = "ADRENO",
    val ppiText: String = "395 ppi",
    val physicalSizeText: String = "6,66 / 169 mm",
)

data class DisplayModeSpec(
    val resolutionText: String,
    val refreshRateText: String,
    val isActive: Boolean = false,
)

data class DisplayDetailSpec(
    // Hero panel
    val resolutionText: String = "1080 x 2400",
    val resolutionClass: String = "FHD+",
    val panelTechnology: String = "AMOLED",
    val aspectRatioText: String = "20:9",
    val activeRefreshRateText: String = "120 Hz",
    val screenDiagonalText: String = "6,67\"",
    val physicalSizeText: String = "6,66 / 169 mm",
    // Density
    val ppiText: String = "395 ppi",
    val densityDpiText: String = "440 dpi",
    // Accelerators
    val gpuModelText: String = "Adreno 725",
    val npuModelText: String = "Hexagon NPU",
    // Color & HDR
    val hdrTypes: List<String> = listOf("HDR10", "HDR10+", "Dolby Vision"),
    val wideColorGamutText: String = "DCI-P3",
    val colorDepthText: String = "10-bit",
    // Panel & link
    val connectorText: String = "card0-DSI-1",
    val connectionStatus: String = "Connected",
    val gpuPillText: String = "ADRENO",
    val modes: List<DisplayModeSpec> = listOf(
        DisplayModeSpec("1080 x 2400", "120 Hz", isActive = true),
        DisplayModeSpec("1080 x 2400", "90 Hz"),
        DisplayModeSpec("1080 x 2400", "60 Hz"),
        DisplayModeSpec("1080 x 2400", "30 Hz"),
    ),
)

data class AndroidVersionSpec(
    val version: String = "Android 17",
    val codename: String = "Cinnamon Bun",
    @get:DrawableRes val logoRes: Int = R.drawable.ic_android17,
    val apiLevel: String = "API 37",
)

data class BatterySpec(
    val level: Int = 89,
    val status: String = "Charging",
    val isCharging: Boolean = true,
    val temperature: String = "35°C",
    val health: String = "Good",
    val capacity: String = "5000 mAh",
)

object DeviceInfoResolver {
    fun resolveDeviceSpec(): DeviceSpec = DeviceSpec()
    fun resolveKernelVersion(): String = "5.10.269-Chandelier-prjkt+"
    fun resolveProcessorSpec(): ProcessorSpec = ProcessorSpec()
    fun resolveProcessorDetailSpec(): ProcessorDetailSpec = ProcessorDetailSpec()
    fun resolveDisplaySpec(): DisplaySpec = DisplaySpec()
    fun resolveDisplayDetailSpec(): DisplayDetailSpec = DisplayDetailSpec()
    fun resolveAndroidVersionSpec(): AndroidVersionSpec = AndroidVersionSpec()
    fun resolveBatterySpec(): BatterySpec = BatterySpec()
}
