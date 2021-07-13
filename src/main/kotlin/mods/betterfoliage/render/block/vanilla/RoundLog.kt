package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.ACCEPTED_ROUND_LOG_MATERIALS
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.render.column.ColumnBlockKey
import mods.betterfoliage.render.column.ColumnMeshSet
import mods.betterfoliage.render.column.ColumnModelBase
import mods.betterfoliage.render.column.ColumnRenderLayer
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.ParametrizedModelDiscovery
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.lazyMap
import mods.betterfoliage.util.tryDefault
import net.minecraft.block.BlockState
import net.minecraft.block.RotatedPillarBlock
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

object StandardRoundLogDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        val barkSprite = params.location("texture-side") ?: return
        val endSprite = params.location("texture-end") ?: return
        val axis = getAxis(ctx.blockState)

        detailLogger.log(INFO, "       axis $axis, material ${ctx.blockState.material}")
        if (!Config.roundLogs.plantsOnly || ctx.blockState.material in ACCEPTED_ROUND_LOG_MATERIALS)
            ctx.addReplacement(StandardRoundLogKey(axis, barkSprite, endSprite))
    }

    fun getAxis(state: BlockState): Axis? {
        val axis = tryDefault(null) { state.getValue(RotatedPillarBlock.AXIS).toString() } ?:
        state.values.entries.find { it.key.name.toLowerCase() == "axis" }?.value?.toString()
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
        val modelSets = BetterFoliage.modelManager.lazyMap { key: StandardRoundLogKey ->
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