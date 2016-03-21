package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.forgeDirOffsets
import mods.octarinecore.common.forgeDirs
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.Axis
import net.minecraft.util.EnumFacing.UP
import org.apache.logging.log4j.Level.INFO

class RenderCoral : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val noise = simplexNoise()

    val coralIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_coral_%d")
    val crustIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_crust_%d")
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
        Client.log(INFO, "Registered ${coralIcons.num} coral textures")
        Client.log(INFO, "Registered ${crustIcons.num} coral crust textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.coral.enabled &&
        ctx.cameraDistance < Config.coral.distance &&
        ctx.blockState(up2).material == Material.water &&
        ctx.blockState(up1).material == Material.water &&
        Config.blocks.sand.matchesID(ctx.block) &&
        ctx.biomeId in Config.coral.biomes &&
        noise[ctx.pos] < Config.coral.population

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        renderWorldBlockBase(ctx, dispatcher, renderer, null)
        modelRenderer.updateShading(Int3.zero, allFaces)

        forgeDirs.forEachIndexed { idx, face ->
            if (!ctx.blockState(forgeDirOffsets[idx]).isOpaqueCube && blockContext.random(idx) < Config.coral.chance) {
                var variation = blockContext.random(6)
                modelRenderer.render(
                    renderer,
                    coralModels[variation++],
                    rotationFromUp[idx],
                    icon = { ctx, qi, q -> if (qi == 4) crustIcons[variation]!! else coralIcons[variation + (qi and 1)]!!},
                    rotateUV = { 0 },
                    postProcess = noPost
                )
            }
        }

        return true
    }
}