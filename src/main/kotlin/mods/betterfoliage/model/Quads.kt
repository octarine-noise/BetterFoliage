package mods.betterfoliage.model

import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.boxFaces
import mods.betterfoliage.util.get
import mods.betterfoliage.util.minmax
import mods.betterfoliage.util.nearestAngle
import mods.betterfoliage.util.rotate
import mods.betterfoliage.util.times
import mods.betterfoliage.util.vec
import net.minecraft.client.renderer.texture.NativeImage
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction
import java.lang.Math.max
import java.lang.Math.min
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vertex UV coordinates
 *
 * Zero-centered: coordinates fall between (-0.5, 0.5) (inclusive)
 */
data class UV(val u: Double, val v: Double) {
    companion object {
        val topLeft = UV(-0.5, -0.5)
        val topRight = UV(0.5, -0.5)
        val bottomLeft = UV(-0.5, 0.5)
        val bottomRight = UV(0.5, 0.5)
    }

    val rotate: UV get() = UV(v, -u)

    fun rotate(n: Int) = when (n % 4) {
        0 -> copy()
        1 -> UV(v, -u)
        2 -> UV(-u, -v)
        else -> UV(-v, u)
    }

    fun clamp(minU: Double = -0.5, maxU: Double = 0.5, minV: Double = -0.5, maxV: Double = 0.5) =
        UV(u.minmax(minU, maxU), v.minmax(minV, maxV))

    fun mirror(mirrorU: Boolean, mirrorV: Boolean) = UV(if (mirrorU) -u else u, if (mirrorV) -v else v)
}

/**
 * Model vertex
 *
 * @param[xyz] x, y, z coordinates
 * @param[uv] u, v coordinates
 * @param[aoShader] [ModelLighter] instance to use with AO rendering
 * @param[flatShader] [ModelLighter] instance to use with non-AO rendering
 */
data class Vertex(
    val xyz: Double3 = Double3(0.0, 0.0, 0.0),
    val uv: UV = UV(0.0, 0.0),
    val color: Color = Color.white,
    val normal: Double3? = null
)

data class Color(val alpha: Int, val red: Int, val green: Int, val blue: Int) {
    constructor(combined: Int) : this(
        combined shr 24 and 255,
        combined shr 16 and 255,
        combined shr 8 and 255,
        combined and 255
    )

    val asInt get() = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    operator fun times(f: Float) = Color(
        alpha,
        (f * red.toFloat()).toInt().coerceIn(0 until 256),
        (f * green.toFloat()).toInt().coerceIn(0 until 256),
        (f * blue.toFloat()).toInt().coerceIn(0 until 256)
    )

    companion object {
        val white get() = Color(255, 255, 255, 255)
    }
}

data class HSB(var hue: Float, var saturation: Float, var brightness: Float) {
    companion object {
        /** Red is assumed to be LSB, see [NativeImage.PixelFormat.RGBA] */
        fun fromColorRGBA(color: Int): HSB {
            val hsbVals = java.awt.Color.RGBtoHSB(color and 255, (color shr 8) and 255, (color shr 16) and 255, null)
            return HSB(hsbVals[0], hsbVals[1], hsbVals[2])
        }
        fun fromColorBGRA(color: Int): HSB {
            val hsbVals = java.awt.Color.RGBtoHSB((color shr 16) and 255, (color shr 8) and 255, color and 255, null)
            return HSB(hsbVals[0], hsbVals[1], hsbVals[2])
        }
    }
    val asInt: Int get() = java.awt.Color.HSBtoRGB(hue, saturation, brightness)
    val asColor: Color get() = Color(asInt)
}

/**
 * Intermediate representation of model quad
 * Immutable, double-precision
 * Zero-centered (both XYZ and UV) coordinates for simpler rotation/mirroring
 */
