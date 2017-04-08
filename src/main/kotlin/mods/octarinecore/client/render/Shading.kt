package mods.octarinecore.client.render

import net.minecraftforge.common.util.ForgeDirection
import java.lang.Math.*

typealias EdgeShaderFactory = (ForgeDirection, ForgeDirection) -> Shader
typealias CornerShaderFactory = (ForgeDirection, ForgeDirection, ForgeDirection) -> Shader
typealias ShaderFactory = (Quad, Vertex) -> Shader

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

    companion object {
        val black = AoData();
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
 * Returns a shader factory for quads that point towards one of the 6 block faces.
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
fun faceOrientedAuto(overrideFace: ForgeDirection? = null,
                     corner: CornerShaderFactory? = null,
                     edge: EdgeShaderFactory? = null) =
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
 * Returns a shader factory for quads that point towards one of the 12 block edges.
 * The resolver works the following way:
 *   - determines which edge the _quad_ normal points towards (if not overridden)
 *   - determines which face midpoint the _vertex_ is closest to, of the 2 block faces that share this edge
 *   - determines which block corner _of this face_ the _vertex_ is closest to
 *   - returns the [Shader] created by _corner_
 *
 * @param[overrideEdge] assume the given edge instead of going by the _quad_ normal
 * @param[corner] shader instantiation lambda
 */
fun edgeOrientedAuto(overrideEdge: Pair<ForgeDirection, ForgeDirection>? = null,
                     corner: CornerShaderFactory) =
    fun(quad: Quad, vertex: Vertex): Shader {
        val edgeDir = overrideEdge ?: nearestAngle(quad.normal, boxEdges) { it.first.vec + it.second.vec }.first
        val nearestFace = nearestPosition(vertex.xyz, edgeDir.toList()) { it.vec }.first
        val nearestCorner = nearestPosition(vertex.xyz, faceCorners[nearestFace.ordinal].asList) {
            (nearestFace.vec + it.first.vec + it.second.vec) * 0.5
        }.first
        return corner(nearestFace, nearestCorner.first, nearestCorner.second)
    }

fun faceOrientedInterpolate(overrideFace: ForgeDirection? = null) =
    fun(quad: Quad, vertex: Vertex): Shader {
        val resolver = faceOrientedAuto(overrideFace, edge = { face, edgeDir ->
            val axis = axes.find { it != face.axis && it != edgeDir.axis }!!
            val vec = Double3((axis to Dir.P).face)
            val pos = vertex.xyz.dot(vec)
            EdgeInterpolateFallback(face, edgeDir, pos)
        })
        return resolver(quad, vertex)
    }