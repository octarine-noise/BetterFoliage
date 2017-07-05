package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.random
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.init.Blocks
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.*
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO

@SideOnly(Side.CLIENT)
class RenderNetherrack : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val netherrackIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_netherrack_%d")
    val netherrackModel = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yTop = -0.5,
        yBottom = -0.5 - random(Config.netherrack.heightMin, Config.netherrack.heightMax))
        .setAoShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerAo(Axis.Y)))
        .setFlatShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerFlat))
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()

    }

    override fun afterStitch() {
        Client.log(INFO, "Registered ${netherrackIcon.num} netherrack textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean {
        if (!Config.enabled || !Config.netherrack.enabled) return false
        return ctx.block == Blocks.NETHERRACK &&
        ctx.cameraDistance < Config.netherrack.distance
    }

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, layer: BlockRenderLayer): Boolean {
        val baseRender = renderWorldBlockBase(ctx, dispatcher, renderer, layer)
        if (!layer.isCutout) return baseRender

        if (ctx.blockState(down1).isOpaqueCube) return baseRender

        modelRenderer.updateShading(Int3.zero, allFaces)

        val rand = ctx.semiRandomArray(2)
        modelRenderer.render(
            renderer,
            netherrackModel[rand[0]],
            Rotation.identity,
            icon = { _, qi, _ -> netherrackIcon[rand[qi and 1]]!! },
            postProcess = noPost
        )

        return true
    }
}