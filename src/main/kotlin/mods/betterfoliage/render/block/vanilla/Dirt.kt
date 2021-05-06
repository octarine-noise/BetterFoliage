package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.Client
import mods.betterfoliage.config.Config
import mods.betterfoliage.render.ISpecialRenderModel
import mods.betterfoliage.render.old.HalfBakedSpecialWrapper
import mods.betterfoliage.render.old.HalfBakedWrapKey
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.ModelBakeKey
import mods.betterfoliage.resource.discovery.ModelReplacer
import mods.betterfoliage.util.offset
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation

object StandardDirtDiscovery : ModelReplacer() {
    val dirtBlocks = listOf(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL)
    override fun processModel(
        bakery: ModelBakery,
        state: BlockState,
        location: ResourceLocation,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakeKey>
    ): Boolean {
        val model = bakery.getUnbakedModel(location)
        if (model is BlockModel && state.block in dirtBlocks) {
            Client.blockTypes.dirt.add(state)
            replacements[location] = StandardDirtKey
            RenderTypeLookup.setRenderLayer(state.block, RenderType.getCutout())
            return true
        }
        return super.processModel(bakery, state, location, sprites, replacements)
    }
}

object StandardDirtKey : HalfBakedWrapKey() {
    override fun replace(wrapped: ISpecialRenderModel) = StandardDirtModel(wrapped)
}

class StandardDirtModel(
    wrapped: ISpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        if (!Config.enabled || noDecorations) return super.render(ctx, false)

        val stateUp = ctx.offset(UP).state
        val isConnectedGrass = Config.connectedGrass.enabled && stateUp in Client.blockTypes.grass
        if (isConnectedGrass) {
            (ctx.blockModelShapes.getModel(stateUp) as? ISpecialRenderModel)?.let { grassModel ->
                ctx.renderMasquerade(UP.offset) {
                    grassModel.render(ctx, true)
                }
                return
            }
            return super.render(ctx, false)
        }

        super.render(ctx, false)
    }
}