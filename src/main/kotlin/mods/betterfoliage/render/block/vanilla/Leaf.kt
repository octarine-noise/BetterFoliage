package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.render.SNOW_MATERIALS
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.render.lighting.roundLeafLighting
import mods.betterfoliage.render.particle.LeafParticleRegistry
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.resource.discovery.*
import mods.betterfoliage.resource.generated.GeneratedLeafSprite
import mods.betterfoliage.resource.model.*
import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockRenderLayer.CUTOUT_MIPPED
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.*
import net.minecraft.world.ExtendedBlockView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

interface LeafKey : BlockRenderKey {
    val roundLeafTexture: Identifier

    /** Type of the leaf block (configurable by user). */
    val leafType: String

    /** Average color of the round leaf texture. */
    val overrideColor: Int?
}

object StandardLeafDiscovery : ConfigurableModelDiscovery() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BetterFoliage.blockConfig.leafBlocks
    override val modelTextures: List<ModelTextureList> get() = BetterFoliage.blockConfig.leafModels.modelList

    override fun processModel(state: BlockState, textures: List<String>, atlas: Consumer<Identifier>) =
        defaultRegisterLeaf(Identifier(textures[0]), atlas)

}

fun HasLogger.defaultRegisterLeaf(sprite: Identifier, atlas: Consumer<Identifier>): BlockRenderKey? {
    val leafType = LeafParticleRegistry.typeMappings.getType(sprite) ?: "default"
    val leafId = GeneratedLeafSprite(sprite, leafType).register(BetterFoliage.generatedPack)
    atlas.accept(leafId)

    log(" leaf texture $sprite")
    log("     particle $leafType")

    return NormalLeavesModel.Key(
        leafId, leafType,
        getAndLogColorOverride(leafId, Atlas.BLOCKS, BetterFoliage.config.shortGrass.saturationThreshold)
    )
}

fun HasLogger.getAndLogColorOverride(sprite: Identifier, atlas: Atlas, threshold: Double): Int? {
    val hsb = resourceManager.averageImageColorHSB(sprite, atlas)
    return if (hsb.saturation >= threshold) {
        log("         brightness ${hsb.brightness}")
        log("         saturation ${hsb.saturation} >= ${threshold}, using texture color")
        hsb.copy(brightness = 0.9f.coerceAtMost(hsb.brightness * 2.0f)).asColor
    } else {
        log("         saturation ${hsb.saturation} < ${threshold}, using block color")
        null
    }
}

class NormalLeavesModel(val key: Key, wrapped: BakedModel) : WrappedBakedModel(wrapped), FabricBakedModel {

    val leafNormal by leafModelsNormal.delegate(key)
    val leafSnowed by leafModelsSnowed.delegate(key)
    val leafLighting = roundLeafLighting()

    override fun emitBlockQuads(blockView: ExtendedBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        if (!BetterFoliage.config.enabled || !BetterFoliage.config.leaves.enabled) return

        val ctx = BasicBlockCtx(blockView, pos)
        val stateAbove = ctx.state(UP)
        val isSnowed = stateAbove.material in SNOW_MATERIALS

        val random = randomSupplier.get()
        context.withLighting(leafLighting) {
            it.accept(leafNormal[random])
            if (isSnowed) it.accept(leafSnowed[random])
        }
    }

    data class Key(
        override val roundLeafTexture: Identifier,
        override val leafType: String,
        override val overrideColor: Int?
    ) : LeafKey {
        override fun replace(model: BakedModel, state: BlockState) = NormalLeavesModel(this, meshifyStandard(model, state, renderLayerOverride = CUTOUT_MIPPED))
    }

    companion object {
        val leafSpritesSnowed by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_leaves_snowed_$idx")
        }
        val leafModelsBase = LazyMap(BetterFoliage.modelReplacer) { key: LeafKey ->
            BetterFoliage.config.leaves.let { crossModelsRaw(64, it.size, it.hOffset, it.vOffset) }
        }
        val leafModelsNormal = LazyMap(BetterFoliage.modelReplacer) { key: LeafKey ->
            crossModelsTextured(leafModelsBase[key], key.overrideColor, true) { Atlas.BLOCKS.atlas[key.roundLeafTexture]!! }
        }
        val leafModelsSnowed = LazyMap(BetterFoliage.modelReplacer) { key: LeafKey ->
            crossModelsTextured(leafModelsBase[key], Color.white.asInt, false) { leafSpritesSnowed[it] }
        }
    }
}
