package mods.betterfoliage.model

import mods.betterfoliage.BakedQuad_sprite
import mods.betterfoliage.VertexFormat_offsets
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.findFirst
import mods.betterfoliage.util.get
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.minecraft.block.BlockState
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormatElement
import net.minecraft.client.render.VertexFormatElement.Type.*
import net.minecraft.client.render.VertexFormatElement.Type.UV
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.util.math.Direction
import java.util.*

interface BakedModelConverter {
    /**
     * Convert baked model. Returns null if conversion unsuccessful (wrong input type).
     * @param model Input model
     * @param converter Converter to use for converting nested models.
     */
    fun convert(model: BakedModel): BakedModel?
    companion object {
        fun of(func: (BakedModel)->BakedModel?) = object : BakedModelConverter {
            override fun convert(model: BakedModel) = func(model)
        }
        val identity = of { model -> model }
    }
}

/**
 * Convert [BakedModel] using the provided list of [BakedModelConverter]s (in order).
 * If all converters fail, gives the original model back.
 */
fun List<BakedModelConverter>.convert(model: BakedModel) = object : BakedModelConverter {
    val converters = this@convert + BakedModelConverter.identity
    override fun convert(model: BakedModel) = converters.findFirst { it.convert(model) }
}.let { converterStack ->
    // we are guaranteed a result here because of the identity converter
    converterStack.convert(model)!!
}

/**
 * Convert [BasicBakedModel] into one using fabric-rendering-api [Mesh] instead of the vanilla pipeline.
 * @param blendMode Use the given [BlockRenderLayer] for the [Mesh]
 * instead of the one declared by the corresponding [Block]
 */
fun meshifyStandard(model: BasicBakedModel, state: BlockState? = null, blendMode: BlendMode? = null) =
    WrappedMeshModel.converter(state, blendModeOverride = blendMode).convert(model)!!

fun meshifySolid(model: BasicBakedModel) = meshifyStandard(model, null, BlendMode.SOLID)
fun meshifyCutoutMipped(model: BasicBakedModel) = meshifyStandard(model, null, BlendMode.CUTOUT_MIPPED)

/**
 * Convert a vanilla [BakedModel] into intermediate [Quad]s
 * Vertex normals not supported (yet)
 * Vertex data elements not aligned to 32 bit boundaries not supported
 */
fun unbakeQuads(model: BakedModel, state: BlockState?, random: Random, unshade: Boolean): List<Quad> {
    return (allDirections.toList() + null as Direction?).flatMap { face ->
        model.getQuads(state, face, random).mapIndexed { qIdx, bakedQuad ->
            var quad = Quad(Vertex(), Vertex(), Vertex(), Vertex(), face = face, colorIndex = bakedQuad.colorIndex, sprite = bakedQuad[BakedQuad_sprite])

            val format = quadVertexFormat(bakedQuad)
            val stride = format.vertexSizeInteger
            format.getIntOffset(POSITION, VertexFormatElement.DataType.FLOAT, 3)?.let { posOffset ->
                quad = quad.transformVI { vertex, vIdx -> vertex.copy(xyz = Double3(
                    x = java.lang.Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + posOffset + 0]).toDouble(),
                    y = java.lang.Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + posOffset + 1]).toDouble(),
                    z = java.lang.Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + posOffset + 2]).toDouble()
                )) }
            }
            format.getIntOffset(COLOR, VertexFormatElement.DataType.UBYTE, 4)?.let { colorOffset ->
                quad = quad.transformVI { vertex, vIdx -> vertex.copy(
                    color = Color(bakedQuad.vertexData[vIdx * stride + colorOffset])
                ) }
            }
            format.getIntOffset(UV, VertexFormatElement.DataType.FLOAT, 2, 0)?.let { uvOffset ->
                quad = quad.transformVI { vertex, vIdx -> vertex.copy(uv = UV(
                    u = java.lang.Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + uvOffset + 0]).toDouble(),
                    v = java.lang.Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + uvOffset + 1]).toDouble()
                )) }
            }

            quad = quad.transformV { it.copy(uv = it.uv.unbake(quad.sprite!!)) }.move(Double3(-0.5, -0.5, -0.5))
            if (unshade) quad = quad.transformV { it.copy(color = it.color * (1.0f / Color.bakeShade(quad.face))) }
            quad
        }
    }
}

/** Get the byte offset of the [VertexFormatElement] matching the given criteria */
fun VertexFormat.getByteOffset(type: VertexFormatElement.Type, format: VertexFormatElement.DataType, count: Int, index: Int = 0): Int? {
    elements.forEachIndexed { idx, element ->
        if (element == VertexFormatElement(index, format, type, count))
            return VertexFormat_offsets[this]!!.getInt(idx)
    }
    return null
}

/**
 * Get the int (32 bit) offset of the [VertexFormatElement] matching the given criteria
 * Returns null if the element is not properly aligned
 */
fun VertexFormat.getIntOffset(type: VertexFormatElement.Type, format: VertexFormatElement.DataType, count: Int, index: Int = 0) =
    getByteOffset(type, format, count, index)?.let { if (it % 4 == 0) it / 4 else null }

/** Function to determine [VertexFormat] used by [BakedQuad] */
var quadVertexFormat: (BakedQuad)->VertexFormat = { VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL }
