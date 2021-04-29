package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.render.column.*
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.resource.discovery.*
import mods.betterfoliage.model.meshifyStandard
import mods.betterfoliage.util.LazyMap
import mods.betterfoliage.util.get
import mods.betterfoliage.util.tryDefault
import net.minecraft.block.BlockState
import net.minecraft.block.LogBlock
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction.Axis
import java.util.function.Consumer

object RoundLogOverlayLayer : ColumnRenderLayer() {
    override fun getColumnKey(state: BlockState) = BetterFoliage.modelReplacer.getTyped<ColumnBlockKey>(state)
    override val connectSolids: Boolean get() = BetterFoliage.config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = BetterFoliage.config.roundLogs.lenientConnect
    override val defaultToY: Boolean get() = BetterFoliage.config.roundLogs.defaultY
}

object StandardLogDiscovery : ConfigurableModelDiscovery() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BetterFoliage.blockConfig.logBlocks
    override val modelTextures: List<ModelTextureList> get() = BetterFoliage.blockConfig.logModels.modelList

    override fun processModel(state: BlockState, textures: List<Identifier>, atlas: Consumer<Identifier>): BlockRenderKey? {
        val axis = getAxis(state)
        log("       axis $axis")
        return RoundLogModel.Key(axis, textures[0], textures[1])
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

interface RoundLogKey : ColumnBlockKey, BlockRenderKey {
    val barkSprite: Identifier
    val endSprite: Identifier
}

class RoundLogModel(val key: Key, wrapped: BakedModel) : ColumnModelBase(wrapped) {
    override val enabled: Boolean get() = BetterFoliage.config.enabled && BetterFoliage.config.roundLogs.enabled
    override val overlayLayer: ColumnRenderLayer get() = RoundLogOverlayLayer
    override val connectPerpendicular: Boolean get() = BetterFoliage.config.roundLogs.connectPerpendicular

    val modelSet by modelSets.delegate(key)
    override fun getMeshSet(axis: Axis, quadrant: Int) = modelSet

    data class Key(
        override val axis: Axis?,
        override val barkSprite: Identifier,
        override val endSprite: Identifier
    ) : RoundLogKey {
        override fun replace(model: BakedModel, state: BlockState) = RoundLogModel(this, meshifyStandard(model, state))
    }

    companion object {
        val modelSets = LazyMap(BetterFoliage.modelReplacer) { key: Key ->
            val barkSprite = Atlas.BLOCKS.atlas[key.barkSprite]!!
            val endSprite = Atlas.BLOCKS.atlas[key.endSprite]!!
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
