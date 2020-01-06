package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.resource.Identifier
import mods.octarinecore.client.render.*
import mods.octarinecore.client.render.lighting.*
import mods.octarinecore.random
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.*
import org.apache.logging.log4j.Level.DEBUG

class RenderNetherrack : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    val netherrackIcon = spriteSet { idx -> Identifier(BetterFoliageMod.MOD_ID, "blocks/better_netherrack_$idx") }
    val netherrackModel = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yTop = -0.5,
        yBottom = -0.5 - random(Config.netherrack.heightMin, Config.netherrack.heightMax))
        .setAoShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerAo(Axis.Y)))
        .setFlatShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerFlat))
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()

    }

    override fun isEligible(ctx: CombinedContext): Boolean {
        if (!Config.enabled || !Config.netherrack.enabled) return false
        return BlockConfig.netherrack.matchesClass(ctx.state.block)
    }

    override fun render(ctx: CombinedContext) {
        ctx.render()
        if (!ctx.isCutout) return
        if (ctx.offset(DOWN).isNormalCube) return

        val rand = ctx.semiRandomArray(2)
        ctx.render(
            netherrackModel[rand[0]],
            icon = { _, qi, _ -> netherrackIcon[rand[qi and 1]] }
        )
    }
}