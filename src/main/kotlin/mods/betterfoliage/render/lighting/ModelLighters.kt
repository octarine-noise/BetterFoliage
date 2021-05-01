package mods.betterfoliage.render.lighting

import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.axes
import mods.betterfoliage.util.boxFaces
import mods.betterfoliage.util.face
import mods.betterfoliage.util.get
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.rotate
import net.minecraft.util.Direction


const val defaultCornerDimming = 0.5f
const val defaultEdgeDimming = 0.8f

// ================================
// Shader instantiation lambdas
// ================================
fun cornerAo(fallbackAxis: Direction.Axis): CornerShaderFactory = { face, dir1, dir2 ->
    val fallbackDir = listOf(face, dir1, dir2).find { it.axis == fallbackAxis }!!
    CornerSingleFallback(face, dir1, dir2, fallbackDir)
}
val cornerFlat = { face: Direction, dir1: Direction, dir2: Direction -> FaceFlat(face) }
fun cornerAoTri(func: (CornerLightData, CornerLightData)-> CornerLightData) = { face: Direction, dir1: Direction, dir2: Direction ->
    CornerTri(face, dir1, dir2, func)
}
val cornerAoMaxGreen = cornerAoTri { s1, s2 -> if (s1.green > s2.green) s1 else s2 }

fun cornerInterpolate(edgeAxis: Direction.Axis, weight: Float, dimming: Float): CornerShaderFactory = { dir1, dir2, dir3 ->
    val edgeDir = listOf(dir1, dir2, dir3).find { it.axis == edgeAxis }!!
    val faceDirs = listOf(dir1, dir2, dir3).filter { it.axis != edgeAxis }
    CornerInterpolateDimming(faceDirs[0], faceDirs[1], edgeDir, weight, dimming)
}

// ================================
// Shaders
// ================================
object NoLighting : ModelLighter {
    override fun shade(context: LightingCtx, vertex: RenderVertex) = vertex.shade(CornerLightData.black)
    override fun rotate(rot: Rotation) = this
}

class CornerSingleFallback(val face: Direction, val dir1: Direction, val dir2: Direction, val fallbackDir: Direction, val fallbackDimming: Float = defaultCornerDimming) :
    ModelLighter {
    val offset = Int3(fallbackDir)
    override fun shade(context: LightingCtx, vertex: RenderVertex) {
        val shading = context.lighting(face, dir1, dir2)
        if (shading.valid)
            vertex.shade(shading)
        else {
            vertex.shade(context.brightness(offset) brMul fallbackDimming, context.color(offset) colorMul fallbackDimming)
        }
    }
    override fun rotate(rot: Rotation) = CornerSingleFallback(face.rotate(rot), dir1.rotate(rot), dir2.rotate(rot), fallbackDir.rotate(rot), fallbackDimming)
}

inline fun accumulate(v1: CornerLightData?, v2: CornerLightData?, func: ((CornerLightData, CornerLightData)-> CornerLightData)): CornerLightData? {
    val v1ok = v1 != null && v1.valid
    val v2ok = v2 != null && v2.valid
    if (v1ok && v2ok) return func(v1!!, v2!!)
    if (v1ok) return v1
    if (v2ok) return v2
    return null
}

class CornerTri(val face: Direction, val dir1: Direction, val dir2: Direction,
                val func: ((CornerLightData, CornerLightData)-> CornerLightData)) : ModelLighter {
    override fun shade(context: LightingCtx, vertex: RenderVertex) {
        var acc = accumulate(
            context.lighting(face, dir1, dir2),
            context.lighting(dir1, face, dir2),
            func)
        acc = accumulate(
            acc,
            context.lighting(dir2, face, dir1),
            func)
        vertex.shade(acc ?: CornerLightData.black)
    }
    override fun rotate(rot: Rotation) = CornerTri(face.rotate(rot), dir1.rotate(rot), dir2.rotate(rot), func)
}

