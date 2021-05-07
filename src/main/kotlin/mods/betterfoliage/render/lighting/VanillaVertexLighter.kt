package mods.betterfoliage.render.lighting

import mods.betterfoliage.model.HalfBakedQuad
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.EPSILON
import mods.betterfoliage.util.minBy
import net.minecraft.client.renderer.color.BlockColors
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import net.minecraft.util.Direction.Axis
import net.minecraftforge.client.model.pipeline.LightUtil
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
        val factor = LightUtil.diffuseLight(face)
        tint[0] *= factor; tint[1] *= factor; tint[2] *= factor
    }
}

abstract class VanillaVertexLighter {
    abstract fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting)

    inline fun VanillaQuadLighting.updateWithCornerAo(quad: HalfBakedQuad, func: (Double3)->Int?) {
        quad.raw.verts.forEachIndexed { idx, vertex ->
            func(vertex.xyz)?.let {
                packedLight[idx] = calc.aoData[it].packedLight
                colorMultiplier[idx] = calc.aoData[it].colorMultiplier
            }
        }
    }
}

object VanillaFullBlockLighting : VanillaVertexLighter() {
    override fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting) {
        // TODO bounds checking
        val face = quad.raw.face()
        lighting.calc.fillLightData(face, true)
        lighting.updateWithCornerAo(quad) { nearestCornerOnFace(it, face) }
        lighting.updateBlockTint(quad.baked.tintIndex)
        if (quad.baked.shouldApplyDiffuseLighting()) lighting.applyDiffuseLighting(face)
    }
}

object RoundLeafLighting : VanillaVertexLighter() {
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

class LightingPreferredFace(val face: Direction) : VanillaVertexLighter() {
    override fun updateLightmapAndColor(quad: HalfBakedQuad, lighting: VanillaQuadLighting) {
        lighting.calc.fillLightData(face)
        lighting.updateWithCornerAo(quad) { nearestCornerOnFace(it, face) }
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

fun faceDistance(face: Direction, pos: Double3) = when(face) {
    WEST -> pos.x; EAST -> 1.0 - pos.x
    DOWN -> pos.y; UP -> 1.0 - pos.y
    NORTH -> pos.z; SOUTH -> 1.0 - pos.z
}