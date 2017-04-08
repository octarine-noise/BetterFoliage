package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraftforge.common.util.ForgeDirection.UP
import org.apache.logging.log4j.Level.INFO

class RenderCoral : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val noise = simplexNoise()

    val coralIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "better_coral_%d")
    val crustIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "better_crust_%d")
    val coralModels = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.0, yTop = 1.0)
        .scale(Config.coral.size).move(0.5 to UP)
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.coral.hOffset) }.addAll()

        val separation = random(0.01, Config.coral.vOffset)
        horizontalRectangle(x1 = -0.5, x2 = 0.5, z1 = -0.5, z2 = 0.5, y = 0.0)
        .scale(Config.coral.crustSize).move(0.5 + separation to UP).add()

        transformQ {
            it.setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
            .setFlatShader(faceOrientedAuto(overrideFace = UP, corner = cornerFlat))
        }
    }

    override fun afterStitch() {
        Client.log(INFO, "Registered ${coralIcons.num} algae textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.coral.enabled &&
        ctx.cameraDistance < Config.coral.distance &&
        (ctx.block(up2).material == Material.water || Config.coral.shallowWater) &&
        ctx.block(up1).material == Material.water &&
        Config.blocks.sand.matchesID(ctx.block) &&
        ctx.biomeId in Config.coral.biomes &&
        noise[ctx.x, ctx.z] < Config.coral.population

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        if (renderWorldBlockBase(parent, face = alwaysRender)) return true

        forgeDirs.forEachIndexed { idx, face ->
            if (!ctx.block(forgeDirOffsets[idx]).isOpaqueCube && blockContext.random(idx) < Config.coral.chance) {
                var variation = blockContext.random(6)
                modelRenderer.render(
                    coralModels[variation++],
                    rotationFromUp[idx],
                    icon = { _, qi, _ -> if (qi == 4) crustIcons[variation]!! else coralIcons[variation + (qi and 1)]!!},
                    rotateUV = { 0 },
                    postProcess = noPost
                )
            }
        }

        return true
    }
}