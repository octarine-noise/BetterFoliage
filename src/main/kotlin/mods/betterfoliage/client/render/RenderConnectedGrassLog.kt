package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.texture.GrassRegistry
import mods.octarinecore.client.render.AbstractBlockRenderingHandler
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.offset
import mods.octarinecore.common.Int3
import mods.octarinecore.common.offset
import net.minecraft.block.Block
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.tags.BlockTags
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.*
import net.minecraftforge.client.model.data.IModelData
import java.util.*

class RenderConnectedGrassLog : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val grassCheckDirs = listOf(EAST, WEST, NORTH, SOUTH)

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled && Config.roundLogs.connectGrass &&
        BlockTags.DIRT_LIKE.contains(ctx.block) &&
        LogRegistry[ctx, up1] != null

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        val grassDir = grassCheckDirs.find {
            GrassRegistry[ctx, it.offset] != null
        } ?: return renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, layer)

        return ctx.offset(Int3.zero, grassDir.offset).let { offsetCtx ->
            renderWorldBlockBase(offsetCtx, dispatcher, renderer, random, modelData, layer)
        }
    }
}