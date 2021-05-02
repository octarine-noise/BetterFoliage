package mods.betterfoliage.resource.model

import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.*
import mods.betterfoliage.util.minmax
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.block.BlockRenderLayer
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import java.lang.Math.max
import java.lang.Math.min
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vertex UV coordinates
 *
 * Zero-centered: sprite coordinates fall between (-0.5, 0.5)
 */
data class UV(val u: Double, val v: Double) {
    companion object {
        val topLeft = UV(-0.5, -0.5)
        val topRight = UV(0.5, -0.5)
        val bottomLeft = UV(-0.5, 0.5)
        val bottomRight = UV(0.5, 0.5)
    }

    val rotate: UV get() = UV(v, -u)

    fun rotate(n: Int) = when(n % 4) {
        0 -> copy()
        1 -> UV(v, -u)
        2 -> UV(-u, -v)
        else -> UV(-v, u)
    }

    fun clamp(minU: Double = -0.5, maxU: Double = 0.5, minV: Double = -0.5, maxV: Double = 0.5) =
        UV(u.minmax(minU, maxU), v.minmax(minV, maxV))

    fun mirror(mirrorU: Boolean, mirrorV: Boolean) = UV(if (mirrorU) -u else u, if (mirrorV) -v else v)

    fun unbake(sprite: Sprite) = UV(
        (u - sprite.minU.toDouble()) / (sprite.maxU - sprite.minU).toDouble() - 0.5,
        (v - sprite.minV.toDouble()) / (sprite.maxV - sprite.minV).toDouble() - 0.5
    )
}

data class Color(val alpha: Int, val red: Int, val green: Int, val blue: Int) {
    constructor(combined: Int) : this(combined shr 24 and 255, combined shr 16 and 255, combined shr 8 and 255, combined and 255)
    val asInt get() = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    operator fun times(f: Float) = Color(
        alpha,
        (f * red.toFloat()).toInt().coerceIn(0 until 256),
        (f * green.toFloat()).toInt().coerceIn(0 until 256),
        (f * blue.toFloat()).toInt().coerceIn(0 until 256)
    )
    companion object {
        val white get() = Color(255, 255, 255, 255)
        /** Amount of vanilla diffuse lighting applied to face quads */
        fun bakeShade(dir: Direction?) = when(dir) {
            DOWN -> 0.5f
            NORTH, SOUTH -> 0.8f
            EAST, WEST -> 0.6f
            else -> 1.0f
        }
    }
}

data class HSB(var hue: Float, var saturation: Float, var brightness: Float) {
    companion object {
        fun fromColor(color: Int): HSB {
            val hsbVals = java.awt.Color.RGBtoHSB((color shr 16) and 255, (color shr 8) and 255, color and 255, null)
            return HSB(hsbVals[0], hsbVals[1], hsbVals[2])
        }
    }
    val asColor: Int get() = java.awt.Color.HSBtoRGB(hue, saturation, brightness)
}

/**
 * Model vertex
 *
 * @param[xyz] x, y, z coordinates
 * @param[uv] u, v coordinates
 * @param[color] vertex color RGB components
 * @param[alpha] vertex color alpha component
 */
data class Vertex(val xyz: Double3 = Double3(0.0, 0.0, 0.0),
                  val uv: UV = UV(0.0, 0.0),
                  val color: Color = Color.white,
                  val alpha: Int = 255,
                  val normal: Double3? = null
)

/**
 * Intermediate (fabric-renderer-api independent) representation of model quad
 * Immutable, double-precision
 * Zero-centered (both XYZ and UV) coordinates for simpler rotation/mirroring
 */
data class Quad(
    val v1: Vertex, val v2: Vertex, val v3: Vertex, val v4: Vertex,
    val sprite: Sprite? = null,
    val colorIndex: Int = -1,
    val face: Direction? = null
) {
    val verts = arrayOf(v1, v2, v3, v4)

    inline fun transformV(trans: (Vertex)-> Vertex): Quad = transformVI { vertex, idx -> trans(vertex) }
    inline fun transformVI(trans: (Vertex, Int)-> Vertex): Quad = copy(
        v1 = trans(v1, 0), v2 = trans(v2, 1), v3 = trans(v3, 2), v4 = trans(v4, 3)
    )
    val normal: Double3 get() = (v2.xyz - v1.xyz).cross(v4.xyz - v1.xyz).normalize

    fun move(trans: Double3) = transformV { it.copy(xyz = it.xyz + trans) }
    fun move(trans: Pair<Double, Direction>) = move(Double3(trans.second) * trans.first)
    fun scale (scale: Double) = transformV { it.copy(xyz = it.xyz * scale) }
    fun scale (scale: Double3) = transformV { it.copy(xyz = Double3(it.xyz.x * scale.x, it.xyz.y * scale.y, it.xyz.z * scale.z)) }
    fun rotate(rot: Rotation) = transformV { it.copy(xyz = it.xyz.rotate(rot), normal = it.normal?.rotate(rot)) }.copy(face = face?.rotate(rot))
    fun rotateZ(angle: Double) = transformV { it.copy(
        xyz = Double3(it.xyz.x * cos(angle) + it.xyz.z * sin(angle), it.xyz.y, it.xyz.z * cos(angle) - it.xyz.x * sin(angle)),
        normal = it.normal?.let { normal-> Double3(normal.x * cos(angle) + normal.z * sin(angle), normal.y, normal.z * cos(angle) - normal.x * sin(angle)) }
    ) }

    fun scaleUV (scale: Double) = transformV { it.copy(uv = UV(it.uv.u * scale, it.uv.v * scale)) }
    fun rotateUV(n: Int) = transformV { it.copy(uv = it.uv.rotate(n)) }
    fun clampUV(minU: Double = -0.5, maxU: Double = 0.5, minV: Double = -0.5, maxV: Double = 0.5) =
        transformV { it.copy(uv = it.uv.clamp(minU, maxU, minV, maxV)) }
    fun mirrorUV(mirrorU: Boolean, mirrorV: Boolean) = transformV { it.copy(uv = it.uv.mirror(mirrorU, mirrorV)) }
    fun scrambleUV(random: Random, canFlipU: Boolean, canFlipV: Boolean, canRotate: Boolean) = this
        .mirrorUV(canFlipU && random.nextBoolean(), canFlipV && random.nextBoolean())
        .let { if (canRotate) it.rotateUV(random.nextInt(4)) else it }

    fun sprite(sprite: Sprite) = copy(sprite = sprite)
    fun color(color: Color) = transformV { it.copy(color = color) }
    fun color(color: Int) = transformV { it.copy(color = Color(color)) }
    fun colorIndex(colorIndex: Int) = copy(colorIndex = colorIndex)
    fun colorAndIndex(color: Int?) = color(color ?: Color.white.asInt).colorIndex(if (color == null) 0 else -1)

    val flipped: Quad get() = Quad(v4, v3, v2, v1, sprite, colorIndex)
    fun cycleVertices(n: Int) = when(n % 4) {
        1 -> Quad(v2, v3, v4, v1)
        2 -> Quad(v3, v4, v1, v2)
        3 -> Quad(v4, v1, v2, v3)
        else -> this.copy()
    }

    companion object {
        fun mix(first: Quad, second: Quad, vertexFactory: (Vertex, Vertex)-> Vertex) = Quad(
            v1 = vertexFactory(first.v1, second.v1),
            v2 = vertexFactory(first.v2, second.v2),
            v3 = vertexFactory(first.v3, second.v3),
            v4 = vertexFactory(first.v4, second.v4)
        )
    }
}

