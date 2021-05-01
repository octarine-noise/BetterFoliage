package mods.betterfoliage.render.block.vanillaold

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.integration.OptifineCustomColors
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.render.DIRT_BLOCKS
import mods.betterfoliage.render.down1
import mods.betterfoliage.render.isSnow
import mods.betterfoliage.resource.Identifier
import mods.betterfoliage.resource.generated.GeneratedGrass
import mods.betterfoliage.texture.GrassRegistry
import mods.betterfoliage.render.old.Model
import mods.betterfoliage.render.old.RenderDecorator
import mods.betterfoliage.render.old.fullCube
import mods.betterfoliage.render.lighting.cornerAo
import mods.betterfoliage.render.lighting.cornerFlat
import mods.betterfoliage.render.lighting.faceOrientedAuto
import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.render.snowOffset
import mods.betterfoliage.render.toCross
import mods.betterfoliage.render.xzDisk
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.randomD
import net.minecraft.util.Direction.*

class RenderGrass : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    companion object {
        @JvmStatic fun grassTopQuads(heightMin: Double, heightMax: Double): Model.(Int)->Unit = { modelIdx ->
            verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.5,
                    yTop = 0.5 + randomD(heightMin, heightMax)
            )
            .setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
            .setFlatShader(faceOrientedAuto(overrideFace = UP, corner = cornerFlat))
            .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()
        }
    }

    val noise = simplexNoise()

    val normalIcons = spriteSet { idx -> Identifier(BetterFoliageMod.MOD_ID, "blocks/better_grass_long_$idx") }
    val snowedIcons = spriteSet { idx -> Identifier(BetterFoliageMod.MOD_ID, "blocks/better_grass_snowed_$idx") }
    val normalGenIcon by sprite { GeneratedGrass(sprite = "minecraft:blocks/tall_grass_top", isSnowed = false).register(BetterFoliage.asyncPack) }
    val snowedGenIcon by sprite { GeneratedGrass(sprite = "minecraft:blocks/tall_grass_top", isSnowed = true).register(BetterFoliage.asyncPack) }

    val grassModels = modelSet(64) { idx -> grassTopQuads(Config.shortGrass.heightMin, Config.shortGrass.heightMax)(idx) }

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled &&
        (Config.shortGrass.grassEnabled || Config.connectedGrass.enabled) &&
        GrassRegistry[ctx] != null

    override val onlyOnCutout get() = true

    override fun render(ctx: CombinedContext) {
        val isConnected = DIRT_BLOCKS.contains(ctx.state(DOWN).block) || GrassRegistry[ctx, down1] != null
        val isSnowed = ctx.state(UP).isSnow
        val connectedGrass = isConnected && Config.connectedGrass.enabled && (!isSnowed || Config.connectedGrass.snowEnabled)

        val grass = GrassRegistry[ctx]!!
        val blockColor = OptifineCustomColors.getBlockColor(ctx)

        if (connectedGrass) {
            // check occlusion
            val isVisible = allDirections.map { ctx.shouldSideBeRendered(it) }

            // render full grass block
            ctx.render(
                fullCube,
                quadFilter = { qi, _ -> isVisible[qi] },
                icon = { _, _, _ -> grass.grassTopTexture },
                postProcess = { ctx, _, _, _, _ ->
                    rotateUV(2)
                    if (isSnowed) {
                        if (!ctx.aoEnabled) setGrey(1.4f)
                    } else if (ctx.aoEnabled && grass.overrideColor == null) multiplyColor(blockColor)
                }
            )
        } else {
            ctx.render()
        }

        if (!Config.shortGrass.grassEnabled) return
        if (isSnowed && !Config.shortGrass.snowEnabled) return
        if (ctx.offset(UP).isNormalCube) return
        if (Config.shortGrass.population < 64 && noise[ctx.pos] >= Config.shortGrass.population) return

        // render grass quads
        val iconset = if (isSnowed) snowedIcons else normalIcons
        val iconGen = if (isSnowed) snowedGenIcon else normalGenIcon
        val rand = ctx.semiRandomArray(2)

        ShadersModIntegration.grass(ctx, Config.shortGrass.shaderWind) {
            ctx.render(
                grassModels[rand[0]],
                translation = ctx.blockCenter + (if (isSnowed) snowOffset else Double3.zero),
                icon = { _, qi, _ -> if (Config.shortGrass.useGenerated) iconGen else iconset[rand[qi and 1]] },
                postProcess = { _, _, _, _, _ -> if (isSnowed) setGrey(1.0f) else multiplyColor(grass.overrideColor ?: blockColor) }
            )
        }
    }
}