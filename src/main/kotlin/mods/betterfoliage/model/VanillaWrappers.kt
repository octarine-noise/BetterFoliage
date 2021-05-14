package mods.betterfoliage.model

import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.util.HasLogger
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.WeightedPicker
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

abstract class ModelWrapKey : ModelBakingKey, HasLogger() {
    override fun bake(ctx: ModelBakingContext): BakedModel? {
        val baseModel = super.bake(ctx)
        if (baseModel is BasicBakedModel)
            return bake(ctx, baseModel)
        else
            return baseModel
    }

    abstract fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel): BakedModel
}

abstract class WrappedBakedModel(val wrapped: BakedModel) : BakedModel by wrapped, FabricBakedModel {
    override fun isVanillaAdapter() = false

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
        (wrapped as FabricBakedModel).emitItemQuads(stack, randomSupplier, context)
    }

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        (wrapped as FabricBakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context)
    }
}

class WrappedMeshModel(wrapped: BasicBakedModel, val mesh: Mesh) : WrappedBakedModel(wrapped) {
    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        context.meshConsumer().accept(mesh)
    }

    companion object {
        /**
         * Converter for [BasicBakedModel] instances.
         * @param state [BlockState] to use when querying [BakedModel]
         * @param unshade undo vanilla diffuse lighting when unbaking the [BakedModel]
         * @param noDiffuse disable diffuse lighting when baking the [Mesh]
         * @param blendModeOverride [BlockRenderLayer] to use instead of the one declared by the corresponding [Block]
         */
        fun converter(state: BlockState?, unshade: Boolean = false, noDiffuse: Boolean = true, blendModeOverride: BlendMode? = null) = BakedModelConverter.of { model ->
            if (model is BasicBakedModel) {
                val mesh = unbakeQuads(model, state, Random(42L), unshade).build(
                    blendMode = blendModeOverride ?: BlendMode.fromRenderLayer(RenderLayers.getBlockLayer(state)),
                    noDiffuse = noDiffuse,
                    flatLighting = !model.useAmbientOcclusion()
                )
                WrappedMeshModel(model, mesh)
            } else null
        }
    }
}

class WeightedModelWrapper(
    val models: List<WeightedModel>, baseModel: BakedModel
): WrappedBakedModel(baseModel), FabricBakedModel {

    class WeightedModel(val model: BakedModel, val weight: Int) : WeightedPicker.Entry(weight)
    fun getModel(random: Random) = WeightedPicker.getRandom(random, models).model

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        (getModel(randomSupplier.get()) as FabricBakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context)
    }
}

fun getUnderlyingModel(model: BakedModel, random: Random): BakedModel = when(model) {
    is WeightedModelWrapper -> getUnderlyingModel(model.getModel(random), random)
    is WrappedBakedModel -> model.wrapped
    else -> model
}