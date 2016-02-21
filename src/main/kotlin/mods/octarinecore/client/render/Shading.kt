package mods.octarinecore.client.render

import mods.octarinecore.common.*
import net.minecraft.client.renderer.BlockModelRenderer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import java.lang.Math.min
import java.util.*

/** Holds shading values for block corners as calculated by vanilla Minecraft rendering. */
class AoData() {
    var valid = false
    var brightness = 0
    var red: Float = 0.0f
    var green: Float = 0.0f
    var blue: Float = 0.0f

    fun reset() { valid = false }

    fun set(brightness: Int, red: Float, green: Float, blue: Float) {
        if (valid) return
        this.valid = true
        this.brightness = brightness
        this.red = red
        this.green = green
        this.blue = blue
    }

    fun set(brightness: Int, colorMultiplier: Float) {
        this.valid = true
        this.brightness = brightness
        this.red = colorMultiplier
        this.green = colorMultiplier
        this.blue = colorMultiplier
    }

    companion object {
        val black = AoData();
    }
}

class AoFaceData(val face: EnumFacing) {
    val ao = BlockModelRenderer().AmbientOcclusionFace()
    val top = faceCorners[face.ordinal].topLeft.first
    val left = faceCorners[face.ordinal].topLeft.second

    val topLeft = AoData()
    val topRight = AoData()
    val bottomLeft = AoData()
    val bottomRight = AoData()
    val ordered = when(face) {
        DOWN -> listOf(topLeft, bottomLeft, bottomRight, topRight)
        UP -> listOf(bottomRight, topRight, topLeft, bottomLeft)
        NORTH -> listOf(bottomLeft, bottomRight, topRight, topLeft)
        SOUTH -> listOf(topLeft, bottomLeft, bottomRight, topRight)
        WEST -> listOf(bottomLeft, bottomRight, topRight, topLeft)
        EAST -> listOf(topRight, topLeft, bottomLeft, bottomRight)
    }

    fun update(offset: Int3, useBounds: Boolean = false, multiplier: Float = 1.0f) {
        val ctx = blockContext
        val blockState = ctx.blockState(offset)
        val quadBounds: FloatArray = FloatArray(12)
        val flags = BitSet(3).apply { set(0) }

        ao.updateVertexBrightness(ctx.world, blockState.block, ctx.pos + offset, face, quadBounds, flags)
        ordered.forEachIndexed { idx, aoData -> aoData.set(ao.vertexBrightness[idx], ao.vertexColorMultiplier[idx] * multiplier) }
    }

    operator fun get(dir1: EnumFacing, dir2: EnumFacing): AoData {
        val isTop = top == dir1 || top == dir2
        val isLeft = left == dir1 || left == dir2
        return if (isTop) {
            if (isLeft) topLeft else topRight
        } else {
            if (isLeft) bottomLeft else bottomRight
        }
    }
}

/**
 * Instances of this interface are associated with [Model] vertices, and used to apply brightness and color
 * values to a [RenderVertex].
 */
interface Shader {
    /**
     * Set shading values of a [RenderVertex]
     *
     * @param[context] context that can be queried for shading data in a [Model]-relative frame of reference
     * @param[vertex] the [RenderVertex] to manipulate
     */
    fun shade(context: ShadingContext, vertex: RenderVertex)

    /**
     * Return a new rotated version of this [Shader]. Used during [Model] setup when rotating the model itself.
     */
    fun rotate(rot: Rotation): Shader

    /** Set all shading values on the [RenderVertex] to match the given [AoData]. */
    fun RenderVertex.shade(shading: AoData) {
        brightness = shading.brightness; red = shading.red; green = shading.green; blue = shading.blue
    }

    /** Set the shading values on the [RenderVertex] to a weighted average of the two [AoData] instances. */
    fun RenderVertex.shade(shading1: AoData, shading2: AoData, weight1: Float = 0.5f, weight2: Float = 0.5f) {
        red = min(shading1.red * weight1 + shading2.red * weight2, 1.0f)
        green = min(shading1.green * weight1 + shading2.green * weight2, 1.0f)
        blue = min(shading1.blue * weight1 + shading2.blue * weight2, 1.0f)
        brightness = brWeighted(shading1.brightness, weight1, shading2.brightness, weight2)
    }

