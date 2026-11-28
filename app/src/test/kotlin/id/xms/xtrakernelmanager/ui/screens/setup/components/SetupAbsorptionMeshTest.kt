package id.xms.xtrakernelmanager.ui.screens.setup.components

import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupAbsorptionMeshTest {
    private val mesh = SetupAbsorptionMesh(1000f, 2000f)

    @Test
    fun firstFramePreservesAllSourcePositions() {
        for (row in 0..mesh.rows) {
            for (column in 0..mesh.columns) {
                val i = (row * (mesh.columns + 1) + column) * 2
                assertEquals(1000f * column / mesh.columns, mesh.vertices[i], 0.001f)
                assertEquals(2000f * row / mesh.rows, mesh.vertices[i + 1], 0.001f)
            }
        }
    }

    @Test
    fun titleImageAndBottomButtonAllConvergeToOneCenter() {
        mesh.update(1f)
        for (i in mesh.vertices.indices step 2) {
            assertEquals(500f, mesh.vertices[i], 0.001f)
            assertEquals(1000f, mesh.vertices[i + 1], 0.001f)
        }
    }

    @Test
    fun verticesRemainFiniteAndNeverMoveAwayFromTheHole() {
        val previousRadii = FloatArray(mesh.vertices.size / 2) { Float.POSITIVE_INFINITY }
        for (frame in 0..100) {
            mesh.update(frame / 100f)
            for (i in previousRadii.indices) {
                val x = mesh.vertices[i * 2]
                val y = mesh.vertices[i * 2 + 1]
                assertTrue(x.isFinite() && y.isFinite())
                val radius = hypot(x - 500f, y - 1000f)
                assertTrue("A vertex moved outward at frame $frame", radius <= previousRadii[i] + 0.001f)
                previousRadii[i] = radius
            }
        }
    }

    @Test
    fun tidalPullElongatesRadiallyAndNarrowsAcrossTheStrand() {
        fun distance(rowA: Int, columnA: Int, rowB: Int, columnB: Int): Float {
            val a = (rowA * (mesh.columns + 1) + columnA) * 2
            val b = (rowB * (mesh.columns + 1) + columnB) * 2
            return hypot(mesh.vertices[a] - mesh.vertices[b], mesh.vertices[a + 1] - mesh.vertices[b + 1])
        }
        val radialBefore = distance(18, 24, 19, 24)
        val acrossBefore = distance(18, 23, 18, 25)
        mesh.update(0.2f)
        assertTrue(distance(18, 24, 19, 24) > radialBefore)
        assertTrue(distance(18, 23, 18, 25) < acrossBefore)
    }
}
