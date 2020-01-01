package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.random
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.data.IModelData
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class RenderNetherrack : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val netherrackIcon = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_netherrack_$idx") }
    val netherrackModel = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yTop = -0.5,
        yBottom = -0.5 - random(Config.netherrack.heightMin, Config.netherrack.heightMax))
        .setAoShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerAo(Axis.Y)))
        .setFlatShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerFlat))
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()

    }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${netherrackIcon.num} netherrack textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean {
        if (!Config.enabled || !Config.netherrack.enabled) return false
        return BlockConfig.netherrack.matchesClass(ctx.block)
    }

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        val baseRender = renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, layer)
        if (!layer.isCutout) return baseRender

        if (ctx.isNormalCube(down1)) return baseRender

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