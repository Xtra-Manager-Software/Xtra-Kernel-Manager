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

data class DisplaySpec(
    val refreshRateText: String = "120 Hz",
    val gpuPillText: String = "ADRENO",
    val ppiText: String = "395 ppi",
    val physicalSizeText: String = "6,66 / 169 mm",
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
    fun resolveDisplaySpec(): DisplaySpec = DisplaySpec()
    fun resolveAndroidVersionSpec(): AndroidVersionSpec = AndroidVersionSpec()
    fun resolveBatterySpec(): BatterySpec = BatterySpec()
}