    /**
     * Set the shading values on the [RenderVertex] directly.
     *
     * @param[brightness] packed brightness value
     * @param[color] packed color value
     */
    fun RenderVertex.shade(brightness: Int, color: Int) {
        this.brightness = brightness; setColor(color)
    }
}

/**
 * Returns a shader resolver for quads that point towards one of the 6 block faces.
 * The resolver works the following way:
 *   - determines which face the _quad_ normal points towards (if not overridden)
 *   - determines the distance of the _vertex_ to the corners and edge midpoints on that block face
 *   - if _corner_ is given, and the _vertex_ is closest to a block corner, returns the [Shader] created by _corner_
 *   - if _edge_ is given, and the _vertex_ is closest to an edge midpoint, returns the [Shader] created by _edge_
 *
 * @param[overrideFace] assume the given face instead of going by the _quad_ normal
 * @param[corner] shader instantiation lambda for corner vertices
 * @param[edge] shader instantiation lambda for edge midpoint vertices
 */
fun faceOrientedAuto(overrideFace: EnumFacing? = null,
                     corner: ((EnumFacing, EnumFacing, EnumFacing)->Shader)? = null,
                     edge: ((EnumFacing, EnumFacing)->Shader)? = null) =
    fun(quad: Quad, vertex: Vertex): Shader {
        val quadFace = overrideFace ?: quad.normal.nearestCardinal
        val nearestCorner = nearestPosition(vertex.xyz, faceCorners[quadFace.ordinal].asList) {
            (quadFace.vec + it.first.vec + it.second.vec) * 0.5
        }
        val nearestEdge = nearestPosition(vertex.xyz, quadFace.perpendiculars) {
            (quadFace.vec + it.vec) * 0.5
        }
        if (edge != null && (nearestEdge.second < nearestCorner.second || corner == null))
            return edge(quadFace, nearestEdge.first)
        else return corner!!(quadFace, nearestCorner.first.first, nearestCorner.first.second)
    }

/**
 * Returns a shader resolver for quads that point towards one of the 12 block edges.
 * The resolver works the following way:
 *   - determines which edge the _quad_ normal points towards (if not overridden)
 *   - determines which face midpoint the _vertex_ is closest to, of the 2 block faces that share this edge
 *   - determines which block corner _of this face_ the _vertex_ is closest to
 *   - returns the [Shader] created by _corner_
 *
 * @param[overrideEdge] assume the given edge instead of going by the _quad_ normal
 * @param[corner] shader instantiation lambda
 */
fun edgeOrientedAuto(overrideEdge: Pair<EnumFacing, EnumFacing>? = null,
                     corner: (EnumFacing, EnumFacing, EnumFacing)->Shader) =
    fun(quad: Quad, vertex: Vertex): Shader {
        val edgeDir = overrideEdge ?: nearestAngle(quad.normal, boxEdges) { it.first.vec + it.second.vec }.first
        val nearestFace = nearestPosition(vertex.xyz, edgeDir.toList()) { it.vec }.first
        val nearestCorner = nearestPosition(vertex.xyz, faceCorners[nearestFace.ordinal].asList) {
            (nearestFace.vec + it.first.vec + it.second.vec) * 0.5
        }.first
        return corner(nearestFace, nearestCorner.first, nearestCorner.second)
    }

fun faceOrientedInterpolate(overrideFace: EnumFacing? = null) =
    fun(quad: Quad, vertex: Vertex): Shader {
        val resolver = faceOrientedAuto(overrideFace, edge = { face, edgeDir ->
            val axis = axes.find { it != face.axis && it != edgeDir.axis }!!
            val vec = Double3((axis to EnumFacing.AxisDirection.POSITIVE).face)
            val pos = vertex.xyz.dot(vec)
            EdgeInterpolateFallback(face, edgeDir, pos)
        })
        return resolver(quad, vertex)
    }