package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
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
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.lang.Math.cos
import java.lang.Math.sin

@SideOnly(Side.CLIENT)
class RenderLeaves : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val leavesModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
        .setAoShader(edgeOrientedAuto(corner = cornerAoMaxGreen))
        .setFlatShader(FlatOffset(Int3.zero))
        .scale(Config.leaves.size)
        .toCross(UP).addAll()
    }
    val snowedIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_leaves_snowed_%d")

    val perturbs = vectorSet(64) { idx ->
        val angle = PI2 * idx / 64.0
        Double3(cos(angle), 0.0, sin(angle)) * Config.leaves.hOffset +
            UP.vec * random(-1.0, 1.0) * Config.leaves.vOffset
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled &&
        Config.leaves.enabled &&
        ctx.cameraDistance < Config.leaves.distance &&
        LeafRegistry[ctx, DOWN] != null

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        val isSnowed = ctx.blockState(up1).material.let {
            it == Material.SNOW || it == Material.CRAFTED_SNOW
        }
        val leafInfo = LeafRegistry[ctx, DOWN] ?: return false
        val blockColor = ctx.blockData(Int3.zero).color

        renderWorldBlockBase(ctx, dispatcher, renderer, null)

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