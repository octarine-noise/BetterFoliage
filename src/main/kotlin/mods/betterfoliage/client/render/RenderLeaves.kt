package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.betterfoliage.client.texture.LeafRegistry
import mods.octarinecore.PI2
import mods.octarinecore.client.render.*
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraftforge.common.util.ForgeDirection.DOWN
import net.minecraftforge.common.util.ForgeDirection.UP
import java.lang.Math.cos
import java.lang.Math.sin

class RenderLeaves : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val leavesModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
        .setAoShader(edgeOrientedAuto(corner = cornerAoMaxGreen))
        .setFlatShader(FlatOffset(Int3.zero))
        .scale(Config.leaves.size)
        .toCross(UP).addAll()
    }
    val snowedIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "better_leaves_snowed_%d")

    val perturbs = vectorSet(64) { idx ->
        val angle = PI2 * idx / 64.0
        Double3(cos(angle), 0.0, sin(angle)) * Config.leaves.hOffset +
            UP.vec * random(-1.0, 1.0) * Config.leaves.vOffset
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled &&
        Config.leaves.enabled &&
        ctx.cameraDistance < Config.leaves.distance &&
        Config.blocks.leaves.matchesID(ctx.block)

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        val isSnowed = ctx.block(up1).material.let {
            it == Material.snow || it == Material.craftedSnow
        }

        if (renderWorldBlockBase(parent, face = alwaysRender)) return true

        val leafInfo = LeafRegistry.leaves[ctx.icon(DOWN)]
        if (leafInfo != null) ShadersModIntegration.leaves {
            val rand = ctx.semiRandomArray(2)
            (if (Config.leaves.dense) denseLeavesRot else normalLeavesRot).forEach { rotation ->
                modelRenderer.render(
                    leavesModel.model,
                    rotation,
                    ctx.blockCenter + perturbs[rand[0]],
                    icon = { ctx, qi, q -> leafInfo.roundLeafTexture },
                    rotateUV = { q -> rand[1] },
                    postProcess = noPost
                )
            }
            if (isSnowed && Config.leaves.snowEnabled) modelRenderer.render(
                leavesModel.model,
                Rotation.identity,
                ctx.blockCenter + perturbs[rand[0]],
                icon = { ctx, qi, q -> snowedIcon[rand[1]]!! },
                rotateUV = { 0 },
                postProcess = whitewash
            )
        }

        return true
    }
}