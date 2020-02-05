package mods.betterfoliage.render.lighting

import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import kotlin.math.abs

val EPSILON = 0.05

interface CustomLighting {
    fun applyLighting(lighting: CustomLightingMeshConsumer, quad: QuadView, flat: Boolean, emissive: Boolean)
}

interface CustomLightingMeshConsumer {
    /** Clear cached block brightness and AO values */
    fun clearLighting()
    /** Fill AO/light cache for given face */
    fun fillAoData(lightFace: Direction)
    /** Set AO/light values for quad vertex */
    fun setLighting(vIdx: Int, ao: Float, light: Int)
    /** Get neighbor block brightness */
    fun brNeighbor(dir: Direction): Int
    /** Block brightness value */
    val brSelf: Int
    /** Cached AO values for all box face corners */
    val aoFull: FloatArray
    /** Cached light values for all box face corners */
    val lightFull: IntArray
}

/** Custom lighting used for protruding tuft quads (short grass, algae, cactus arms, etc.) */
fun grassTuftLighting(lightFace: Direction) = object : CustomLighting {
    override fun applyLighting(lighting: CustomLightingMeshConsumer, quad : QuadView, flat: Boolean, emissive: Boolean) {
        if (flat) lighting.flatForceNeighbor(quad, lightFace) else lighting.smoothWithFaceOverride(quad, lightFace)
    }
}

/** Custom lighting used for round leaves */
fun roundLeafLighting() = object : CustomLighting {
    override fun applyLighting(lighting: CustomLightingMeshConsumer, quad: QuadView, flat: Boolean, emissive: Boolean) {
        if (flat) lighting.flatMax(quad) else lighting.smooth45PreferUp(quad)
    }
}

/** Custom lighting used for reeds */
fun reedLighting() = object : CustomLighting {
    override fun applyLighting(lighting: CustomLightingMeshConsumer, quad: QuadView, flat: Boolean, emissive: Boolean) {
        lighting.flatForceNeighbor(quad, UP)
    }
}

/** Flat lighting, use neighbor brightness in the given direction */
fun CustomLightingMeshConsumer.flatForceNeighbor(quad: QuadView, lightFace: Direction) {
    for (vIdx in 0 until 4) {
        setLighting(vIdx, 1.0f, brNeighbor(lightFace))
    }
}

/** Smooth lighting, use *only* AO/light values on the given face (closest corner) */
fun CustomLightingMeshConsumer.smoothWithFaceOverride(quad: QuadView, lightFace: Direction) {
    fillAoData(lightFace)
    forEachVertex(quad) { vIdx, x, y, z ->
        val cornerUndir = getCornerUndir(x, y, z)
        cornerDirFromUndir[lightFace.ordinal][cornerUndir]?.let { aoCorner ->
            setLighting(vIdx, aoFull[aoCorner], lightFull[aoCorner])
        }
    }
}

/**
 * Smooth lighting scheme for 45-degree quads bisecting the box along 2 opposing face diagonals.
 *
 * Determine 2 *primary faces* based on the normal direction.
 * Take AO/light values *only* from the 2 primary faces *or* the UP direction,
 * based on which box corner is closest. Prefer taking values from the top face.
 */
fun CustomLightingMeshConsumer.smooth45PreferUp(quad: QuadView) {
    getAngles45(quad)?.let { normalFaces ->
        fillAoData(normalFaces.first)
        fillAoData(normalFaces.second)
        if (normalFaces.first != UP && normalFaces.second != UP) fillAoData(UP)
        forEachVertex(quad) { vIdx, x, y, z ->
            val isUp = y > 0.5f
            val cornerUndir = getCornerUndir(x, y, z)
            val preferredFace = if (isUp) UP else normalFaces.minBy { faceDistance(it, x, y, z) }
            val aoCorner = cornerDirFromUndir[preferredFace.ordinal][cornerUndir]!!
            setLighting(vIdx, aoFull[aoCorner], lightFull[aoCorner])
        }
    }
}

/** Flat lighting, use maximum neighbor brightness at the nearest box corner */
fun CustomLightingMeshConsumer.flatMax(quad: QuadView) {
    forEachVertex(quad) { vIdx, x, y, z ->
        val maxBrightness = cornersUndir[getCornerUndir(x, y, z)].maxValueBy { brNeighbor(it) }
        setLighting(vIdx, 1.0f, maxBrightness)
    }
}

/**
 * If the quad normal approximately bisects 2 axes at a 45 degree angle,
 * and is approximately perpendicular to the third, returns the 2 directions
 * the quad normal points towards.
 * Returns null otherwise.
 */
fun getAngles45(quad: QuadView): Pair<Direction, Direction>? {
    val normal = quad.faceNormal()
    // one of the components must be close to zero
    val zeroAxis = when {
        abs(normal.x) < EPSILON -> Axis.X
        abs(normal.y) < EPSILON -> Axis.Y
        abs(normal.z) < EPSILON -> Axis.Z
        else -> return null
    }
    // the other two must be of similar magnitude
    val diff = when(zeroAxis) {
        Axis.X -> abs(abs(normal.y) - abs(normal.z))
        Axis.Y -> abs(abs(normal.x) - abs(normal.z))
        Axis.Z -> abs(abs(normal.x) - abs(normal.y))
    }
    if (diff > EPSILON) return null
    return when(zeroAxis) {
        Axis.X -> Pair(if (normal.y > 0.0f) UP else DOWN, if (normal.z > 0.0f) SOUTH else NORTH)
        Axis.Y -> Pair(if (normal.x > 0.0f) EAST else WEST, if (normal.z > 0.0f) SOUTH else NORTH)
        Axis.Z -> Pair(if (normal.x > 0.0f) EAST else WEST, if (normal.y > 0.0f) UP else DOWN)
    }
}

fun faceDistance(face: Direction, x: Float, y: Float, z: Float) = when(face) {
    WEST -> x; EAST -> 1.0f - x
    DOWN -> y; UP -> 1.0f - y
    NORTH -> z; SOUTH -> 1.0f - z
}

inline fun forEachVertex(quad: QuadView, func: (vIdx: Int, x: Float, y: Float, z: Float)->Unit) {
    for (vIdx in 0..3) {
        func(vIdx, quad.x(vIdx), quad.y(vIdx), quad.z(vIdx))
    }
}