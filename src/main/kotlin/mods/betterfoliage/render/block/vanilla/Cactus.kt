package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.roundLeafLighting
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.resource.discovery.BlockRenderKey
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelTextureList
import mods.betterfoliage.resource.discovery.SimpleBlockMatcher
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.SpriteDelegate
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.crossModelsRaw
import mods.betterfoliage.model.crossModelsTextured
import mods.betterfoliage.model.meshifyStandard
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyMap
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.get
import mods.betterfoliage.util.horizontalDirections
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.CactusBlock
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.world.BlockRenderView
import java.util.Random
import java.util.function.Consumer
import java.util.function.Supplier


interface CactusKey : BlockRenderKey {
    val cactusTop: Identifier
    val cactusBottom: Identifier
    val cactusSide: Identifier
}

object StandardCactusDiscovery : ConfigurableModelDiscovery() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses = SimpleBlockMatcher(CactusBlock::class.java)
    override val modelTextures = listOf(ModelTextureList("block/cactus", "top", "bottom", "side"))

    override fun processModel(state: BlockState, textures: List<Identifier>, atlas: Consumer<Identifier>): BlockRenderKey? {
        return CactusModel.Key(textures[0], textures[1], textures[2])
    }
}

class CactusModel(val key: Key, wrapped: BakedModel) : WrappedBakedModel(wrapped), FabricBakedModel {

    val crossModels by cactusCrossModels.delegate(key)
    val armModels by cactusArmModels.delegate(key)
    val armLighting = horizontalDirections.map { grassTuftLighting(it) }
    val crossLighting = roundLeafLighting()

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        (wrapped as FabricBakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context)
        if (!BetterFoliage.config.enabled || !BetterFoliage.config.cactus.enabled) return

        val random = randomSupplier.get()
        val armSide = random.nextInt() and 3

        context.withLighting(armLighting[armSide]) {
            it.accept(armModels[armSide][random])
        }
        context.withLighting(crossLighting) {
            it.accept(crossModels[random])
        }
    }

    data class Key(
        override val cactusTop: Identifier,
        override val cactusBottom: Identifier,
        override val cactusSide: Identifier
    ) : CactusKey {
        override fun replace(model: BakedModel, state: BlockState) = CactusModel(this, meshifyStandard(model, state))
    }

    companion object {
        val cactusCrossSprite by SpriteDelegate(Atlas.BLOCKS) {
            Identifier(BetterFoliage.MOD_ID, "blocks/better_cactus")
        }
        val cactusArmSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_cactus_arm_$idx")
        }
        val cactusArmModels = LazyMap(BetterFoliage.modelReplacer) { key: CactusKey ->
            val shapes = BetterFoliage.config.cactus.let { tuftShapeSet(0.8, 0.8, 0.8, 0.2) }
            val models = tuftModelSet(shapes, Color.white.asInt) { cactusArmSprites[randomI()] }
            horizontalDirections.map { side ->
                models.transform { move(0.0625 to DOWN).rotate(Rotation.fromUp[side.ordinal]) }.buildTufts()
            }.toTypedArray()
        }
        val cactusCrossModels = LazyMap(BetterFoliage.modelReplacer) { key: CactusKey ->
            val models = BetterFoliage.config.cactus.let { config ->
                crossModelsRaw(64, config.size, 0.0, 0.0)
                    .transform { rotateZ(randomD(-config.sizeVariation, config.sizeVariation)) }
            }
            crossModelsTextured(models, Color.white.asInt, true) { cactusCrossSprite }
        }
    }
}
