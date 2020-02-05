package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.roundLeafLighting
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.resource.discovery.*
import mods.betterfoliage.resource.model.*
import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.CactusBlock
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.world.ExtendedBlockView
import java.util.*
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

    override fun processModel(state: BlockState, textures: List<String>, atlas: Consumer<Identifier>): BlockRenderKey? {
        val sprites = textures.map { Identifier(it) }
        return CactusModel.Key(sprites[0], sprites[1], sprites[2])
    }
}

class CactusModel(val key: Key, wrapped: BakedModel) : WrappedBakedModel(wrapped), FabricBakedModel {

    val crossModels by cactusCrossModels.delegate(key)
    val armModels by cactusArmModels.delegate(key)
    val armLighting = horizontalDirections.map { grassTuftLighting(it) }
    val crossLighting = roundLeafLighting()

    override fun emitBlockQuads(blockView: ExtendedBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
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
