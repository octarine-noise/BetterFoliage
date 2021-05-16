package mods.betterfoliage.render.lighting

import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.model.HalfBakedQuad
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.EPSILON_ONE
import mods.betterfoliage.util.EPSILON_ZERO
import mods.betterfoliage.util.get
import mods.betterfoliage.util.minBy
import net.minecraft.client.renderer.color.BlockColors
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import kotlin.math.abs

class VanillaQuadLighting {
    val packedLight = IntArray(4)
    val colorMultiplier = FloatArray(4)
    val tint = FloatArray(3)

    val calc = VanillaAoCalculator()
    lateinit var blockColors: BlockColors

    fun updateBlockTint(tintIndex: Int) {
        if (tintIndex == -1) {
            tint[0] = 1.0f; tint[1] = 1.0f; tint[2] = 1.0f
        } else {
            val state = calc.world.getBlockState(calc.blockPos)
            blockColors.getColor(state, calc.world, calc.blockPos, tintIndex).let { blockTint ->
                tint[0] = (blockTint shr 16 and 255).toFloat() / 255.0f
                tint[1] = (blockTint shr 8 and 255).toFloat() / 255.0f
                tint[2] = (blockTint and 255).toFloat() / 255.0f
            }
        }
    }

    fun applyDiffuseLighting(face: Direction) {
        val factor = ShadersModIntegration.diffuseShades[face]
        tint[0] *= factor; tint[1] *= factor; tint[2] *= factor
    }
}

abstract class VanillaVertexLighter {
    abstract fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting)

    /**
     * Update lighting for each vertex with AO values from one of the corners.
     * Does not calculate missing AO values!
     * @param quad the quad to shade
     * @param func selector function from vertex position to directed corner index of desired AO values
     */
    inline fun VanillaQuadLighting.updateWithCornerAo(quad: HalfBakedQuad, func: (Double3)->Int?) {
        quad.raw.verts.forEachIndexed { idx, vertex ->
            func(vertex.xyz)?.let {
                packedLight[idx] = calc.aoData[it].packedLight
                colorMultiplier[idx] = calc.aoData[it].colorMultiplier
            }
        }
    }
}

/**
 * Replicates vanilla shading for full blocks. Interpolation for non-full blocks
 * is not implemented.
 */
object VanillaFullBlockLighting : VanillaVertexLighter() {
    override fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting) {
        // TODO bounds checking & interpolation
        val face = quad.raw.face()
        lighting.calc.fillLightData(face, true)
        lighting.updateWithCornerAo(quad) { nearestCornerOnFace(it, face) }
        lighting.updateBlockTint(quad.baked.tintIndex)
        if (quad.baked.isShade) lighting.applyDiffuseLighting(face)
    }
}

object RoundLeafLightingPreferUp : VanillaVertexLighter() {
    override fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting) {
        val angles = getAngles45(quad)?.let { normalFaces ->
            lighting.calc.fillLightData(normalFaces.first)
            lighting.calc.fillLightData(normalFaces.second)
            if (normalFaces.first != UP && normalFaces.second != UP) lighting.calc.fillLightData(UP)
            lighting.updateWithCornerAo(quad) { vertex ->
                val isUp = vertex.y > 0.5f
                val cornerUndir = AoSideHelper.getCornerUndir(vertex.x, vertex.y, vertex.z)
                val preferredFace = if (isUp) UP else normalFaces.minBy { faceDistance(it, vertex) }
                AoSideHelper.boxCornersDirFromUndir[preferredFace.ordinal][cornerUndir]
            }
            lighting.updateBlockTint(quad.baked.tintIndex)
        }
    }
}

/**
 * Lights vertices with the AO values from the nearest corner on either of
 * the 2 faces the quad normal points towards.
 */
object RoundLeafLighting : VanillaVertexLighter() {
    override fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting) {
        val angles = getAngles45(quad)?.let { normalFaces ->
            lighting.calc.fillLightData(normalFaces.first)
            lighting.calc.fillLightData(normalFaces.second)
            lighting.updateWithCornerAo(quad) { vertex ->
                val cornerUndir = AoSideHelper.getCornerUndir(vertex.x, vertex.y, vertex.z)
                val preferredFace = normalFaces.minBy { faceDistance(it, vertex) }
                AoSideHelper.boxCornersDirFromUndir[preferredFace.ordinal][cornerUndir]
            }
            lighting.updateBlockTint(quad.baked.tintIndex)
        }
    }
}

/**
 * Lights vertices with the AO values from the nearest corner on the preferred face.
 */
