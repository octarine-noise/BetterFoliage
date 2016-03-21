package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import org.apache.logging.log4j.Level.INFO

class RenderAlgae : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val noise = simplexNoise()

    val algaeIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_algae_%d")
    val algaeModels = modelSet(64, RenderGrass.grassTopQuads)

    override fun afterStitch() {
        Client.log(INFO, "Registered ${algaeIcons.num} algae textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.algae.enabled &&
        ctx.cameraDistance < Config.algae.distance &&
        ctx.blockState(up2).material == Material.water &&
        ctx.blockState(up1).material == Material.water &&
        Config.blocks.dirt.matchesID(ctx.block) &&
        ctx.biomeId in Config.algae.biomes &&
        noise[ctx.pos] < Config.algae.population

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        renderWorldBlockBase(ctx, dispatcher, renderer, null)
        modelRenderer.updateShading(Int3.zero, allFaces)

        val rand = ctx.semiRandomArray(3)

        ShadersModIntegration.grass(renderer, Config.algae.shaderWind) {
            modelRenderer.render(
                renderer,
                algaeModels[rand[2]],
                Rotation.identity,
                icon = { ctx, qi, q -> algaeIcons[rand[qi and 1]]!! },
                rotateUV = { 0 },
                postProcess = noPost
            )
        }
        return true
    }
}