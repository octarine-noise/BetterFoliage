package mods.betterfoliage.render.lighting

import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.render.old.Quad
import mods.betterfoliage.render.old.Vertex
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Rotation
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction.*
import java.awt.Color

typealias QuadIconResolver = (CombinedContext, Int, Quad) -> TextureAtlasSprite?
typealias PostProcessLambda = RenderVertex.(CombinedContext, Int, Quad, Int, Vertex) -> Unit

@Suppress("NOTHING_TO_INLINE")
class RenderVertex {
    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    var u: Double = 0.0
    var v: Double = 0.0
    var brightness: Int = 0
    var red: Float = 0.0f
    var green: Float = 0.0f
    var blue: Float = 0.0f

    val rawData = IntArray(7)

    fun init(vertex: Vertex, rot: Rotation, trans: Double3): RenderVertex {
        val result = vertex.xyz.rotate(rot) + trans
        x = result.x; y = result.y; z = result.z
        return this
    }
    fun init(vertex: Vertex): RenderVertex {
        x = vertex.xyz.x; y = vertex.xyz.y; z = vertex.xyz.z;
        u = vertex.uv.u; v = vertex.uv.v
        return this
    }
    fun translate(trans: Double3): RenderVertex { x += trans.x; y += trans.y; z += trans.z; return this }
    fun rotate(rot: Rotation): RenderVertex {
        if (rot === Rotation.identity) return this
        val rotX = rot.rotatedComponent(EAST, x, y, z)
        val rotY = rot.rotatedComponent(UP, x, y, z)
        val rotZ = rot.rotatedComponent(SOUTH, x, y, z)
        x = rotX; y = rotY; z = rotZ
        return this
    }
    inline fun rotateUV(n: Int): RenderVertex {
        when (n % 4) {
            1 -> { val t = v; v = -u; u = t; return this }
            2 -> { u = -u; v = -v; return this }
            3 -> { val t = -v; v = u; u = t; return this }
            else -> { return this }
        }
    }
    inline fun mirrorUV(mirrorU: Boolean, mirrorV: Boolean) {
        if (mirrorU) u = -u
        if (mirrorV) v = -v
    }
    inline fun setIcon(icon: TextureAtlasSprite): RenderVertex {
        u = (icon.maxU - icon.minU) * (u + 0.5) + icon.minU
        v = (icon.maxV - icon.minV) * (v + 0.5) + icon.minV
        return this
    }

    inline fun setGrey(level: Float) {
        val grey = Math.min((red + green + blue) * 0.333f * level, 1.0f)
        red = grey; green = grey; blue = grey
    }
    inline fun multiplyColor(color: Int) {
        red *= (color shr 16 and 255) / 256.0f
        green *= (color shr 8 and 255) / 256.0f
        blue *= (color and 255) / 256.0f
    }
    inline fun setColor(color: Int) {
        red = (color shr 16 and 255) / 256.0f
        green = (color shr 8 and 255) / 256.0f
        blue = (color and 255) / 256.0f
    }

}

/** List of bit-shift offsets in packed brightness values where meaningful (4-bit) data is contained. */
var brightnessComponents = listOf(20, 4)

/** Multiply the components of this packed brightness value with the given [Float]. */
infix fun Int.brMul(f: Float): Int {
    val weight = (f * 256.0f).toInt()
    var result = 0
    brightnessComponents.forEach { shift ->
        val raw = (this shr shift) and 15
        val weighted = (raw) * weight / 256
        result = result or (weighted shl shift)
    }
    return result
}

/** Multiply the components of this packed color value with the given [Float]. */
infix fun Int.colorMul(f: Float): Int {
    val weight = (f * 256.0f).toInt()
    val red = (this shr 16 and 255) * weight / 256
    val green = (this shr 8 and 255) * weight / 256
    val blue = (this and 255) * weight / 256
    return (red shl 16) or (green shl 8) or blue
}

/** Sum the components of all packed brightness values given. */
fun brSum(multiplier: Float?, vararg brightness: Int): Int {
    val sum = Array(brightnessComponents.size) { 0 }
    brightnessComponents.forEachIndexed { idx, shift -> brightness.forEach { br ->
        val comp = (br shr shift) and 15
        sum[idx] += comp
    } }
    var result = 0
    brightnessComponents.forEachIndexed { idx, shift ->
        val comp = if (multiplier == null)
            ((sum[idx]) shl shift)
        else
            ((sum[idx].toFloat() * multiplier).toInt() shl shift)
        result = result or comp
    }
    return result
}

fun brWeighted(br1: Int, weight1: Float, br2: Int, weight2: Float): Int {
    val w1int = (weight1 * 256.0f + 0.5f).toInt()
    val w2int = (weight2 * 256.0f + 0.5f).toInt()
    var result = 0
    brightnessComponents.forEachIndexed { idx, shift ->
        val comp1 = (br1 shr shift) and 15
        val comp2 = (br2 shr shift) and 15
        val compWeighted = (comp1 * w1int + comp2 * w2int) / 256
        result = result or ((compWeighted and 15) shl shift)
    }
    return result
}

data class HSB(var hue: Float, var saturation: Float, var brightness: Float) {
    companion object {
        fun fromColor(color: Int): HSB {
            val hsbVals = Color.RGBtoHSB((color shr 16) and 255, (color shr 8) and 255, color and 255, null)
            return HSB(hsbVals[0], hsbVals[1], hsbVals[2])
        }
    }
    val asColor: Int get() = Color.HSBtoRGB(hue, saturation, brightness)
}