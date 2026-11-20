package id.xms.xtrakernelmanager.domain.native

/**
 * Domain models holding parsed and raw JNI telemetry and subsystem information.
 */

data class MetricItem(
    val label: String,
    val value: String,
    val badge: String? = null,
    val isMonospace: Boolean = false,
)

data class TelemetryDomainData(
    val rawJson: String = "{}",
    val timestampMs: Long = 0L,
    val cpuModel: String = "",
    val cpuLoadPct: Float = 0f,
    val coreCount: Int = 0,
    val clusterCount: Int = 0,
    val gpuModel: String = "",
    val gpuVendor: String = "",
    val gpuFreqMhz: Int = 0,
    val gpuLoadPct: Int = 0,
    val memUsedMb: Long = 0L,
    val memTotalMb: Long = 0L,
    val memAvailableMb: Long = 0L,
    val zramUsedMb: Long = 0L,
    val zramTotalMb: Long = 0L,
    val swappiness: Int = 0,
    val batteryLevel: Int = 0,
    val batteryTempC: Float = 0f,
    val batteryVoltageMv: Int = 0,
    val batteryCurrentMa: Int = 0,
    val isCharging: Boolean = false,
    val cpuTempC: Float = 0f,
    val gpuTempC: Float = 0f,
    val thermalZonesCount: Int = 0,
    val foregroundApp: String? = null,
    val metrics: List<MetricItem> = emptyList(),
)

data class SystemDomainData(
    val rawJson: String = "{}",
    val brand: String = "",
    val hardware: String = "",
    val socManufacturer: String = "",
    val socModel: String = "",
    val osName: String = "",
    val osCodename: String = "",
    val apiLevel: Long = 0L,
    val securityPatch: String = "",
    val buildId: String = "",
    val kernelVersion: String = "",
    val activeSlot: String = "",
    val verifiedBootState: String = "",
    val bootloaderStatus: String = "",
    val projectTreble: Boolean = false,
    val projectMainline: Boolean = false,
    val dynamicPartitions: Boolean = false,
    val seamlessUpdates: Boolean = false,
    val metrics: List<MetricItem> = emptyList(),
)

data class GpuDomainData(
    val rawJson: String = "{}",
    val vendor: String = "",
    val model: String = "",
    val driverVersion: String = "",
    val driverInfo: String = "",
    val vulkanApiVersion: String = "",
    val rayTracing: Boolean = false,
    val meshShaders: Boolean = false,
    val variableRateShading: Boolean = false,
    val deviceMemoryLocal: String = "",
    val deviceExtensionsCount: Int = 0,
    val metrics: List<MetricItem> = emptyList(),
)

data class DrmDomainData(
    val rawJson: String = "{}",
    val widevineAvailable: Boolean = false,
    val widevineSecurityLevel: String = "",
    val widevineService: String = "",
    val clearkeyAvailable: Boolean = false,
    val clearkeyService: String = "",
    val teeInterface: String = "",
    val cryptoEngine: String = "",
    val keymintService: String = "",
    val displayConnector: String = "",
    val displayStatus: String = "",
    val supportedRefreshRates: List<Int> = emptyList(),
    val hdrTypes: List<String> = emptyList(),
    val wideColorGamut: Boolean = false,
    val metrics: List<MetricItem> = emptyList(),
)

data class SensorsDomainData(
    val rawJson: String = "{}",
    val hardwareSensorsCount: Int = 0,
    val activeSensorsSummary: String = "",
    val inputDevicesCount: Int = 0,
    val cameraSensorsCount: Int = 0,
    val sensorNames: List<String> = emptyList(),
    val metrics: List<MetricItem> = emptyList(),
)

data class NpuDomainData(
    val rawJson: String = "{}",
    val isAvailable: Boolean = false,
    val vendorFamily: String = "",
    val hardwareState: String = "",
    val coprocessors: List<String> = emptyList(),
    val devNodes: List<String> = emptyList(),
    val activeDaemons: List<String> = emptyList(),
    val aiRuntimes: List<String> = emptyList(),
    val metrics: List<MetricItem> = emptyList(),
)

data class MediaDomainData(
    val rawJson: String = "{}",
    val videoDecodersCount: Int = 0,
    val videoEncodersCount: Int = 0,
    val topDecoders: List<String> = emptyList(),
    val topEncoders: List<String> = emptyList(),
    val audioCodecs: List<String> = emptyList(),
    val metrics: List<MetricItem> = emptyList(),
)

data class NetworkDomainData(
    val rawJson: String = "{}",
    val wifiSsid: String = "",
    val wifiLinkSpeed: String = "",
    val wifiFreq: String = "",
    val wifiState: String = "",
    val simState: String = "",
    val simOperator: String = "",
    val telephonyNetworkType: String = "",
    val metrics: List<MetricItem> = emptyList(),
)

data class LoadedModuleItem(
    val name: String,
    val sizeBytes: Long,
    val instances: Int = 0,
    val state: String = "Live",
    val isProtected: Boolean = false,
    val usedBy: List<String> = emptyList(),
)

data class ModulesDomainData(
    val rawJson: String = "{}",
    val totalCount: Int = 0,
    val totalMemoryBytes: Long = 0L,
    val topModules: List<LoadedModuleItem> = emptyList(),
    val metrics: List<MetricItem> = emptyList(),
) {
    val modules: List<LoadedModuleItem> get() = topModules
}

data class FpsDomainData(
    val rawJson: String = "{}",
    val currentFps: Float = -1f,
    val isEbpfAttached: Boolean = false,
    val dropCause: String = "",
    val metrics: List<MetricItem> = emptyList(),
)

data class AllNativeDomainsSnapshot(
    val isNativeLoaded: Boolean = false,
    val telemetry: TelemetryDomainData = TelemetryDomainData(),
    val system: SystemDomainData = SystemDomainData(),
    val gpu: GpuDomainData = GpuDomainData(),
    val drm: DrmDomainData = DrmDomainData(),
    val sensors: SensorsDomainData = SensorsDomainData(),
    val npu: NpuDomainData = NpuDomainData(),
    val media: MediaDomainData = MediaDomainData(),
    val network: NetworkDomainData = NetworkDomainData(),
    val modules: ModulesDomainData = ModulesDomainData(),
    val fps: FpsDomainData = FpsDomainData(),
)
