package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.AbstractBlockRenderingHandler
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.modelRenderer
import mods.octarinecore.client.render.noPost
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Rotation
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.data.IModelData
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class RenderMycelium : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val myceliumIcon = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_mycel_$idx") }
    val myceliumModel = modelSet(64) { idx -> RenderGrass.grassTopQuads(Config.shortGrass.heightMin, Config.shortGrass.heightMax)(idx) }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${myceliumIcon.num} mycelium textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean {
        if (!Config.enabled || !Config.shortGrass.myceliumEnabled) return false
        return BlockConfig.mycelium.matchesClass(ctx.block)
    }

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        // render the whole block on the cutout layer
        if (!layer.isCutout) return false

        val isSnowed = ctx.blockState(up1).isSnow

        renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, null)

        if (isSnowed && !Config.shortGrass.snowEnabled) return true
        if (ctx.isNormalCube(up1)) return true

        val rand = ctx.semiRandomArray(2)
        modelRenderer.render(
            renderer,
            myceliumModel[rand[0]],
            Rotation.identity,
            ctx.blockCenter + (if (isSnowed) snowOffset else Double3.zero),
            icon = { _, qi, _ -> myceliumIcon[rand[qi and 1]]!! },
            postProcess = if (isSnowed) whitewash else noPost
        )

        return true
    }
}