package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.column.AbstractRenderColumn
import mods.betterfoliage.client.render.column.ColumnRenderLayer
import mods.betterfoliage.client.render.column.ColumnTextureInfo
import mods.betterfoliage.client.render.column.SimpleColumnInfo
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.tryDefault
import net.minecraft.block.BlockLog
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing.Axis
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class RenderLog : AbstractRenderColumn(BetterFoliageMod.MOD_ID) {

    override val addToCutout: Boolean get() = false

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled &&
        Config.blocks.logClasses.matchesClass(ctx.block)

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
    override val blockPredicate = { state: IBlockState -> Config.blocks.logClasses.matchesClass(state.block) }
    override val surroundPredicate = { state: IBlockState -> state.isOpaqueCube && !Config.blocks.logClasses.matchesClass(state.block) }

    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val defaultToY: Boolean get() = Config.roundLogs.defaultY
}

@SideOnly(Side.CLIENT)
object LogRegistry : ModelRenderRegistryRoot<ColumnTextureInfo>()

object StandardLogRegistry : ModelRenderRegistryConfigurable<ColumnTextureInfo>() {
    override val logger = BetterFoliageMod.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = Config.blocks.logClasses
    override val modelTextures: List<ModelTextureList> get() = Config.blocks.logModels.list
    override fun processModel(state: IBlockState, textures: List<String>) = SimpleColumnInfo.Key(logger, getAxis(state), textures)

    fun getAxis(state: IBlockState): Axis? {
        val axis = tryDefault(null) { state.getValue(BlockLog.LOG_AXIS).toString() } ?:
        state.properties.entries.find { it.key.getName().toLowerCase() == "axis" }?.value?.toString()
        return when (axis) {
            "x" -> Axis.X
            "y" -> Axis.Y
            "z" -> Axis.Z
            else -> null
        }
    }
}