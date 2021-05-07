package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.Color
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
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.randomI
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation

object StandardMyceliumDiscovery : AbstractModelDiscovery() {
    val MYCELIUM_BLOCKS = listOf(Blocks.MYCELIUM)

    override fun processModel(
        bakery: ModelBakery,
        state: BlockState,
        location: ResourceLocation,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    ): Boolean {
        val model = bakery.getUnbakedModel(location)
        if (model is BlockModel && state.block in MYCELIUM_BLOCKS) {
            replacements[location] = StandardMyceliumKey
            RenderTypeLookup.setRenderLayer(state.block, RenderType.getCutout())
            return true
        }
        return super.processModel(bakery, state, location, sprites, replacements)
    }
}

object StandardMyceliumKey : HalfBakedWrapperKey() {
    override fun replace(wrapped: SpecialRenderModel) = StandardMyceliumModel(wrapped)
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
            ctx.render(myceliumTuftModels[ctx.random])
        }
    }

    companion object {
        val myceliumTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_mycel_$idx")
        }
        val myceliumTuftModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.shortGrass.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white) { idx -> myceliumTuftSprites[randomI()] }.buildTufts()
        }
    }
}