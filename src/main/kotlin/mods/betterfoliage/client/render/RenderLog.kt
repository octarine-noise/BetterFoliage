package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.Quad
import mods.octarinecore.client.render.ShadingContext
import mods.octarinecore.client.resource.BlockTextureInspector
import mods.octarinecore.common.Int3
import net.minecraft.block.BlockLog
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing.Axis

class RenderLog : AbstractRenderColumn(BetterFoliageMod.MOD_ID) {

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled &&
        ctx.cameraDistance < Config.roundLogs.distance &&
        Config.blocks.logs.matchesID(ctx.block)

    override var axisFunc = { state: IBlockState ->
        when (state.getValue(BlockLog.LOG_AXIS).toString()) {
            "x" -> Axis.X
            "z" -> Axis.Z
            else -> Axis.Y
        }
    }

    val columnTextures = ColumnTextures(Config.blocks.logs)

    override val blockPredicate = { state: IBlockState -> Config.blocks.logs.matchesID(state.block) }
    override val surroundPredicate = { state: IBlockState -> state.block.isOpaqueCube && !Config.blocks.logs.matchesID(state.block) }

    override val connectPerpendicular: Boolean get() = Config.roundLogs.connectPerpendicular
    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val radiusLarge: Double get() = Config.roundLogs.radiusLarge
    override val radiusSmall: Double get() = Config.roundLogs.radiusSmall

    override val downTexture = { ctx: ShadingContext, idx: Int, quad: Quad ->
        columnTextures[ctx.blockData(Int3.zero).state]?.bottomTexture
    }
    override val sideTexture = { ctx: ShadingContext, idx: Int, quad: Quad ->
        columnTextures[ctx.blockData(Int3.zero).state]?.sideTexture
    }
    override val upTexture = { ctx: ShadingContext, idx: Int, quad: Quad ->
        columnTextures[ctx.blockData(Int3.zero).state]?.topTexture
    }
}