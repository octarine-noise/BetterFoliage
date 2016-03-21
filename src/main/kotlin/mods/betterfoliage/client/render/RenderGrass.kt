package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCTM
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.betterfoliage.client.texture.GrassRegistry
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.random
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.Axis
import net.minecraft.util.EnumFacing.UP
import org.apache.logging.log4j.Level.INFO

class RenderGrass : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    companion object {
        @JvmStatic val grassTopQuads: Model.(Int)->Unit = { modelIdx ->
            verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.5,
                    yTop = 0.5 + random(Config.shortGrass.heightMin, Config.shortGrass.heightMax)
            )
            .setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
            .setFlatShader(faceOrientedAuto(overrideFace = UP, corner = cornerFlat))
            .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()
        }
    }

    val normalIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_grass_long_%d")
    val snowedIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_grass_snowed_%d")
    val normalGenIcon = iconStatic(Client.genGrass.generatedResource("minecraft:blocks/tallgrass", "snowed" to false))
    val snowedGenIcon = iconStatic(Client.genGrass.generatedResource("minecraft:blocks/tallgrass", "snowed" to true))

    val grassModels = modelSet(64, grassTopQuads)

    override fun afterStitch() {
        Client.log(INFO, "Registered ${normalIcons.num} grass textures")
        Client.log(INFO, "Registered ${snowedIcons.num} snowed grass textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled &&
        ctx.cameraDistance < Config.shortGrass.distance &&
        (Config.shortGrass.grassEnabled || Config.connectedGrass.enabled) &&
        Config.blocks.grass.matchesID(ctx.block)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        val isConnected = ctx.block(down1).let { Config.blocks.dirt.matchesID(it) || Config.blocks.grass.matchesID(it) }
        val isSnowed = ctx.blockState(up1).isSnow
        val connectedGrass = isConnected && Config.connectedGrass.enabled && (!isSnowed || Config.connectedGrass.snowEnabled)

        val grassInfo = GrassRegistry[ctx.blockState(Int3.zero)] ?: return renderWorldBlockBase(ctx, dispatcher, renderer, layer)
        val grassTopTexture = OptifineCTM.override(grassInfo.grassTopTexture, ctx, UP)

        val blockColor = ctx.blockData(Int3.zero).color

        if (connectedGrass) {
            // get full AO data
            modelRenderer.updateShading(Int3.zero, allFaces)

            // render full grass block
            modelRenderer.render(
                renderer,
                fullCube,
                Rotation.identity,
                ctx.blockCenter,
                icon =  { ctx, qi, q -> grassTopTexture },
                rotateUV = { 2 },
                postProcess = { ctx, qi, q, vi, v ->
                    if (isSnowed) { if(!ctx.aoEnabled) setGrey(1.4f) }
                    else if (ctx.aoEnabled) multiplyColor(blockColor)
                }
            )
        } else {
            renderWorldBlockBase(ctx, dispatcher, renderer, null)

            // get AO data only for block top
            modelRenderer.updateShading(Int3.zero, topOnly)
        }

        if (!Config.shortGrass.grassEnabled) return true
        if (isSnowed && !Config.shortGrass.snowEnabled) return true
        if (ctx.blockState(up1).isOpaqueCube) return true

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
                icon = { ctx: ShadingContext, qi: Int, q: Quad ->
                    if (Config.shortGrass.useGenerated) iconGen.icon!! else iconset[rand[qi and 1]]!!
                },
                rotateUV = { 0 },
                postProcess = { ctx, qi, q, vi, v -> if (isSnowed) setGrey(1.0f) else multiplyColor(grassInfo.overrideColor ?: blockColor) }
            )
        }

        return true
    }
}