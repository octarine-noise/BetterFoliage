package mods.betterfoliage.model

import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.directionsAndNull
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IModelTransform
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.Material
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.SimpleBakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormatElement
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder
import java.util.Random
import java.util.function.Function

/**
 * Hybrid baked quad implementation, carrying both baked and unbaked information.
 * Used to do advanced vertex lighting without unbaking vertex data at lighting time.
 */
data class HalfBakedQuad(
    val raw: Quad,
    val baked: BakedQuad
)

/**
 *
 */
open class HalfBakedSimpleModelWrapper(baseModel: SimpleBakedModel): IBakedModel by baseModel, SpecialRenderModel {
    val baseQuads = baseModel.unbakeQuads()

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        ctx.render(baseQuads)
    }
}

open class HalfBakedSpecialWrapper(val baseModel: SpecialRenderModel): IBakedModel by baseModel, SpecialRenderModel {
    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        baseModel.render(ctx, noDecorations)
    }
}

abstract class HalfBakedWrapperKey : ModelBakingKey, HasLogger() {
    override fun bake(
        location: ResourceLocation,
        unbaked: IUnbakedModel,
        transform: IModelTransform,
        bakery: ModelBakery,
        spriteGetter: Function<Material, TextureAtlasSprite>
    ): IBakedModel? {
        val baseModel = super.bake(location, unbaked, transform, bakery, spriteGetter)
        val halfBaked = when(baseModel) {
            is SimpleBakedModel -> HalfBakedSimpleModelWrapper(baseModel)
            else -> null
        }
        return if (halfBaked == null) baseModel else replace(halfBaked)
    }


    abstract fun replace(wrapped: SpecialRenderModel): SpecialRenderModel
}

fun List<Quad>.bake(applyDiffuseLighting: Boolean) = map { quad ->
    if (quad.sprite == null) throw IllegalStateException("Quad must have a texture assigned before baking")
    val builder = BakedQuadBuilder(quad.sprite)
    builder.setApplyDiffuseLighting(applyDiffuseLighting)
    builder.setQuadOrientation(quad.face())
    builder.setQuadTint(quad.colorIndex)
    quad.verts.forEach { vertex ->
        DefaultVertexFormats.BLOCK.elements.forEachIndexed { idx, element ->
            when {
                element.usage == VertexFormatElement.Usage.POSITION -> builder.put(idx,
                    (vertex.xyz.x + 0.5).toFloat(),
                    (vertex.xyz.y + 0.5).toFloat(),
                    (vertex.xyz.z + 0.5).toFloat(),
                    1.0f
                )
                // don't fill lightmap UV coords
                element.usage == VertexFormatElement.Usage.UV && element.type == VertexFormatElement.Type.FLOAT -> builder.put(idx,
                    quad.sprite.minU + (quad.sprite.maxU - quad.sprite.minU) * (vertex.uv.u + 0.5).toFloat(),
                    quad.sprite.minV + (quad.sprite.maxV - quad.sprite.minV) * (vertex.uv.v + 0.5).toFloat(),
                    0.0f, 1.0f
                )
                element.usage == VertexFormatElement.Usage.COLOR -> builder.put(idx,
                    (vertex.color.red and 255).toFloat() / 255.0f,
                    (vertex.color.green and 255).toFloat() / 255.0f,
                    (vertex.color.blue and 255).toFloat() / 255.0f,
                    (vertex.color.alpha and 255).toFloat() / 255.0f
                )
                element.usage == VertexFormatElement.Usage.NORMAL -> builder.put(idx,
                    (vertex.normal ?: quad.normal).x.toFloat(),
                    (vertex.normal ?: quad.normal).y.toFloat(),
                    (vertex.normal ?: quad.normal).z.toFloat(),
                    0.0f
                )
                else -> builder.put(idx)
            }
        }
    }
    HalfBakedQuad(quad, builder.build())
}

fun BakedQuad.unbake(): HalfBakedQuad {
    val size = DefaultVertexFormats.BLOCK.integerSize
    val verts = Array(4) { vIdx ->
        val x = java.lang.Float.intBitsToFloat(vertexData[vIdx * size + 0])
        val y = java.lang.Float.intBitsToFloat(vertexData[vIdx * size + 1])
        val z = java.lang.Float.intBitsToFloat(vertexData[vIdx * size + 2])
        val color = vertexData[vIdx * size + 3]
        val u = java.lang.Float.intBitsToFloat(vertexData[vIdx * size + 4])
        val v = java.lang.Float.intBitsToFloat(vertexData[vIdx * size + 5])
        Vertex(Double3(x, y, z), UV(u.toDouble(), v.toDouble()), Color(color))
    }
    val unbaked = Quad(
        verts[0], verts[1], verts[2], verts[3],
        colorIndex = if (hasTintIndex()) tintIndex else -1,
        face = face
    )
    return HalfBakedQuad(unbaked, this)
}

fun SimpleBakedModel.unbakeQuads() = directionsAndNull.flatMap { face ->
    getQuads(null, face, Random()).map { it.unbake() }
}

