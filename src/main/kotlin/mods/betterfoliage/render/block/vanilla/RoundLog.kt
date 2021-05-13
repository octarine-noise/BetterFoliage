package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.resource.discovery.ModelTextureList
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.render.column.ColumnBlockKey
import mods.betterfoliage.render.column.ColumnMeshSet
import mods.betterfoliage.render.column.ColumnModelBase
import mods.betterfoliage.render.column.ColumnRenderLayer
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ConfigurableBlockMatcher
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyMapInvalidatable
import mods.betterfoliage.util.tryDefault
import net.minecraft.block.BlockState
import net.minecraft.block.LogBlock
import net.minecraft.util.Direction.Axis
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.INFO

interface RoundLogKey : ColumnBlockKey, ModelBakingKey {
    val barkSprite: ResourceLocation
    val endSprite: ResourceLocation
}

object RoundLogOverlayLayer : ColumnRenderLayer() {
    override fun getColumnKey(state: BlockState) = BetterFoliage.blockTypes.getTypedOrNull<ColumnBlockKey>(state)
    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val defaultToY: Boolean get() = Config.roundLogs.defaultY
}

object StandardRoundLogDiscovery : ConfigurableModelDiscovery() {
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.logBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.logModels.modelList

    override fun processModel(ctx: ModelDiscoveryContext, textureMatch: List<ResourceLocation>) {
        val axis = getAxis(ctx.blockState)
        detailLogger.log(INFO, "       axis $axis")
        ctx.addReplacement(StandardRoundLogKey(axis, textureMatch[0], textureMatch[1]))
    }

    fun getAxis(state: BlockState): Axis? {
        val axis = tryDefault(null) { state.get(LogBlock.AXIS).toString() } ?:
        state.values.entries.find { it.key.getName().toLowerCase() == "axis" }?.value?.toString()
        return when (axis) {
            "x" -> Axis.X
            "y" -> Axis.Y
            "z" -> Axis.Z
            else -> null
        }
    }
}

data class StandardRoundLogKey(
    override val axis: Axis?,
    override val barkSprite: ResourceLocation,
    override val endSprite: ResourceLocation
) : RoundLogKey, HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardRoundLogModel(this, wrapped)
}

class StandardRoundLogModel(
    val key: StandardRoundLogKey,
    wrapped: SpecialRenderModel
) : ColumnModelBase(wrapped) {
    override val enabled: Boolean get() = Config.enabled && Config.roundLogs.enabled
    override val overlayLayer: ColumnRenderLayer get() = RoundLogOverlayLayer
    override val connectPerpendicular: Boolean get() = Config.roundLogs.connectPerpendicular

    val modelSet by modelSets.delegate(key)
    override fun getMeshSet(axis: Axis, quadrant: Int) = modelSet

    companion object {
        val modelSets = LazyMapInvalidatable(BakeWrapperManager) { key: StandardRoundLogKey ->
            val barkSprite = Atlas.BLOCKS[key.barkSprite]
            val endSprite = Atlas.BLOCKS[key.endSprite]
            Config.roundLogs.let { config ->
                ColumnMeshSet(
                    config.radiusSmall, config.radiusLarge, config.zProtection,
                    key.axis ?: Axis.Y,
                    barkSprite, barkSprite,
                    endSprite, endSprite
                )
            }
        }
    }
}