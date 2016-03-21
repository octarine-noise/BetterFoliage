package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.UP
import org.apache.logging.log4j.Level

class RenderReeds : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val noise = simplexNoise()
    val reedIcons = iconSet(Client.genReeds.generatedResource("${BetterFoliageMod.LEGACY_DOMAIN}:blocks/better_reed_%d"))
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

    override fun afterStitch() {
        Client.log(Level.INFO, "Registered ${reedIcons.num} reed textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.reed.enabled &&
        ctx.cameraDistance < Config.reed.distance &&
        ctx.blockState(up2).material == Material.air &&
        ctx.blockState(up1).material == Material.water &&
        Config.blocks.dirt.matchesID(ctx.block) &&
        ctx.biomeId in Config.reed.biomes &&
        noise[ctx.pos] < Config.reed.population

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        renderWorldBlockBase(ctx, dispatcher, renderer, null)
        modelRenderer.updateShading(Int3.zero, allFaces)

        val iconVar = ctx.random(1)
        ShadersModIntegration.grass(renderer, Config.reed.shaderWind) {
            modelRenderer.render(
                renderer,
                reedModels[ctx.random(0)],
                Rotation.identity,
                forceFlat = true,
                icon = { ctx, qi, q -> reedIcons[iconVar]!! },
                rotateUV = { 0 },
                postProcess = noPost
            )
        }
        return true
    }
}