class EdgeInterpolateFallback(val face: Direction, val edgeDir: Direction, val pos: Double, val fallbackDimming: Float = defaultEdgeDimming):
    ModelLighter {
    val offset = Int3(edgeDir)
    val edgeAxis = axes.find { it != face.axis && it != edgeDir.axis }!!
    val weightN = (0.5 - pos).toFloat()
    val weightP = (0.5 + pos).toFloat()

    override fun shade(context: LightingCtx, vertex: RenderVertex) {
        val shadingP = context.lighting(face, edgeDir, (edgeAxis to Direction.AxisDirection.POSITIVE).face)
        val shadingN = context.lighting(face, edgeDir, (edgeAxis to Direction.AxisDirection.NEGATIVE).face)
        if (!shadingP.valid && !shadingN.valid)  {
            return vertex.shade(context.brightness(offset) brMul fallbackDimming, context.color(offset) colorMul fallbackDimming)
        }
        if (!shadingP.valid) return vertex.shade(shadingN)
        if (!shadingN.valid) return vertex.shade(shadingP)
        vertex.shade(shadingP, shadingN, weightP, weightN)
    }
    override fun rotate(rot: Rotation) = EdgeInterpolateFallback(face.rotate(rot), edgeDir.rotate(rot), pos)
}

class CornerInterpolateDimming(val face1: Direction, val face2: Direction, val edgeDir: Direction,
                               val weight: Float, val dimming: Float, val fallbackDimming: Float = defaultCornerDimming
) : ModelLighter {
    val offset = Int3(edgeDir)
    override fun shade(context: LightingCtx, vertex: RenderVertex) {
        var shading1 = context.lighting(face1, edgeDir, face2)
        var shading2 = context.lighting(face2, edgeDir, face1)
        var weight1 = weight
        var weight2 = 1.0f - weight
        if (!shading1.valid && !shading2.valid) {
            return vertex.shade(context.brightness(offset) brMul fallbackDimming, context.color(offset) colorMul fallbackDimming)
        }
        if (!shading1.valid) { shading1 = shading2; weight1 *= dimming }
        if (!shading2.valid) { shading2 = shading1; weight2 *= dimming }
        vertex.shade(shading1, shading2, weight1, weight2)
    }

    override fun rotate(rot: Rotation) =
        CornerInterpolateDimming(face1.rotate(rot), face2.rotate(rot), edgeDir.rotate(rot), weight, dimming, fallbackDimming)
}

class FaceCenter(val face: Direction): ModelLighter {
    override fun shade(context: LightingCtx, vertex: RenderVertex) {
        vertex.red = 0.0f; vertex.green = 0.0f; vertex.blue = 0.0f;
        val b = IntArray(4)
        boxFaces[face].allCorners.forEachIndexed { idx, corner ->
            val shading = context.lighting(face, corner.first, corner.second)
            vertex.red += shading.red
            vertex.green += shading.green
            vertex.blue += shading.blue
            b[idx] = shading.brightness
        }
        vertex.apply { red *= 0.25f; green *= 0.25f; blue *= 0.25f }
        vertex.brightness = brSum(0.25f, *b)
    }
    override fun rotate(rot: Rotation) = FaceCenter(face.rotate(rot))
}

class FaceFlat(val face: Direction): ModelLighter {
    override fun shade(context: LightingCtx, vertex: RenderVertex) {
        vertex.shade(context.brightness(face.offset), context.color(Int3.zero))
    }
    override fun rotate(rot: Rotation): ModelLighter = FaceFlat(face.rotate(rot))
}

class FlatOffset(val offset: Int3): ModelLighter {
    override fun shade(context: LightingCtx, vertex: RenderVertex)  {
        vertex.brightness = context.brightness(offset)
        vertex.setColor(context.color(offset))
    }
    override fun rotate(rot: Rotation): ModelLighter = this
}

class FlatOffsetNoColor(val offset: Int3): ModelLighter {
    override fun shade(context: LightingCtx, vertex: RenderVertex)  {
        vertex.brightness = context.brightness(offset)
        vertex.red = 1.0f; vertex.green = 1.0f; vertex.blue = 1.0f
    }
    override fun rotate(rot: Rotation): ModelLighter = this
}