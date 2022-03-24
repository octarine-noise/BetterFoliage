package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.CachedBlockCtx
import mods.betterfoliage.config.SALTWATER_BIOMES
import mods.betterfoliage.config.SAND_BLOCKS
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.ModelWrapKey
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.build
import mods.betterfoliage.model.horizontalRectangle
import mods.betterfoliage.model.meshifySolid
import mods.betterfoliage.model.meshifyStandard
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.model.withOpposites
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.get
import mods.betterfoliage.util.randomB
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.UP
import net.minecraft.world.BlockRenderView
import net.minecraft.world.biome.Biome
import java.util.Random
import java.util.function.Supplier

object StandardSandDiscovery : AbstractModelDiscovery() {


    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is JsonUnbakedModel && ctx.blockState.block in SAND_BLOCKS) {
            BetterFoliage.blockTypes.dirt.add(ctx.blockState)
            ctx.addReplacement(StandardSandKey)
//            RenderTypeLookup.setRenderLayer(ctx.blockState.block, RenderType.getCutoutMipped())
        }
        super.processModel(ctx)
    }
}

object StandardSandKey : ModelWrapKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel) = StandardSandModel(meshifySolid(wrapped))
}

class StandardSandModel(wrapped: BakedModel) : WrappedBakedModel(wrapped) {

    val coralLighting = allDirections.map { grassTuftLighting(it) }.toTypedArray()

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context)

        val ctx = CachedBlockCtx(blockView, pos)

        val random = randomSupplier.get()
        if (!BetterFoliage.config.enabled || !BetterFoliage.config.coral.enabled(random)) return
        if (ctx.biome?.let(Biome::getCategory) !in SALTWATER_BIOMES) return

        allDirections.filter { random.nextInt(64) < BetterFoliage.config.coral.chance }.forEach { face ->
            val isWater = ctx.state(face).material == Material.WATER
            val isDeepWater = isWater && ctx.offset(face).state(UP).material == Material.WATER
            if (isDeepWater) context.withLighting(coralLighting[face]) {
                it.accept(coralCrustModels[face][random])
                it.accept(coralTuftModels[face][random])
            }
        }
    }

    companion object {
//        val sandModel by LazyInvalidatable(BetterFoliage.modelReplacer) {
//            Array(64) { fullCubeTextured(Identifier("block/sand"), Color.white.asInt) }
//        }

        val coralTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_coral_$idx")
        }
        val coralCrustSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_crust_$idx")
        }
        val coralTuftModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = BetterFoliage.config.coral.let { tuftShapeSet(it.size, 1.0, 1.0, it.hOffset) }
            allDirections.map { face ->
                tuftModelSet(shapes, Color.white.asInt) { coralTuftSprites[randomI()] }
                    .transform { rotate(Rotation.fromUp[face]) }
                    .withOpposites()
                    .build(BlendMode.CUTOUT_MIPPED)
            }.toTypedArray()
        }
        val coralCrustModels by LazyInvalidatable(BakeWrapperManager) {
            allDirections.map { face ->
                Array(64) { idx ->
                    listOf(horizontalRectangle(x1 = -0.5, x2 = 0.5, z1 = -0.5, z2 = 0.5, y = 0.0)
                        .scale(BetterFoliage.config.coral.crustSize)
                        .move(0.5 + randomD(0.01, BetterFoliage.config.coral.vOffset) to UP)
                        .rotate(Rotation.fromUp[face])
                        .mirrorUV(randomB(), randomB()).rotateUV(randomI(max = 4))
                        .sprite(coralCrustSprites[idx]).colorAndIndex(null)
                    ).build(BlendMode.CUTOUT_MIPPED)
                }
            }.toTypedArray()
        }
    }
}
