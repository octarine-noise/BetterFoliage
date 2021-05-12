package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.lighting.LightingPreferredFace
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.randomI
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation

object StandardMyceliumDiscovery : AbstractModelDiscovery() {
    val MYCELIUM_BLOCKS = listOf(Blocks.MYCELIUM)

    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is BlockModel && ctx.blockState.block in MYCELIUM_BLOCKS) {
            ctx.addReplacement(StandardMyceliumKey)
            RenderTypeLookup.setRenderLayer(ctx.blockState.block, RenderType.getCutout())
        }
        super.processModel(ctx)
    }
}

object StandardMyceliumKey : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardMyceliumModel(wrapped)
}

class StandardMyceliumModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {

    val tuftLighting = LightingPreferredFace(Direction.UP)

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        super.render(ctx, noDecorations)

        if (Config.shortGrass.enabled &&
            Config.shortGrass.myceliumEnabled &&
            Config.shortGrass.enabled(ctx.random) &&
            ctx.state(Direction.UP).isAir(ctx.world, ctx.pos)
        ) {
            ctx.vertexLighter = tuftLighting
            ctx.renderQuads(myceliumTuftModels[ctx.random])
        }
    }

    companion object {
        val myceliumTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_mycel_$idx")
        }
        val myceliumTuftModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.shortGrass.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, -1) { idx -> myceliumTuftSprites[randomI()] }.buildTufts()
        }
    }
}