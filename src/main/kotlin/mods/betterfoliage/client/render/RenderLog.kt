package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.column.AbstractRenderColumn
import mods.betterfoliage.client.render.column.ColumnRenderLayer
import mods.betterfoliage.client.render.column.ColumnTextureInfo
import mods.betterfoliage.client.render.column.SimpleColumnInfo
import mods.octarinecore.client.render.BlockCtx
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.client.resource.ModelRenderRegistry
import mods.octarinecore.client.resource.ModelRenderRegistryConfigurable
import mods.octarinecore.client.resource.ModelRenderRegistryRoot
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.tryDefault
import net.minecraft.block.BlockState
import net.minecraft.block.LogBlock
import net.minecraft.util.Direction.Axis
import net.minecraft.world.IEnviromentBlockReader

class RenderLog : AbstractRenderColumn(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    override val renderOnCutout: Boolean get() = false

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled && Config.roundLogs.enabled &&
        LogRegistry[ctx] != null

    override val overlayLayer = RoundLogOverlayLayer()
    override val connectPerpendicular: Boolean get() = Config.roundLogs.connectPerpendicular
    override val radiusSmall: Double get() = Config.roundLogs.radiusSmall
    override val radiusLarge: Double get() = Config.roundLogs.radiusLarge
    init {
        ChunkOverlayManager.layers.add(overlayLayer)
    }
}

class RoundLogOverlayLayer : ColumnRenderLayer() {
    override val registry: ModelRenderRegistry<ColumnTextureInfo> get() = LogRegistry
    override val blockPredicate = { state: BlockState -> BlockConfig.logBlocks.matchesClass(state.block) }

    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val defaultToY: Boolean get() = Config.roundLogs.defaultY
}

object LogRegistry : ModelRenderRegistryRoot<ColumnTextureInfo>()

object StandardLogRegistry : ModelRenderRegistryConfigurable<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.logBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.logModels.modelList
    override fun processModel(state: BlockState, textures: List<String>) = SimpleColumnInfo.Key(logger, getAxis(state), textures)
    init { BetterFoliage.modBus.register(this) }

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