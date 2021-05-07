package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.Client
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.Quad
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.bake
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.block.vanilla.StandardDirtModel.Companion.SALTWATER_BIOMES
import mods.betterfoliage.render.lighting.LightingPreferredFace
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.get
import mods.betterfoliage.util.mapArray
import mods.betterfoliage.util.randomB
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation

object StandardSandDiscovery : AbstractModelDiscovery() {
    val SAND_BLOCKS = listOf(Blocks.SAND, Blocks.RED_SAND)
    override fun processModel(
        bakery: ModelBakery,
        state: BlockState,
        location: ResourceLocation,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    ): Boolean {
        val model = bakery.getUnbakedModel(location)
        if (model is BlockModel && state.block in SAND_BLOCKS) {
            Client.blockTypes.dirt.add(state)
            replacements[location] = StandardSandKey
            RenderTypeLookup.setRenderLayer(state.block, RenderType.getCutoutMipped())
            return true
        }
        return super.processModel(bakery, state, location, sprites, replacements)
    }
}

object StandardSandKey : HalfBakedWrapperKey() {
    override fun replace(wrapped: SpecialRenderModel) = StandardSandModel(wrapped)
}

class StandardSandModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    val coralLighting = Direction.values().mapArray { LightingPreferredFace(it) }

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        super.render(ctx, noDecorations)
        if (noDecorations || !Config.enabled || !Config.coral.enabled(ctx.random)) return
        if (ctx.biome?.category !in SALTWATER_BIOMES) return

        allDirections.filter { ctx.random.nextInt(64) < Config.coral.chance }.forEach { face ->
            val isWater = ctx.state(face).material == Material.WATER
            val isDeepWater = isWater && ctx.offset(face).state(UP).material == Material.WATER
            if (isDeepWater) {
                ctx.vertexLighter = coralLighting[face]
                ctx.render(coralCrustModels[face][ctx.random])
                ctx.render(coralTuftModels[face][ctx.random])
            }
        }
    }

    companion object {
        val coralTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_coral_$idx")
        }
        val coralCrustSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_crust_$idx")
        }
        val coralTuftModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.coral.let { tuftShapeSet(it.size, 1.0, 1.0, it.hOffset) }
            allDirections.mapArray { face ->
                tuftModelSet(shapes, Color.white) { coralTuftSprites[randomI()] }
                    .transform { rotate(Rotation.fromUp[face]) }
                    .buildTufts()
            }
        }
        val coralCrustModels by LazyInvalidatable(BakeWrapperManager) {
            allDirections.map { face ->
                Array(64) { idx ->
                    listOf(
                        Quad.horizontalRectangle(x1 = -0.5, x2 = 0.5, z1 = -0.5, z2 = 0.5, y = 0.0)
                            .scale(Config.coral.crustSize)
                            .move(0.5 + randomD(0.01, Config.coral.vOffset) to UP)
                            .rotate(Rotation.fromUp[face])
                            .mirrorUV(randomB(), randomB()).rotateUV(randomI(max = 4))
                            .sprite(coralCrustSprites[idx]).colorAndIndex(null)
                    ).bake(applyDiffuseLighting = false)
                }
            }.toTypedArray()
        }
    }
}
