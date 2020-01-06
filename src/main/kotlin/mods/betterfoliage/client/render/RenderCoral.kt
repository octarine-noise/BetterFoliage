package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.client.render.lighting.*
import mods.octarinecore.common.allDirections
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.Biome
import org.apache.logging.log4j.Level.DEBUG

class RenderCoral : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    val noise = simplexNoise()

    val coralIcons = spriteSet { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_coral_$idx") }
    val crustIcons = spriteSet { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_crust_$idx") }
    val coralModels = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.0, yTop = 1.0)
        .scale(Config.coral.size).move(0.5 to UP)
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.coral.hOffset) }.addAll()

        val separation = random(0.01, Config.coral.vOffset)
        horizontalRectangle(x1 = -0.5, x2 = 0.5, z1 = -0.5, z2 = 0.5, y = 0.0)
        .scale(Config.coral.crustSize).move(0.5 + separation to UP).add()

        transformQ {
            it.setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
            .setFlatShader(faceOrientedAuto(overrideFace = UP, corner = cornerFlat))
        }
    }

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled && Config.coral.enabled &&
        (ctx.state(up2).material == Material.WATER || Config.coral.shallowWater) &&
        ctx.state(up1).material == Material.WATER &&
        BlockConfig.sand.matchesClass(ctx.state.block) &&
        ctx.biome.category.let { it == Biome.Category.OCEAN || it == Biome.Category.BEACH } &&
        noise[ctx.pos] < Config.coral.population

    override fun render(ctx: CombinedContext) {
        val baseRender = ctx.render()
        if (!ctx.isCutout) return

        allDirections.forEachIndexed { idx, face ->
            if (ctx.state(face).material == Material.WATER && ctx.semiRandom(idx) < Config.coral.chance) {
                var variation = ctx.semiRandom(6)
                ctx.render(
                    coralModels[variation++],
                    rotationFromUp[idx],
                    icon = { _, qi, _ -> if (qi == 4) crustIcons[variation] else coralIcons[variation + (qi and 1)] }
                )
            }
        }
    }
}