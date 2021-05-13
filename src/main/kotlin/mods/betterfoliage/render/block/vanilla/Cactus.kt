package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.CACTUS_BLOCKS
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.ModelWrapKey
import mods.betterfoliage.model.SpriteDelegate
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.crossModelsRaw
import mods.betterfoliage.model.crossModelsTextured
import mods.betterfoliage.model.meshifyCutoutMipped
import mods.betterfoliage.model.meshifyStandard
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.roundLeafLighting
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.get
import mods.betterfoliage.util.horizontalDirections
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.world.BlockRenderView
import java.util.Random
import java.util.function.Supplier


object StandardCactusDiscovery : AbstractModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext) {
        val model = ctx.getUnbaked()
        if (model is JsonUnbakedModel && ctx.blockState.block in CACTUS_BLOCKS) {
            BetterFoliage.blockTypes.dirt.add(ctx.blockState)
            ctx.addReplacement(StandardCactusKey)
            ctx.sprites.add(StandardCactusModel.cactusCrossSprite)
        }
        super.processModel(ctx)
    }
}

object StandardCactusKey : ModelWrapKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel) = StandardCactusModel(meshifyCutoutMipped(wrapped))
}

class StandardCactusModel(wrapped: BakedModel) : WrappedBakedModel(wrapped), FabricBakedModel {

    val armLighting = horizontalDirections.map { grassTuftLighting(it) }
    val crossLighting = roundLeafLighting()

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        (wrapped as FabricBakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context)
        if (!BetterFoliage.config.enabled || !BetterFoliage.config.cactus.enabled) return

        val random = randomSupplier.get()
        val armSide = random.nextInt() and 3

        context.withLighting(armLighting[armSide]) {
            it.accept(cactusArmModels[armSide][random])
        }
        context.withLighting(crossLighting) {
            it.accept(cactusCrossModels[random])
        }
    }

    companion object {
        val cactusCrossSprite = Identifier(BetterFoliage.MOD_ID, "blocks/better_cactus")
        val cactusArmSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_cactus_arm_$idx")
        }
        val cactusArmModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = BetterFoliage.config.cactus.let { tuftShapeSet(0.8, 0.8, 0.8, 0.2) }
            val models = tuftModelSet(shapes, Color.white.asInt) { cactusArmSprites[randomI()] }
            horizontalDirections.map { side ->
                models.transform { move(0.0625 to DOWN).rotate(Rotation.fromUp[side.ordinal]) }.buildTufts()
            }.toTypedArray()
        }
        val cactusCrossModels by LazyInvalidatable(BakeWrapperManager) {
            val models = BetterFoliage.config.cactus.let { config ->
                crossModelsRaw(64, config.size, 0.0, 0.0)
                    .transform { rotateZ(randomD(-config.sizeVariation, config.sizeVariation)) }
            }
            crossModelsTextured(models, Color.white.asInt, true) { cactusCrossSprite }
        }
    }
}
