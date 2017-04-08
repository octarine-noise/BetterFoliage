package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.*
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraftforge.common.util.ForgeDirection.UP
import org.apache.logging.log4j.Level

class RenderReeds : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val noise = simplexNoise()
    val reedIcons = iconSet(Client.genReeds.generatedResource("${BetterFoliageMod.LEGACY_DOMAIN}:better_reed_%d"))
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
        ctx.block(up2).material == Material.air &&
        ctx.block(up1).material == Material.water &&
        Config.blocks.dirt.matchesID(ctx.block) &&
        ctx.biomeId in Config.reed.biomes &&
        noise[ctx.x, ctx.z] < Config.reed.population

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        if (renderWorldBlockBase(parent, face = alwaysRender)) return true

        val iconVar = ctx.random(1)
        ShadersModIntegration.grass(Config.reed.shaderWind) {
            modelRenderer.render(
                reedModels[ctx.random(0)],
                Rotation.identity,
                forceFlat = true,
                icon = { _, _, _ -> reedIcons[iconVar]!! },
                rotateUV = { 0 },
                postProcess = noPost
            )
        }
        return true
    }
}