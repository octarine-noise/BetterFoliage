package mods.betterfoliage.render.lighting

import mods.betterfoliage.render.old.Quad
import mods.betterfoliage.render.old.Vertex
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.axes
import mods.betterfoliage.util.boxEdges
import mods.betterfoliage.util.boxFaces
import mods.betterfoliage.util.face
import mods.betterfoliage.util.get
import mods.betterfoliage.util.nearestAngle
import mods.betterfoliage.util.nearestPosition
import mods.betterfoliage.util.perpendiculars
import mods.betterfoliage.util.vec
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import java.lang.Math.min

typealias EdgeShaderFactory = (Direction, Direction) -> ModelLighter
typealias CornerShaderFactory = (Direction, Direction, Direction) -> ModelLighter
typealias ShaderFactory = (Quad, Vertex) -> ModelLighter

/** Holds lighting values for block corners as calculated by vanilla Minecraft rendering. */
class CornerLightData {
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
        val black: CornerLightData get() = CornerLightData()
    }
}

/**
 * Instances of this interface are associated with [Model] vertices, and used to apply brightness and color
 * values to a [RenderVertex].
 */
interface ModelLighter {
    /**
     * Set shading values of a [RenderVertex]
     *
     * @param[context] context that can be queried for lighting data in a [Model]-relative frame of reference
     * @param[vertex] the [RenderVertex] to manipulate
     */
    fun shade(context: LightingCtx, vertex: RenderVertex)

    /**
     * Return a new rotated version of this [ModelLighter]. Used during [Model] setup when rotating the model itself.
     */
    fun rotate(rot: Rotation): ModelLighter

    /** Set all lighting values on the [RenderVertex] to match the given [CornerLightData]. */
    fun RenderVertex.shade(shading: CornerLightData) {
        brightness = shading.brightness; red = shading.red; green = shading.green; blue = shading.blue
    }

    /** Set the lighting values on the [RenderVertex] to a weighted average of the two [CornerLightData] instances. */
    fun RenderVertex.shade(shading1: CornerLightData, shading2: CornerLightData, weight1: Float = 0.5f, weight2: Float = 0.5f) {
        red = min(shading1.red * weight1 + shading2.red * weight2, 1.0f)
        green = min(shading1.green * weight1 + shading2.green * weight2, 1.0f)
        blue = min(shading1.blue * weight1 + shading2.blue * weight2, 1.0f)
        brightness = brWeighted(shading1.brightness, weight1, shading2.brightness, weight2)
    }

    /**
     * Set the lighting values on the [RenderVertex] directly.
     *
     * @param[brightness] packed brightness value
     * @param[color] packed color value
     */
    fun RenderVertex.shade(brightness: Int, color: Int) {
        this.brightness = brightness; setColor(color)
    }
}

/**
 * Returns a [ModelLighter] resolver for quads that point towards one of the 6 block faces.
 * The resolver works the following way:
 *   - determines which face the _quad_ normal points towards (if not overridden)
 *   - determines the distance of the _vertex_ to the corners and edge midpoints on that block face
 *   - if _corner_ is given, and the _vertex_ is closest to a block corner, returns the [ModelLighter] created by _corner_
 *   - if _edge_ is given, and the _vertex_ is closest to an edge midpoint, returns the [ModelLighter] created by _edge_
 *
 * @param[overrideFace] assume the given face instead of going by the _quad_ normal
 * @param[corner] [ModelLighter] instantiation lambda for corner vertices
 * @param[edge] [ModelLighter] instantiation lambda for edge midpoint vertices
 */
fun faceOrientedAuto(overrideFace: Direction? = null,
                     corner: CornerShaderFactory? = null,
                     edge: EdgeShaderFactory? = null) =
    fun(quad: Quad, vertex: Vertex): ModelLighter {
        val quadFace = overrideFace ?: quad.normal.nearestCardinal
        val nearestCorner = nearestPosition(vertex.xyz, boxFaces[quadFace].allCorners) {
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
 * Returns a ModelLighter resolver for quads that point towards one of the 12 block edges.
 * The resolver works the following way:
 *   - determines which edge the _quad_ normal points towards (if not overridden)
 *   - determines which face midpoint the _vertex_ is closest to, of the 2 block faces that share this edge
 *   - determines which block corner _of this face_ the _vertex_ is closest to
 *   - returns the [ModelLighter] created by _corner_
 *
 * @param[overrideEdge] assume the given edge instead of going by the _quad_ normal
 * @param[corner] ModelLighter instantiation lambda
 */
fun edgeOrientedAuto(overrideEdge: Pair<Direction, Direction>? = null,
                     corner: CornerShaderFactory
) =
    fun(quad: Quad, vertex: Vertex): ModelLighter {
        val edgeDir = overrideEdge ?: nearestAngle(quad.normal, boxEdges) { it.first.vec + it.second.vec }.first
        val nearestFace = nearestPosition(vertex.xyz, edgeDir.toList()) { it.vec }.first
        val nearestCorner = nearestPosition(vertex.xyz, boxFaces[nearestFace].allCorners) {
            (nearestFace.vec + it.first.vec + it.second.vec) * 0.5
        }.first
        return corner(nearestFace, nearestCorner.first, nearestCorner.second)
    }

fun faceOrientedInterpolate(overrideFace: Direction? = null) =
    fun(quad: Quad, vertex: Vertex): ModelLighter {
        val resolver = faceOrientedAuto(overrideFace, edge = { face, edgeDir ->
            val axis = axes.find { it != face.axis && it != edgeDir.axis }!!
            val vec = Double3((axis to AxisDirection.POSITIVE).face)
            val pos = vertex.xyz.dot(vec)
            EdgeInterpolateFallback(face, edgeDir, pos)
        })
        return resolver(quad, vertex)
    }