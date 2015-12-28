package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import net.minecraft.client.renderer.RenderBlocks
import net.minecraftforge.common.util.ForgeDirection.*
import org.apache.logging.log4j.Level

class RenderCactus : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val cactusStemRadius = 0.4375
    val cactusArmRotation = listOf(NORTH, SOUTH, EAST, WEST).map { Rotation.rot90[it.ordinal] }

    val iconCross = iconStatic(BetterFoliageMod.LEGACY_DOMAIN, "better_cactus")
    val iconArm = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "better_cactus_arm_%d")

    val modelStem = model {
        horizontalRectangle(x1 = -cactusStemRadius, x2 = cactusStemRadius, z1 = -cactusStemRadius, z2 = cactusStemRadius, y = 0.5)
        .scaleUV(cactusStemRadius * 2.0)
        .let { listOf(it.flipped.move(1.0 to DOWN), it) }
        .forEach { it.setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y), edge = null)).add() }

        verticalRectangle(x1 = -0.5, z1 = cactusStemRadius, x2 = 0.5, z2 = cactusStemRadius, yBottom = -0.5, yTop = 0.5)
        .setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y), edge = null))
        .toCross(UP).addAll()
    }
    val modelCross = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
        .setAoShader(edgeOrientedAuto(corner = cornerAoMaxGreen))
        .scale(1.4)
        .transformV { v ->
            val perturb = xzDisk(modelIdx) * Config.cactus.sizeVariation
            Vertex(v.xyz + (if (v.uv.u < 0.0) perturb else -perturb), v.uv, v.aoShader)
        }
        .toCross(UP).addAll()
    }
    val modelArm = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.0, yTop = 1.0)
        .scale(Config.cactus.size).move(0.5 to UP)

        .setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y), edge = null))
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.cactus.hOffset) }.addAll()
    }

    override fun afterStitch() {
        Client.log(Level.INFO, "Registered ${iconArm.num} cactus arm textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean =
        Config.enabled && Config.cactus.enabled &&
        ctx.cameraDistance < Config.cactus.distance &&
        Config.blocks.cactus.matchesID(ctx.block)

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        // get AO data
        if (renderWorldBlockBase(parent, face = neverRender)) return true

        modelRenderer.render(
            modelStem.model,
            Rotation.identity,
            icon = { ctx, qi, q -> ctx.icon(forgeDirs[qi])},
            rotateUV = { 0 },
            postProcess = noPost
        )
        modelRenderer.render(
            modelCross[ctx.random(0)],
            Rotation.identity,
            icon = { ctx, qi, q -> iconCross.icon!!},
            rotateUV = { 0 },
            postProcess = noPost
        )
        modelRenderer.render(
            modelArm[ctx.random(1)],
            cactusArmRotation[ctx.random(2) % 4],
            icon = { ctx2, qi, q -> iconArm[ctx.random(3)]!!},
            rotateUV = { 0 },
            postProcess = noPost
        )
        return true
    }
}