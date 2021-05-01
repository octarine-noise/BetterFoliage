package mods.betterfoliage.render.block.vanillaold

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.render.DIRT_BLOCKS
import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.render.up1
import mods.betterfoliage.render.up2
import mods.betterfoliage.render.old.RenderDecorator
import net.minecraft.block.material.Material
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.Biome

class RenderAlgae : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    val noise = simplexNoise()

    val algaeIcons = spriteSet { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_algae_$idx") }
    val algaeModels = modelSet(64) { idx -> RenderGrass.grassTopQuads(Config.algae.heightMin, Config.algae.heightMax)(idx) }

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled && Config.algae.enabled &&
        ctx.state(up2).material == Material.WATER &&
        ctx.state(up1).material == Material.WATER &&
        DIRT_BLOCKS.contains(ctx.state.block) &&
        ctx.biome?.category
            .let { it == Biome.Category.OCEAN || it == Biome.Category.BEACH || it == Biome.Category.RIVER } &&
            noise[ctx.pos] < Config.algae.population

    override fun render(ctx: CombinedContext) {
        ctx.render()
        if (!ctx.isCutout) return
        val rand = ctx.semiRandomArray(3)
        ShadersModIntegration.grass(ctx, Config.algae.shaderWind) {
            ctx.render(
                algaeModels[rand[2]],
                icon = { _, qi, _ -> algaeIcons[rand[qi and 1]] }
            )
        }
    }
}