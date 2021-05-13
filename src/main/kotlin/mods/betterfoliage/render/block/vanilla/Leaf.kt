package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.config.SNOW_MATERIALS
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.ModelWrapKey
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.crossModelsRaw
import mods.betterfoliage.model.crossModelsTextured
import mods.betterfoliage.model.meshifyCutoutMipped
import mods.betterfoliage.render.ShadersModIntegration
import mods.betterfoliage.render.lighting.roundLeafLighting
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.render.particle.LeafParticleRegistry
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ConfigurableBlockMatcher
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.ModelTextureList
import mods.betterfoliage.resource.generated.GeneratedLeafSprite
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyMap
import mods.betterfoliage.util.averageColor
import mods.betterfoliage.util.colorOverride
import mods.betterfoliage.util.get
import mods.betterfoliage.util.logColorOverride
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.UP
import net.minecraft.world.BlockRenderView
import org.apache.logging.log4j.Level
import java.util.Random
import java.util.function.Supplier

interface LeafBlockModel {
    val key: LeafParticleKey
}

interface LeafParticleKey {
    val leafType: String
    val overrideColor: Color?
}

object StandardLeafDiscovery : ConfigurableModelDiscovery() {
    override val matchClasses: ConfigurableBlockMatcher get() = BetterFoliage.blockConfig.leafBlocks
    override val modelTextures: List<ModelTextureList> get() = BetterFoliage.blockConfig.leafModels.modelList

    override fun processModel(ctx: ModelDiscoveryContext, textureMatch: List<Identifier>) {
        val leafType = LeafParticleRegistry.typeMappings.getType(textureMatch[0]) ?: "default"
        val generated = GeneratedLeafSprite(textureMatch[0], leafType)
            .register(BetterFoliage.generatedPack)
            .apply { ctx.sprites.add(this) }

        detailLogger.log(Level.INFO, "     particle $leafType")
        ctx.addReplacement(StandardLeafKey(generated, leafType, null))
    }
}

data class StandardLeafKey(
    val roundLeafTexture: Identifier,
    override val leafType: String,
    override val overrideColor: Color?
) : ModelWrapKey(), LeafParticleKey {
    val tintIndex: Int get() = if (overrideColor == null) 0 else -1

    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel): BakedModel {
        val leafSpriteColor = Atlas.BLOCKS[roundLeafTexture].averageColor.let { hsb ->
            logColorOverride(detailLogger, BetterFoliage.config.leaves.saturationThreshold, hsb)
            hsb.colorOverride(BetterFoliage.config.leaves.saturationThreshold)
        }
        return StandardLeafModel(meshifyCutoutMipped(wrapped), this.copy(overrideColor = leafSpriteColor))
    }
}

class StandardLeafModel(
    wrapped: BakedModel,
    override val key: StandardLeafKey
) : WrappedBakedModel(wrapped), LeafBlockModel {

    val leafNormal by leafModelsNormal.delegate(key)
    val leafSnowed by leafModelsSnowed.delegate(key)
    val leafLighting = roundLeafLighting()

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        ShadersModIntegration.leaves(context, BetterFoliage.config.leaves.shaderWind) {
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
    }

    companion object {
        val leafSpritesSnowed by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_leaves_snowed_$idx")
        }
        val leafModelsBase = LazyMap(BakeWrapperManager) { key: StandardLeafKey ->
            BetterFoliage.config.leaves.let { crossModelsRaw(64, it.size, it.hOffset, it.vOffset) }
        }
        val leafModelsNormal = LazyMap(BakeWrapperManager) { key: StandardLeafKey ->
            crossModelsTextured(leafModelsBase[key], key.tintIndex, true) { key.roundLeafTexture }
        }
        val leafModelsSnowed = LazyMap(BakeWrapperManager) { key: StandardLeafKey ->
            crossModelsTextured(leafModelsBase[key], Color.white.asInt, false) { leafSpritesSnowed[it].id }
        }
    }
}
