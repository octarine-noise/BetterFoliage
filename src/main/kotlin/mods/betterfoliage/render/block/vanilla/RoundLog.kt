package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.model.ModelWrapKey
import mods.betterfoliage.model.meshifySolid
import mods.betterfoliage.model.meshifyStandard
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
import mods.betterfoliage.resource.discovery.ModelTextureList
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyMap
import mods.betterfoliage.util.tryDefault
import net.minecraft.block.BlockState
import net.minecraft.block.LogBlock
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction.Axis
import org.apache.logging.log4j.Level

interface RoundLogKey : ColumnBlockKey, ModelBakingKey {
    val barkSprite: Identifier
    val endSprite: Identifier
}

object RoundLogOverlayLayer : ColumnRenderLayer() {
    override fun getColumnKey(state: BlockState) = BetterFoliage.blockTypes.getTyped<ColumnBlockKey>(state)
    override val connectSolids: Boolean get() = BetterFoliage.config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = BetterFoliage.config.roundLogs.lenientConnect
    override val defaultToY: Boolean get() = BetterFoliage.config.roundLogs.defaultY
}

object StandardRoundLogDiscovery : ConfigurableModelDiscovery() {
    override val matchClasses: ConfigurableBlockMatcher get() = BetterFoliage.blockConfig.logBlocks
    override val modelTextures: List<ModelTextureList> get() = BetterFoliage.blockConfig.logModels.modelList

    override fun processModel(ctx: ModelDiscoveryContext, textureMatch: List<Identifier>) {
        val axis = getAxis(ctx.blockState)
        detailLogger.log(Level.INFO, "       axis $axis")
        ctx.addReplacement(StandardRoundLogKey(axis, textureMatch[0], textureMatch[1]))
    }

    fun getAxis(state: BlockState): Axis? {
        val axis = tryDefault(null) { state.get(LogBlock.AXIS).toString() } ?:
        state.entries.entries.find { it.key.getName().toLowerCase() == "axis" }?.value?.toString()
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
    override val barkSprite: Identifier,
    override val endSprite: Identifier
) : RoundLogKey, ModelWrapKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel) = StandardRoundLogModel(meshifySolid(wrapped), this)
}

class StandardRoundLogModel(wrapped: BakedModel, val key: StandardRoundLogKey) : ColumnModelBase(wrapped) {
    override val enabled: Boolean get() = BetterFoliage.config.enabled && BetterFoliage.config.roundLogs.enabled
    override val overlayLayer: ColumnRenderLayer get() = RoundLogOverlayLayer
    override val connectPerpendicular: Boolean get() = BetterFoliage.config.roundLogs.connectPerpendicular

    val modelSet by modelSets.delegate(key)
    override fun getMeshSet(axis: Axis, quadrant: Int) = modelSet

    companion object {
        val modelSets = LazyMap(BakeWrapperManager) { key: StandardRoundLogKey ->
            val barkSprite = Atlas.BLOCKS[key.barkSprite]!!
            val endSprite = Atlas.BLOCKS[key.endSprite]!!
            BetterFoliage.config.roundLogs.let { config ->
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
