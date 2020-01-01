package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.texture.GrassRegistry
import mods.octarinecore.client.render.AbstractBlockRenderingHandler
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.offset
import mods.octarinecore.common.Int3
import mods.octarinecore.common.forgeDirsHorizontal
import mods.octarinecore.common.offset
import net.minecraft.block.Block
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.tags.BlockTags
import net.minecraft.util.BlockRenderLayer
import net.minecraftforge.client.model.data.IModelData
import java.util.*

class RenderConnectedGrass : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {
    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.connectedGrass.enabled &&
        BlockTags.DIRT_LIKE.contains(ctx.block) &&
        GrassRegistry[ctx, up1] != null &&
        (Config.connectedGrass.snowEnabled || !ctx.blockState(up2).isSnow)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        // if the block sides are not visible anyway, render normally
        if (forgeDirsHorizontal.none { ctx.shouldSideBeRendered(it) }) return renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, layer)

        return ctx.offset(Int3.zero, up1).offset(up1, up2).let { offsetCtx ->
            renderWorldBlockBase(offsetCtx, dispatcher, renderer, random, modelData, layer)
        }
    }
}