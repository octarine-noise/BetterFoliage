package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.AbstractBlockRenderingHandler
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.modelRenderer
import mods.octarinecore.client.render.noPost
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Rotation
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.init.Blocks
import net.minecraft.util.BlockRenderLayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO

@SideOnly(Side.CLIENT)
class RenderMycelium : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val myceliumIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_mycel_%d")
    val myceliumModel = modelSet(64, RenderGrass.grassTopQuads(Config.shortGrass.heightMin, Config.shortGrass.heightMax))

    override fun afterStitch() {
        Client.log(INFO, "Registered ${myceliumIcon.num} mycelium textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean {
        if (!Config.enabled || !Config.shortGrass.myceliumEnabled) return false
        return ctx.block == Blocks.MYCELIUM &&
        ctx.cameraDistance < Config.shortGrass.distance
    }

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        // render the whole block on the cutout layer
        if (!layer.isCutout) return false

        val isSnowed = ctx.blockState(up1).isSnow

        renderWorldBlockBase(ctx, dispatcher, renderer, null)

        if (isSnowed && !Config.shortGrass.snowEnabled) return true
        if (ctx.blockState(up1).isOpaqueCube) return true

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