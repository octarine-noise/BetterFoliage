package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCustomColors
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.betterfoliage.client.texture.GrassRegistry
import mods.octarinecore.client.render.*
import mods.octarinecore.common.*
import mods.octarinecore.random
import net.minecraft.block.Block
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.tags.BlockTags
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.data.IModelData
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class RenderGrass : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

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

    override fun isEligible(ctx: BlockContext) =
        Config.enabled &&
        (Config.shortGrass.grassEnabled || Config.connectedGrass.enabled) &&
        GrassRegistry[ctx] != null

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        // render the whole block on the cutout layer
        if (!layer.isCutout) return false

        val isConnected = BlockTags.DIRT_LIKE.contains(ctx.block(down1)) || GrassRegistry[ctx, down1] != null
        val isSnowed = ctx.blockState(up1).isSnow
        val connectedGrass = isConnected && Config.connectedGrass.enabled && (!isSnowed || Config.connectedGrass.snowEnabled)

        val grass = GrassRegistry[ctx]
        if (grass == null) {
            // shouldn't happen
            Client.logRenderError(ctx.blockState(Int3.zero), ctx.pos)
            return renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, null)
        }
        val blockColor = OptifineCustomColors.getBlockColor(ctx)

        if (connectedGrass) {
            // get full AO data
            modelRenderer.updateShading(Int3.zero, allFaces)

            // check occlusion
            val isVisible = forgeDirs.map { ctx.shouldSideBeRendered(it) }

            // render full grass block
            ShadersModIntegration.renderAs(ctx.blockState(Int3.zero), renderer) {
                modelRenderer.render(
                    renderer,
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
            }
        } else {
            renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, null)

            // get AO data only for block top
            modelRenderer.updateShading(Int3.zero, topOnly)
        }

        if (!Config.shortGrass.grassEnabled) return true
        if (isSnowed && !Config.shortGrass.snowEnabled) return true
        if (ctx.isNormalCube(up1)) return true
        if (Config.shortGrass.population < 64 && noise[ctx.pos] >= Config.shortGrass.population) return true

        // render grass quads
        val iconset = if (isSnowed) snowedIcons else normalIcons
        val iconGen = if (isSnowed) snowedGenIcon else normalGenIcon
        val rand = ctx.semiRandomArray(2)

        ShadersModIntegration.grass(renderer, Config.shortGrass.shaderWind) {
            modelRenderer.render(
                renderer,
                grassModels[rand[0]],
                Rotation.identity,
                ctx.blockCenter + (if (isSnowed) snowOffset else Double3.zero),
                icon = { _, qi, _ -> if (Config.shortGrass.useGenerated) iconGen.icon!! else iconset[rand[qi and 1]]!! },
                postProcess = { _, _, _, _, _ -> if (isSnowed) setGrey(1.0f) else multiplyColor(grass.overrideColor ?: blockColor) }
            )
        }

        return true
    }
}