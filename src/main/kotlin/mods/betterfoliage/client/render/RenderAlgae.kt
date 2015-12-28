package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.*
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.init.Blocks
import org.apache.logging.log4j.Level.INFO

class RenderAlgae : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val noise = simplexNoise()

    val algaeIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "better_algae_%d")
    val algaeModels = modelSet(64, RenderGrass.grassTopQuads)

    override fun afterStitch() {
        Client.log(INFO, "Registered ${algaeIcons.num} algae textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.algae.enabled &&
        ctx.cameraDistance < Config.algae.distance &&
        ctx.block(up2).material == Material.water &&
        ctx.block(up1).material == Material.water &&
        Config.blocks.dirt.matchesID(ctx.block) &&
        ctx.biomeId in Config.algae.biomes &&
        noise[ctx.x, ctx.z] < Config.algae.population

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        if (renderWorldBlockBase(parent, face = alwaysRender)) return true

        val rand = ctx.semiRandomArray(3)

        ShadersModIntegration.grass(Config.algae.shaderWind) {
            modelRenderer.render(
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