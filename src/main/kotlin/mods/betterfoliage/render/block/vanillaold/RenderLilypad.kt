package mods.betterfoliage.render.block.vanillaold

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.resource.Identifier
import mods.betterfoliage.render.old.RenderDecorator
import mods.betterfoliage.render.lighting.FlatOffsetNoColor
import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.render.toCross
import mods.betterfoliage.render.xzDisk
import mods.betterfoliage.util.Int3
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP

class RenderLilypad : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    val rootModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -1.5, yTop = -0.5)
        .setFlatShader(FlatOffsetNoColor(Int3.zero))
        .toCross(UP).addAll()
    }
    val flowerModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.0, yTop = 1.0)
        .scale(0.5).move(0.5 to DOWN)
        .setFlatShader(FlatOffsetNoColor(Int3.zero))
        .toCross(UP).addAll()
    }
    val rootIcon = spriteSet { idx -> Identifier(BetterFoliageMod.MOD_ID, "blocks/better_lilypad_roots_$idx") }
    val flowerIcon = spriteSet { idx -> Identifier(BetterFoliageMod.MOD_ID, "blocks/better_lilypad_flower_$idx") }
    val perturbs = vectorSet(64) { modelIdx -> xzDisk(modelIdx) * Config.lilypad.hOffset }

    override fun isEligible(ctx: CombinedContext): Boolean =
        Config.enabled && Config.lilypad.enabled &&
        BlockConfig.lilypad.matchesClass(ctx.state.block)

    override fun render(ctx: CombinedContext) {
        ctx.render()

        val rand = ctx.semiRandomArray(5)
        ShadersModIntegration.grass(ctx) {
            ctx.render(
                rootModel.model,
                translation = ctx.blockCenter.add(perturbs[rand[2]]),
                forceFlat = true,
                icon = { ctx, qi, q -> rootIcon[rand[qi and 1]] }
            )
        }

        if (rand[3] < Config.lilypad.flowerChance) ctx.render(
            flowerModel.model,
            translation = ctx.blockCenter.add(perturbs[rand[4]]),
            forceFlat = true,
            icon = { _, _, _ -> flowerIcon[rand[0]] }
        )
    }
}