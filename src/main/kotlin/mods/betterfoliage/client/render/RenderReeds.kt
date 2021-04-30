package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.client.render.lighting.FlatOffsetNoColor
import mods.octarinecore.client.resource.CenteredSprite
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.tags.BlockTags
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation

class RenderReeds : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    val noise = simplexNoise()
    val reedIcons = spriteSetTransformed(
        check = { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_reed_$idx") },
        register = { CenteredSprite(it).register(BetterFoliage.asyncPack) }
    )
    val reedModels = modelSet(64) { modelIdx ->
        val height = random(Config.reed.heightMin, Config.reed.heightMax)
        val waterline = 0.875f
        val vCutLine = 0.5 - waterline / height
        listOf(
            // below waterline
            verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.5, yTop = 0.5 + waterline)
            .setFlatShader(FlatOffsetNoColor(up1)).clampUV(minV = vCutLine),

            // above waterline
            verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.5 + waterline, yTop = 0.5 + height)
            .setFlatShader(FlatOffsetNoColor(up2)).clampUV(maxV = vCutLine)
        ).forEach {
            it.clampUV(minU = -0.25, maxU = 0.25)
            .toCross(UP) { it.move(xzDisk(modelIdx) * Config.reed.hOffset) }.addAll()
        }
    }

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled && Config.reed.enabled &&
        ctx.state(up2).material == Material.AIR &&
        ctx.state(UP).material == Material.WATER &&
        BlockTags.DIRT_LIKE.contains(ctx.state.block) &&
        ctx.biome.let { it.downfall > Config.reed.minBiomeRainfall && it.defaultTemperature >= Config.reed.minBiomeTemp } &&
        noise[ctx.pos] < Config.reed.population

    override val onlyOnCutout get() = false

    override fun render(ctx: CombinedContext) {
        ctx.render()
        if (!ctx.isCutout) return

        val iconVar = ctx.semiRandom(1)
        ShadersModIntegration.grass(ctx, Config.reed.shaderWind) {
            ctx.render(
                reedModels[ctx.semiRandom(0)],
                forceFlat = true,
                icon = { _, _, _ -> reedIcons[iconVar] }
            )
        }
    }
}