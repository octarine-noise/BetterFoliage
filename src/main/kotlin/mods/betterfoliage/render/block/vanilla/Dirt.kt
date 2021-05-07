package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.Client
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
import mods.betterfoliage.render.pipeline.RenderCtxVanilla
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.resource.generated.CenteredSprite
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.randomI
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.Biome

object StandardDirtDiscovery : AbstractModelDiscovery() {
    val DIRT_BLOCKS = listOf(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL)
    override fun processModel(
        bakery: ModelBakery,
        state: BlockState,
        location: ResourceLocation,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    ): Boolean {
        val model = bakery.getUnbakedModel(location)
        if (model is BlockModel && state.block in DIRT_BLOCKS) {
            Client.blockTypes.dirt.add(state)
            replacements[location] = StandardDirtKey
//            RenderTypeLookup.setRenderLayer(state.block, RenderType.getCutout())
            RenderTypeLookup.setRenderLayer(state.block, RenderType.getCutoutMipped())
            return true
        }
        return super.processModel(bakery, state, location, sprites, replacements)
    }
}

object StandardDirtKey : HalfBakedWrapperKey() {
    override fun replace(wrapped: SpecialRenderModel) = StandardDirtModel(wrapped)
}

class StandardDirtModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    val vanillaTuftLighting = LightingPreferredFace(UP)

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        if (!Config.enabled || noDecorations) return super.render(ctx, noDecorations)

        val stateUp = ctx.offset(UP).state
        val isConnectedGrass = Config.connectedGrass.enabled && stateUp in Client.blockTypes.grass
        if (isConnectedGrass) {
            (ctx.blockModelShapes.getModel(stateUp) as? SpecialRenderModel)?.let { grassModel ->
                ctx.renderMasquerade(UP.offset) {
                    grassModel.render(ctx, true)
                }
                return
            }
            return super.render(ctx, false)
        }

        super.render(ctx, false)

        val isWater = stateUp.material == Material.WATER
        val isDeepWater = isWater && ctx.offset(Int3(2 to UP)).state.material == Material.WATER
        val isShallowWater = isWater && ctx.offset(Int3(2 to UP)).state.isAir
        val isSaltWater = isWater && ctx.biome?.category in SALTWATER_BIOMES

        if (Config.algae.enabled(ctx.random) && isDeepWater) {
            (ctx as? RenderCtxVanilla)?.vertexLighter = vanillaTuftLighting
            ctx.render(algaeModels[ctx.random])
        } else if (Config.reed.enabled(ctx.random) && isShallowWater && !isSaltWater) {
            (ctx as? RenderCtxVanilla)?.vertexLighter = vanillaTuftLighting
            ctx.render(reedModels[ctx.random])
        }
    }

    companion object {
        val SALTWATER_BIOMES = listOf(Biome.Category.BEACH, Biome.Category.OCEAN)

        val algaeSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_algae_$idx")
        }
        val reedSprites by SpriteSetDelegate(
            Atlas.BLOCKS,
            idFunc = { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_reed_$idx") },
            idRegister = { id -> CenteredSprite(id, aspectHeight = 2).register(Client.generatedPack) }
        )
        val algaeModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.algae.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white) { algaeSprites[randomI()] }.buildTufts()
        }
        val reedModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.reed.let { tuftShapeSet(2.0, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white) { reedSprites[randomI()] }.buildTufts()
        }
    }
}