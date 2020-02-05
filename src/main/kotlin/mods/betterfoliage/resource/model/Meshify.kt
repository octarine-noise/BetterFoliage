package mods.betterfoliage.resource.model

import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.findFirst
import net.minecraft.block.BlockRenderLayer
import net.minecraft.block.BlockState
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormatElement
import net.minecraft.client.render.VertexFormatElement.Format.FLOAT
import net.minecraft.client.render.VertexFormatElement.Format.UBYTE
import net.minecraft.client.render.VertexFormatElement.Type.*
import net.minecraft.client.render.VertexFormatElement.Type.UV
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.util.math.Direction
import java.lang.Float
import java.util.*

interface BakedModelConverter {
    /**
     * Convert baked model. Returns null if conversion unsuccessful (wrong input type).
     * @param model Input model
     * @param converter Converter to use for converting nested models.
     */
    fun convert(model: BakedModel, converter: BakedModelConverter): BakedModel?
    companion object {
        fun of(func: (BakedModel, BakedModelConverter)->BakedModel?) = object : BakedModelConverter {
            override fun convert(model: BakedModel, converter: BakedModelConverter) = func(model, converter)
        }
        val identity = of { model, _ -> model }
    }
}

/**
 * Convert [BakedModel] using the provided list of [BakedModelConverter]s (in order).
 * If all converters fail, gives the original model back.
 */
fun List<BakedModelConverter>.convert(model: BakedModel) = object : BakedModelConverter {
    val converters = this@convert + BakedModelConverter.identity
    override fun convert(model: BakedModel, converter: BakedModelConverter) = converters.findFirst { it.convert(model, converter) }
}.let { converterStack ->
    // we are guaranteed a result here because of the identity converter
    converterStack.convert(model, converterStack)!!
}

/** List of converters without meaningful configuration that should always be used */
val COMMON_MESH_CONVERTERS = listOf(WrappedWeightedModel.converter)

/**
 * Convert [BakedModel] into one using fabric-rendering-api [Mesh] instead of the vanilla pipeline.
 * @param renderLayerOverride Use the given [BlockRenderLayer] for the [Mesh]
 * instead of the one declared by the corresponding [Block]
 */
fun meshifyStandard(model: BakedModel, state: BlockState, renderLayerOverride: BlockRenderLayer? = null) =
    (COMMON_MESH_CONVERTERS + WrappedMeshModel.converter(state, renderLayerOverride = renderLayerOverride)).convert(model)

/**
 * Convert a vanilla [BakedModel] into intermediate [Quad]s
 * Vertex normals not supported (yet)
 * Vertex data elements not aligned to 32 bit boundaries not supported
 */
fun unbakeQuads(model: BakedModel, state: BlockState, random: Random, unshade: Boolean): List<Quad> {
    return (allDirections.toList() + null as Direction?).flatMap { face ->
        model.getQuads(state, face, random).mapIndexed { qIdx, bakedQuad ->
            var quad = Quad(Vertex(), Vertex(), Vertex(), Vertex(), face = face, colorIndex = bakedQuad.colorIndex, sprite = bakedQuad.sprite)

            val format = quadVertexFormat(bakedQuad)
            val stride = format.vertexSizeInteger
            format.getIntOffset(POSITION, FLOAT, 3)?.let { posOffset ->
                quad = quad.transformVI { vertex, vIdx -> vertex.copy(xyz = Double3(
                    x = Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + posOffset + 0]).toDouble(),
                    y = Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + posOffset + 1]).toDouble(),
                    z = Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + posOffset + 2]).toDouble()
                )) }
            }
            format.getIntOffset(COLOR, UBYTE, 4)?.let { colorOffset ->
                quad = quad.transformVI { vertex, vIdx -> vertex.copy(
                    color = Color(bakedQuad.vertexData[vIdx * stride + colorOffset])
                ) }
            }
            format.getIntOffset(UV, FLOAT, 2, 0)?.let { uvOffset ->
                quad = quad.transformVI { vertex, vIdx -> vertex.copy(uv = UV(
                    u = Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + uvOffset + 0]).toDouble(),
                    v = Float.intBitsToFloat(bakedQuad.vertexData[vIdx * stride + uvOffset + 1]).toDouble()
                )) }
            }

            quad = quad.transformV { it.copy(uv = it.uv.unbake(quad.sprite!!)) }.move(Double3(-0.5, -0.5, -0.5))
            if (unshade) quad = quad.transformV { it.copy(color = it.color * (1.0f / Color.bakeShade(quad.face))) }
            quad
        }
    }
}

/** Get the byte offset of the [VertexFormatElement] matching the given criteria */
fun VertexFormat.getByteOffset(type: VertexFormatElement.Type, format: VertexFormatElement.Format, count: Int, index: Int = 0): Int? {
    elements.forEachIndexed { idx, element ->
        if (element.type == type && element.format == format && element.count == count && element.index == index)
            return getElementOffset(idx)
    }
    return null
}

/**
 * Get the int (32 bit) offset of the [VertexFormatElement] matching the given criteria
 * Returns null if the element is not properly aligned
 */
fun VertexFormat.getIntOffset(type: VertexFormatElement.Type, format: VertexFormatElement.Format, count: Int, index: Int = 0) =
    getByteOffset(type, format, count, index)?.let { if (it % 4 == 0) it / 4 else null }

/** Function to determine [VertexFormat] used by [BakedQuad] */
var quadVertexFormat: (BakedQuad)->VertexFormat = { VertexFormats.POSITION_COLOR_UV_LMAP }