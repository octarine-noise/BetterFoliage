package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCustomColors
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.betterfoliage.client.texture.LeafRegistry
import mods.octarinecore.PI2
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.vec
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.data.IModelData
import java.lang.Math.cos
import java.lang.Math.sin
import java.util.*

class RenderLeaves : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val leavesModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
        .setAoShader(edgeOrientedAuto(corner = cornerAoMaxGreen))
        .setFlatShader(FlatOffset(Int3.zero))
        .scale(Config.leaves.size)
        .toCross(UP).addAll()
    }
    val snowedIcon = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_leaves_snowed_$idx") }

    val perturbs = vectorSet(64) { idx ->
        val angle = PI2 * idx / 64.0
        Double3(cos(angle), 0.0, sin(angle)) * Config.leaves.hOffset +
            UP.vec * random(-1.0, 1.0) * Config.leaves.vOffset
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled &&
        Config.leaves.enabled &&
        LeafRegistry[ctx] != null &&
        !(Config.leaves.hideInternal && ctx.isSurroundedByNormal)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        val isSnowed = ctx.blockState(up1).isSnow
        val leafInfo = LeafRegistry[ctx]
        if (leafInfo == null) {
            // shouldn't happen
            Client.logRenderError(ctx.blockState(Int3.zero), ctx.pos)
            return renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, layer)
        }
        val blockColor = OptifineCustomColors.getBlockColor(ctx)

        renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, layer)
        if (!layer.isCutout) return true

        modelRenderer.updateShading(Int3.zero, allFaces)
        ShadersModIntegration.leaves(renderer) {
            val rand = ctx.semiRandomArray(2)
            (if (Config.leaves.dense) denseLeavesRot else normalLeavesRot).forEach { rotation ->
                modelRenderer.render(
                    renderer,
                    leavesModel.model,
                    rotation,
                    ctx.blockCenter + perturbs[rand[0]],
                    icon = { _, _, _ -> leafInfo.roundLeafTexture },
                    postProcess = { _, _, _, _, _ ->
                        rotateUV(rand[1])
                        multiplyColor(blockColor)
                    }
                )
            }
            if (isSnowed && Config.leaves.snowEnabled) modelRenderer.render(
                renderer,
                leavesModel.model,
                Rotation.identity,
                ctx.blockCenter + perturbs[rand[0]],
                icon = { _, _, _ -> snowedIcon[rand[1]]!! },
                postProcess = whitewash
            )
        }

        return true
    }
}