package mods.betterfoliage.render.block.vanillaold

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.integration.OptifineCustomColors
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.render.denseLeavesRot
import mods.betterfoliage.render.isSnow
import mods.betterfoliage.resource.Identifier
import mods.betterfoliage.texture.LeafRegistry
import mods.betterfoliage.render.old.RenderDecorator
import mods.betterfoliage.render.lighting.FlatOffset
import mods.betterfoliage.render.lighting.cornerAoMaxGreen
import mods.betterfoliage.render.lighting.edgeOrientedAuto
import mods.betterfoliage.render.normalLeavesRot
import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.render.toCross
import mods.betterfoliage.render.whitewash
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.PI2
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.vec
import net.minecraft.util.Direction.UP
import java.lang.Math.cos
import java.lang.Math.sin

class RenderLeaves : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    val leavesModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
        .setAoShader(edgeOrientedAuto(corner = cornerAoMaxGreen))
        .setFlatShader(FlatOffset(Int3.zero))
        .scale(Config.leaves.size)
        .toCross(UP).addAll()
    }
    val snowedIcon = spriteSet { idx -> Identifier(BetterFoliageMod.MOD_ID, "blocks/better_leaves_snowed_$idx") }

    val perturbs = vectorSet(64) { idx ->
        val angle = PI2 * idx / 64.0
        Double3(cos(angle), 0.0, sin(angle)) * Config.leaves.hOffset +
            UP.vec * randomD(-1.0, 1.0) * Config.leaves.vOffset
    }

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled &&
        Config.leaves.enabled &&
        LeafRegistry[ctx] != null &&
        !(Config.leaves.hideInternal && allDirections.all { ctx.offset(it).isNormalCube } )

    override val onlyOnCutout get() = true

    override fun render(ctx: CombinedContext) {
        val isSnowed = ctx.state(UP).isSnow
        val leafInfo = LeafRegistry[ctx]!!
        val blockColor = OptifineCustomColors.getBlockColor(ctx)

        ctx.render(force = true)

        ShadersModIntegration.leaves(ctx) {
            val rand = ctx.semiRandomArray(2)
            (if (Config.leaves.dense) denseLeavesRot else normalLeavesRot).forEach { rotation ->
                ctx.render(
                    leavesModel.model,
                    rotation,
                    translation = ctx.blockCenter + perturbs[rand[0]],
                    icon = { _, _, _ -> leafInfo.roundLeafTexture },
                    postProcess = { _, _, _, _, _ ->
                        rotateUV(rand[1])
                        multiplyColor(blockColor)
                    }
                )
            }
            if (isSnowed && Config.leaves.snowEnabled) ctx.render(
                leavesModel.model,
                translation = ctx.blockCenter + perturbs[rand[0]],
                icon = { _, _, _ -> snowedIcon[rand[1]] },
                postProcess = whitewash
            )
        }
    }
}