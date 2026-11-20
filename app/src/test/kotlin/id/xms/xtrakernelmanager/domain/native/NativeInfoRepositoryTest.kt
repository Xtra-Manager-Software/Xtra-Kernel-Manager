package id.xms.xtrakernelmanager.domain.native

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test verifying JNI repository fallback handling, JSON parsing,
 * and domain snapshot models under JVM test environment.
 */
class NativeInfoRepositoryTest {

    @Test
    fun testLoadAllDomainsGracefulFallbackWhenNativeNotLinked() = runTest {
        val snapshot = NativeInfoRepository.loadAllDomains()

        assertNotNull("Snapshot must not be null", snapshot)
        assertNotNull("Telemetry data must not be null", snapshot.telemetry)
        assertNotNull("System data must not be null", snapshot.system)
        assertNotNull("GPU data must not be null", snapshot.gpu)
        assertNotNull("DRM data must not be null", snapshot.drm)
        assertNotNull("Sensors data must not be null", snapshot.sensors)
        assertNotNull("NPU data must not be null", snapshot.npu)
        assertNotNull("Media data must not be null", snapshot.media)
        assertNotNull("Network data must not be null", snapshot.network)
        assertNotNull("Modules data must not be null", snapshot.modules)
        assertNotNull("FPS data must not be null", snapshot.fps)
    }

    @Test
    fun testMetricItemCreation() {
        val metric = MetricItem(
            label = "Test Label",
            value = "100%",
            badge = "Active",
            isMonospace = true,
        )

        assertEquals("Test Label", metric.label)
        assertEquals("100%", metric.value)
        assertEquals("Active", metric.badge)
        assertTrue(metric.isMonospace)
    }
}
