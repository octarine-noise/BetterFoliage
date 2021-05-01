package mods.betterfoliage.render.block.vanillaold

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.render.column.AbstractRenderColumn
import mods.betterfoliage.render.column.ColumnRenderLayer
import mods.betterfoliage.render.column.ColumnTextureInfo
import mods.betterfoliage.render.column.SimpleColumnInfo
import mods.betterfoliage.resource.Identifier
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelRenderRegistry
import mods.betterfoliage.resource.discovery.ModelRenderRegistryRoot
import mods.octarinecore.client.resource.*
import mods.betterfoliage.config.ConfigurableBlockMatcher
import mods.betterfoliage.config.ModelTextureList
import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.util.tryDefault
import net.minecraft.block.BlockState
import net.minecraft.block.LogBlock
import net.minecraft.util.Direction.Axis
import org.apache.logging.log4j.Level
import java.util.concurrent.CompletableFuture

class RenderLog : AbstractRenderColumn(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

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

object AsyncLogDiscovery : ConfigurableModelDiscovery<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.logBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.logModels.modelList

    override fun processModel(state: BlockState, textures: List<Identifier>, atlas: AtlasFuture): CompletableFuture<ColumnTextureInfo> {
        val axis = getAxis(state)
        logger.log(Level.DEBUG, "$logName:       axis $axis")
        val spriteList = textures.map { atlas.sprite(it) }
        return atlas.mapAfter {
            SimpleColumnInfo(
                axis,
                spriteList[0].get(),
                spriteList[1].get(),
                spriteList.drop(2).map { it.get() }
            )
        }
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

    fun init() {
        LogRegistry.registries.add(this)
        BetterFoliage.blockSprites.providers.add(this)
    }
}