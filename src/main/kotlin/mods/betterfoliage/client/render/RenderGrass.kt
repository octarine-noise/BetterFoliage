package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCustomColors
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.betterfoliage.client.texture.GrassRegistry
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.client.render.Model
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.client.render.fullCube
import mods.octarinecore.client.render.lighting.cornerAo
import mods.octarinecore.client.render.lighting.cornerFlat
import mods.octarinecore.client.render.lighting.faceOrientedAuto
import mods.octarinecore.common.Double3
import mods.octarinecore.common.allDirections
import mods.octarinecore.random
import net.minecraft.tags.BlockTags
import net.minecraft.util.Direction.*
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.DEBUG

class RenderGrass : RenderDecorator(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    companion object {
        @JvmStatic fun grassTopQuads(heightMin: Double, heightMax: Double): Model.(Int)->Unit = { modelIdx ->
            verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.5,
                    yTop = 0.5 + random(heightMin, heightMax)
            )
            .setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
            .setFlatShader(faceOrientedAuto(overrideFace = UP, corner = cornerFlat))
            .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()
        }
    }

    val noise = simplexNoise()

    val normalIcons = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_grass_long_$idx") }
    val snowedIcons = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_grass_snowed_$idx") }
    val normalGenIcon = iconStatic { Client.genGrass.register(texture = "minecraft:blocks/tallgrass", isSnowed = false) }
    val snowedGenIcon = iconStatic { Client.genGrass.register(texture = "minecraft:blocks/tallgrass", isSnowed = true) }

    val grassModels = modelSet(64) { idx -> grassTopQuads(Config.shortGrass.heightMin, Config.shortGrass.heightMax)(idx) }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${normalIcons.num} grass textures")
        Client.log(DEBUG, "Registered ${snowedIcons.num} snowed grass textures")
    }

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled &&
        (Config.shortGrass.grassEnabled || Config.connectedGrass.enabled) &&
        GrassRegistry[ctx] != null

    override val onlyOnCutout get() = true

    override fun render(ctx: CombinedContext) {
        val isConnected = BlockTags.DIRT_LIKE.contains(ctx.state(DOWN).block) || GrassRegistry[ctx, down1] != null
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
                icon = { _, qi, _ -> if (Config.shortGrass.useGenerated) iconGen.icon!! else iconset[rand[qi and 1]]!! },
                postProcess = { _, _, _, _, _ -> if (isSnowed) setGrey(1.0f) else multiplyColor(grass.overrideColor ?: blockColor) }
            )
        }
    }
}