data class Quad(
    val v1: Vertex, val v2: Vertex, val v3: Vertex, val v4: Vertex,
    val sprite: TextureAtlasSprite? = null,
    val colorIndex: Int = -1,
    val face: Direction? = null
) {
    val verts = arrayOf(v1, v2, v3, v4)

    inline fun transformV(trans: (Vertex) -> Vertex): Quad = transformVI { vertex, idx -> trans(vertex) }
    inline fun transformVI(trans: (Vertex, Int) -> Vertex): Quad = copy(
        v1 = trans(v1, 0), v2 = trans(v2, 1), v3 = trans(v3, 2), v4 = trans(v4, 3)
    )

    val normal: Double3 get() = (v2.xyz - v1.xyz).cross(v4.xyz - v1.xyz).normalize

    fun move(trans: Double3) = transformV { it.copy(xyz = it.xyz + trans) }
    fun move(trans: Pair<Double, Direction>) = move(Double3(trans.second) * trans.first)
    fun scale(scale: Double) = transformV { it.copy(xyz = it.xyz * scale) }
    fun scale(scale: Double3) =
        transformV { it.copy(xyz = Double3(it.xyz.x * scale.x, it.xyz.y * scale.y, it.xyz.z * scale.z)) }

    fun rotate(rot: Rotation) =
        transformV { it.copy(xyz = it.xyz.rotate(rot), normal = it.normal?.rotate(rot)) }.copy(face = face?.rotate(rot))

    fun rotateZ(angle: Double) = transformV {
        it.copy(
            xyz = Double3(
                it.xyz.x * cos(angle) + it.xyz.z * sin(angle),
                it.xyz.y,
                it.xyz.z * cos(angle) - it.xyz.x * sin(angle)
            ),
            normal = it.normal?.let { normal ->
                Double3(
                    normal.x * cos(angle) + normal.z * sin(angle),
                    normal.y,
                    normal.z * cos(angle) - normal.x * sin(angle)
                )
            }
        )
    }

    fun scaleUV(scale: Double) = transformV { it.copy(uv = UV(it.uv.u * scale, it.uv.v * scale)) }
    fun rotateUV(n: Int) = transformV { it.copy(uv = it.uv.rotate(n)) }
    fun clampUV(minU: Double = -0.5, maxU: Double = 0.5, minV: Double = -0.5, maxV: Double = 0.5) =
        transformV { it.copy(uv = it.uv.clamp(minU, maxU, minV, maxV)) }
    fun mirrorUV(mirrorU: Boolean, mirrorV: Boolean) = transformV { it.copy(uv = it.uv.mirror(mirrorU, mirrorV)) }
    fun scrambleUV(random: Random, canFlipU: Boolean, canFlipV: Boolean, canRotate: Boolean) = this
        .mirrorUV(canFlipU && random.nextBoolean(), canFlipV && random.nextBoolean())
        .let { if (canRotate) it.rotateUV(random.nextInt(4)) else it }

    fun sprite(sprite: TextureAtlasSprite) = copy(sprite = sprite)
    fun color(color: Color) = transformV { it.copy(color = color) }
    fun color(color: Int) = transformV { it.copy(color = Color(color)) }
    fun colorIndex(colorIndex: Int) = copy(colorIndex = colorIndex)
    fun colorAndIndex(color: Color?) = color(color ?: Color.white).colorIndex(if (color == null) 0 else -1)

    fun face() = face ?: nearestAngle(normal, Direction.values().toList()) { it.vec }.first

    val flipped: Quad get() = Quad(v4, v3, v2, v1, sprite, colorIndex)
    fun cycleVertices(n: Int) = when (n % 4) {
        1 -> Quad(v2, v3, v4, v1)
        2 -> Quad(v3, v4, v1, v2)
        3 -> Quad(v4, v1, v2, v3)
        else -> this.copy()
    }

    companion object {
        fun mix(first: Quad, second: Quad, vertexFactory: (Vertex, Vertex) -> Vertex) = Quad(
            v1 = vertexFactory(first.v1, second.v1),
            v2 = vertexFactory(first.v2, second.v2),
            v3 = vertexFactory(first.v3, second.v3),
            v4 = vertexFactory(first.v4, second.v4)
        )

        fun verticalRectangle(x1: Double, z1: Double, x2: Double, z2: Double, yBottom: Double, yTop: Double) = Quad(
            Vertex(Double3(x1, yBottom, z1), UV.bottomLeft),
            Vertex(Double3(x2, yBottom, z2), UV.bottomRight),
            Vertex(Double3(x2, yTop, z2), UV.topRight),
            Vertex(Double3(x1, yTop, z1), UV.topLeft)
        )

        fun horizontalRectangle(x1: Double, z1: Double, x2: Double, z2: Double, y: Double): Quad {
            val xMin = min(x1, x2);
            val xMax = max(x1, x2)
            val zMin = min(z1, z2);
            val zMax = max(z1, z2)
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
                Vertex(base + top - left, UV.topRight)
            )
        }
    }
}
