package mods.octarinecore.client.render

import mods.octarinecore.common.*
import net.minecraft.util.EnumFacing


const val defaultCornerDimming = 0.5f
const val defaultEdgeDimming = 0.8f

// ================================
// Shader instantiation lambdas
// ================================
fun cornerAo(fallbackAxis: EnumFacing.Axis): (EnumFacing, EnumFacing, EnumFacing)->Shader = { face, dir1, dir2 ->
    val fallbackDir = listOf(face, dir1, dir2).find { it.axis == fallbackAxis }!!
    CornerSingleFallback(face, dir1, dir2, fallbackDir)
}
val cornerFlat = { face: EnumFacing, dir1: EnumFacing, dir2: EnumFacing -> FaceFlat(face) }
fun cornerAoTri(func: (AoData, AoData)-> AoData) = { face: EnumFacing, dir1: EnumFacing, dir2: EnumFacing ->
    CornerTri(face, dir1, dir2, func)
}
val cornerAoMaxGreen = cornerAoTri { s1, s2 -> if (s1.green > s2.green) s1 else s2 }

fun cornerInterpolate(edgeAxis: EnumFacing.Axis, weight: Float, dimming: Float): (EnumFacing, EnumFacing, EnumFacing)->Shader = { dir1, dir2, dir3 ->
    val edgeDir = listOf(dir1, dir2, dir3).find { it.axis == edgeAxis }!!
    val faceDirs = listOf(dir1, dir2, dir3).filter { it.axis != edgeAxis }
    CornerInterpolateDimming(faceDirs[0], faceDirs[1], edgeDir, weight, dimming)
}

// ================================
// Shaders
// ================================
object NoShader : Shader {
    override fun shade(context: ShadingContext, vertex: RenderVertex) = vertex.shade(AoData.black)
    override fun rotate(rot: Rotation) = this
}

class CornerSingleFallback(val face: EnumFacing, val dir1: EnumFacing, val dir2: EnumFacing, val fallbackDir: EnumFacing, val fallbackDimming: Float = defaultCornerDimming) : Shader {
    val offset = Int3(fallbackDir)
    override fun shade(context: ShadingContext, vertex: RenderVertex) {
        val shading = context.aoShading(face, dir1, dir2)
        if (shading.valid)
            vertex.shade(shading)
        else context.blockData(offset).let {
            vertex.shade(it.brightness brMul fallbackDimming, it.color colorMul fallbackDimming)
        }
    }
    override fun rotate(rot: Rotation) = CornerSingleFallback(face.rotate(rot), dir1.rotate(rot), dir2.rotate(rot), fallbackDir.rotate(rot), fallbackDimming)
}

inline fun accumulate(v1: AoData?, v2: AoData?, func: ((AoData, AoData)-> AoData)): AoData? {
    val v1ok = v1 != null && v1.valid
    val v2ok = v2 != null && v2.valid
    if (v1ok && v2ok) return func(v1!!, v2!!)
    if (v1ok) return v1
    if (v2ok) return v2
    return null
}

class CornerTri(val face: EnumFacing, val dir1: EnumFacing, val dir2: EnumFacing,
                val func: ((AoData, AoData)-> AoData)) : Shader {
    override fun shade(context: ShadingContext, vertex: RenderVertex) {
        var acc = accumulate(
            context.aoShading(face, dir1, dir2),
            context.aoShading(dir1, face, dir2),
            func)
        acc = accumulate(
            acc,
            context.aoShading(dir2, face, dir1),
            func)
        vertex.shade(acc ?: AoData.black)
    }
    override fun rotate(rot: Rotation) = CornerTri(face.rotate(rot), dir1.rotate(rot), dir2.rotate(rot), func)
}

class EdgeInterpolateFallback(val face: EnumFacing, val edgeDir: EnumFacing, val pos: Double, val fallbackDimming: Float = defaultEdgeDimming): Shader {
    val offset = Int3(edgeDir)
    val edgeAxis = axes.find { it != face.axis && it != edgeDir.axis }!!
    val weightN = (0.5 - pos).toFloat()
    val weightP = (0.5 + pos).toFloat()

    override fun shade(context: ShadingContext, vertex: RenderVertex) {
        val shadingP = context.aoShading(face, edgeDir, (edgeAxis to EnumFacing.AxisDirection.POSITIVE).face)
        val shadingN = context.aoShading(face, edgeDir, (edgeAxis to EnumFacing.AxisDirection.NEGATIVE).face)
        if (!shadingP.valid && !shadingN.valid) context.blockData(offset).let {
            return vertex.shade(it.brightness brMul fallbackDimming, it.color colorMul fallbackDimming)
        }
        if (!shadingP.valid) return vertex.shade(shadingN)
        if (!shadingN.valid) return vertex.shade(shadingP)
        vertex.shade(shadingP, shadingN, weightP, weightN)
    }
    override fun rotate(rot: Rotation) = EdgeInterpolateFallback(face.rotate(rot), edgeDir.rotate(rot), pos)
}

class CornerInterpolateDimming(val face1: EnumFacing, val face2: EnumFacing, val edgeDir: EnumFacing,
                               val weight: Float, val dimming: Float, val fallbackDimming: Float = defaultCornerDimming) : Shader {
    val offset = Int3(edgeDir)
    override fun shade(context: ShadingContext, vertex: RenderVertex) {
        var shading1 = context.aoShading(face1, edgeDir, face2)
        var shading2 = context.aoShading(face2, edgeDir, face1)
        var weight1 = weight
        var weight2 = 1.0f - weight
        if (!shading1.valid && !shading2.valid) context.blockData(offset).let {
            return vertex.shade(it.brightness brMul fallbackDimming, it.color colorMul fallbackDimming)
        }
        if (!shading1.valid) { shading1 = shading2; weight1 *= dimming }
        if (!shading2.valid) { shading2 = shading1; weight2 *= dimming }
        vertex.shade(shading1, shading2, weight1, weight2)
    }

    override fun rotate(rot: Rotation) =
        CornerInterpolateDimming(face1.rotate(rot), face2.rotate(rot), edgeDir.rotate(rot), weight, dimming, fallbackDimming)
}

class FaceCenter(val face: EnumFacing): Shader {
    override fun shade(context: ShadingContext, vertex: RenderVertex) {
        vertex.red = 0.0f; vertex.green = 0.0f; vertex.blue = 0.0f;
        val b = IntArray(4)
        faceCorners[face.ordinal].asList.forEachIndexed { idx, corner ->
            val shading = context.aoShading(face, corner.first, corner.second)
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

class FaceFlat(val face: EnumFacing): Shader {
    override fun shade(context: ShadingContext, vertex: RenderVertex) {
        val color = context.blockData(Int3.zero).color
        vertex.shade(context.blockData(face.offset).brightness, color)
    }
    override fun rotate(rot: Rotation): Shader = FaceFlat(face.rotate(rot))
}

class FlatOffset(val offset: Int3): Shader {
    override fun shade(context: ShadingContext, vertex: RenderVertex)  {
        context.blockData(offset).let {
            vertex.brightness = it.brightness
            vertex.setColor(it.color)
        }
    }
    override fun rotate(rot: Rotation): Shader = this
}

class FlatOffsetNoColor(val offset: Int3): Shader {
    override fun shade(context: ShadingContext, vertex: RenderVertex)  {
        vertex.brightness = context.blockData(offset).brightness
        vertex.red = 1.0f; vertex.green = 1.0f; vertex.blue = 1.0f
    }
    override fun rotate(rot: Rotation): Shader = this
}