fun List<Quad>.transform(trans: Quad.()-> Quad) = map { it.trans() }
fun Array<List<Quad>>.transform(trans: Quad.(Int)-> Quad) = mapIndexed { idx, qList -> qList.map { it.trans(idx) } }.toTypedArray()

fun List<Quad>.withOpposites() = flatMap { listOf(it, it.flipped) }
fun Array<List<Quad>>.withOpposites() = map { it.withOpposites() }.toTypedArray()

/**
 * Pour quad data into a fabric-renderer-api Mesh
 */
fun List<Quad>.build(layer: BlockRenderLayer, noDiffuse: Boolean = false, flatLighting: Boolean = false): Mesh {
    val renderer = RendererAccess.INSTANCE.renderer
    val material = renderer.materialFinder().blendMode(0, layer).disableAo(0, flatLighting).disableDiffuse(0, noDiffuse).find()
    val builder = renderer.meshBuilder()
    builder.emitter.apply {
        forEach { quad ->
            val sprite = quad.sprite ?: Atlas.BLOCKS.atlas[MissingSprite.getMissingSpriteId()]!!
            quad.verts.forEachIndexed { idx, vertex ->
                pos(idx, (vertex.xyz + Double3(0.5, 0.5, 0.5)).asVec3f)
                sprite(idx, 0,
                    (sprite.maxU - sprite.minU) * (vertex.uv.u.toFloat() + 0.5f) + sprite.minU,
                    (sprite.maxV - sprite.minV) * (vertex.uv.v.toFloat() + 0.5f) + sprite.minV
                )
                spriteColor(idx, 0, vertex.color.asInt)
            }
            cullFace(quad.face)
            colorIndex(quad.colorIndex)
            material(material)
            emit()
        }
    }
    return builder.build()
}

fun Array<List<Quad>>.build(layer: BlockRenderLayer, noDiffuse: Boolean = false, flatLighting: Boolean = false) = map { it.build(layer, noDiffuse, flatLighting) }.toTypedArray()

/**
 * The model should be positioned so that (0,0,0) is the block center.
 * The block extends to (-0.5, 0.5) in all directions (inclusive).
 */
fun verticalRectangle(x1: Double, z1: Double, x2: Double, z2: Double, yBottom: Double, yTop: Double) = Quad(
    Vertex(Double3(x1, yBottom, z1), UV.bottomLeft),
    Vertex(Double3(x2, yBottom, z2), UV.bottomRight),
    Vertex(Double3(x2, yTop, z2), UV.topRight),
    Vertex(Double3(x1, yTop, z1), UV.topLeft)
)

fun horizontalRectangle(x1: Double, z1: Double, x2: Double, z2: Double, y: Double): Quad {
    val xMin = min(x1, x2); val xMax = max(x1, x2)
    val zMin = min(z1, z2); val zMax = max(z1, z2)
    return Quad(
        Vertex(Double3(xMin, y, zMin), UV.topLeft),
        Vertex(Double3(xMin, y, zMax), UV.bottomLeft),
        Vertex(Double3(xMax, y, zMax), UV.bottomRight),
        Vertex(Double3(xMax, y, zMin), UV.topRight)
    )
}

fun faceQuad(face: Direction): Quad {
    val base = face.vec * 0.5
    val top = boxFaces[face].top * 0.5
    val left = boxFaces[face].left * 0.5
    return Quad(
        Vertex(base + top + left, UV.topLeft),
        Vertex(base - top + left, UV.bottomLeft),
        Vertex(base - top - left, UV.bottomRight),
        Vertex(base + top - left, UV.topRight),
        face = face
    )
}

fun xzDisk(modelIdx: Int) = (PI2 * modelIdx / 64.0).let { Double3(cos(it), 0.0, sin(it)) }