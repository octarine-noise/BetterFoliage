package mods.betterfoliage.client.render

import com.google.common.collect.ImmutableMap
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCTM
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.Quad
import mods.octarinecore.client.render.ShadingContext
import mods.octarinecore.client.render.blockContext
import mods.octarinecore.common.Int3
import mods.octarinecore.common.rotate
import mods.octarinecore.tryDefault
import net.minecraft.block.BlockLog
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing.*

class RenderLog : AbstractRenderColumn(BetterFoliageMod.MOD_ID) {

    override val moveToCutout: Boolean get() = false

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled &&
        ctx.cameraDistance < Config.roundLogs.distance &&
        Config.blocks.logs.matchesID(ctx.block)

    override var axisFunc = { state: IBlockState ->
        var axis = tryDefault(null) { state.getValue(BlockLog.LOG_AXIS).toString() } ?:
        (state.properties as ImmutableMap<IProperty, Any>).entries
            .find { it.key.name.toLowerCase() == "axis" }?.let { it.value.toString() }
        when (axis) {
            "x" -> Axis.X
            "y" -> Axis.Y
            "z" -> Axis.Z
            else -> if (Config.roundLogs.defaultY) Axis.Y else null
        }
    }

    val columnTextures = object : ColumnTextures(Config.blocks.logs) {
        init {
            matchClassAndModel(matcher, "plantmegapack:block/_cube_column", listOf("end", "end", "side"))
            matchClassAndModel(matcher, "plantmegapack:block/_column_side", listOf("end", "end", "side"))
            matchClassAndModel(matcher, "cookingplus:block/palmlog", listOf("top", "top", "texture"))
        }
    }

    override fun resolver(ctx: BlockContext): ColumnTextureResolver? = columnTextures[ctx.blockState(Int3.zero)]

    override val blockPredicate = { state: IBlockState -> Config.blocks.logs.matchesID(state.block) }
    override val surroundPredicate = { state: IBlockState -> state.block.isOpaqueCube && !Config.blocks.logs.matchesID(state.block) }

    override val connectPerpendicular: Boolean get() = Config.roundLogs.connectPerpendicular
    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val radiusLarge: Double get() = Config.roundLogs.radiusLarge
    override val radiusSmall: Double get() = Config.roundLogs.radiusSmall

}