package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.random
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.init.Blocks
import net.minecraftforge.common.util.ForgeDirection.DOWN
import net.minecraftforge.common.util.ForgeDirection.UP
import org.apache.logging.log4j.Level.INFO

class RenderNetherrack : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val netherrackIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "better_netherrack_%d")
    val netherrackModel = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yTop = -0.5,
        yBottom = -0.5 - random(Config.netherrack.heightMin, Config.netherrack.heightMax))
        .setAoShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerAo(Axis.Y)))
        .setFlatShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerFlat))
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()

    }

    override fun afterStitch() {
        Client.log(INFO, "Registered ${netherrackIcon.num} netherrack textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean {
        if (!Config.enabled || !Config.netherrack.enabled) return false
        return ctx.block == Blocks.netherrack &&
        ctx.cameraDistance < Config.netherrack.distance
    }

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        if (renderWorldBlockBase(parent, face = alwaysRender)) return true
        if (ctx.block(down1).isOpaqueCube) return true

        val rand = ctx.semiRandomArray(2)
        modelRenderer.render(
            netherrackModel[rand[0]],
            Rotation.identity,
            icon = { _, qi, _ -> netherrackIcon[rand[qi and 1]]!! },
            rotateUV = { 0 },
            postProcess = noPost
        )

        return true
    }
}