class LightingPreferredFace(val face: Direction) : VanillaVertexLighter() {
    override fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting) {
        lighting.calc.fillLightData(face)
        lighting.updateWithCornerAo(quad) { nearestCornerOnFace(it, face) }
        lighting.updateBlockTint(quad.baked.tintIndex)
    }
}

object ColumnLighting : VanillaVertexLighter() {
    override fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting) {
        // faces pointing in cardinal directions
        getNormalFace(quad)?.let { face ->
            lighting.calc.fillLightData(face)
            lighting.updateWithCornerAo(quad) { nearestCornerOnFace(it, face) }
            lighting.updateBlockTint(quad.baked.tintIndex)
            return
        }
        // faces pointing at 45deg angles
        getAngles45(quad)?.let { (face1, face2) ->
            lighting.calc.fillLightData(face1)
            lighting.calc.fillLightData(face2)
            quad.raw.verts.forEachIndexed { idx, vertex ->
                val cornerUndir = AoSideHelper.getCornerUndir(vertex.xyz.x, vertex.xyz.y, vertex.xyz.z)
                val cornerDir1 = AoSideHelper.boxCornersDirFromUndir[face1.ordinal][cornerUndir]
                val cornerDir2 = AoSideHelper.boxCornersDirFromUndir[face2.ordinal][cornerUndir]
                if (cornerDir1 == null || cornerDir2 == null) return@let
                val ao1 = lighting.calc.aoData[cornerDir1]
                val ao2 = lighting.calc.aoData[cornerDir2]
                lighting.packedLight[idx] = ((ao1.packedLight + ao2.packedLight) shr 1) and 0xFF00FF
                lighting.colorMultiplier[idx] = (ao1.colorMultiplier + ao2.colorMultiplier) * 0.5f
            }
            lighting.updateBlockTint(quad.baked.tintIndex)
            return
        }
        // something is wrong...
        lighting.updateWithCornerAo(quad) { nearestCornerOnFace(it, quad.raw.face()) }
        lighting.updateBlockTint(quad.baked.tintIndex)
    }
}

/**
 * Return the directed box corner index for the corner nearest the given vertex,
 * which is on the given face. May return null if the vertex is closest to
 * one of the opposite 4 corners
 */
fun nearestCornerOnFace(pos: Double3, face: Direction): Int? {
    val cornerUndir = AoSideHelper.getCornerUndir(pos.x, pos.y, pos.z)
    return AoSideHelper.boxCornersDirFromUndir[face.ordinal][cornerUndir]
}

/**
 * If the quad normal approximately bisects 2 axes at a 45 degree angle,
 * and is approximately perpendicular to the third, returns the 2 directions
 * the quad normal points towards.
 * Returns null otherwise.
 */
fun getAngles45(quad: HalfBakedQuad): Pair<Direction, Direction>? {
    val normal = quad.raw.normal
    // one of the components must be close to zero
    val zeroAxis = when {
        abs(normal.x) < EPSILON_ZERO -> Axis.X
        abs(normal.y) < EPSILON_ZERO -> Axis.Y
        abs(normal.z) < EPSILON_ZERO -> Axis.Z
        else -> return null
    }
    // the other two must be of similar magnitude
    val diff = when(zeroAxis) {
        Axis.X -> abs(abs(normal.y) - abs(normal.z))
        Axis.Y -> abs(abs(normal.x) - abs(normal.z))
        Axis.Z -> abs(abs(normal.x) - abs(normal.y))
    }
    if (diff > EPSILON_ZERO) return null
    return when(zeroAxis) {
        Axis.X -> Pair(if (normal.y > 0.0f) UP else DOWN, if (normal.z > 0.0f) SOUTH else NORTH)
        Axis.Y -> Pair(if (normal.x > 0.0f) EAST else WEST, if (normal.z > 0.0f) SOUTH else NORTH)
        Axis.Z -> Pair(if (normal.x > 0.0f) EAST else WEST, if (normal.y > 0.0f) UP else DOWN)
    }
}

fun getNormalFace(quad: HalfBakedQuad) = quad.raw.normal.let { normal ->
    when {
        normal.x > EPSILON_ONE -> EAST
        normal.x < -EPSILON_ONE -> WEST
        normal.y > EPSILON_ONE -> UP
        normal.y < -EPSILON_ONE -> DOWN
        normal.z > EPSILON_ONE -> SOUTH
        normal.z < -EPSILON_ONE -> NORTH
        else -> null
    }
}

fun faceDistance(face: Direction, pos: Double3) = when(face) {
    WEST -> pos.x; EAST -> 1.0 - pos.x
    DOWN -> pos.y; UP -> 1.0 - pos.y
    NORTH -> pos.z; SOUTH -> 1.0 - pos.z
}