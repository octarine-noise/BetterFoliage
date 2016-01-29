@file:JvmName("PixelFormat")
package mods.octarinecore.client.render

import java.awt.Color

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