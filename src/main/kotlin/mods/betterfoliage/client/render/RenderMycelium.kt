package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.init.Blocks
import org.apache.logging.log4j.Level.INFO

class RenderMycelium : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val myceliumIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "better_mycel_%d")
    val myceliumModel = modelSet(64, RenderGrass.grassTopQuads)

    override fun afterStitch() {
        Client.log(INFO, "Registered ${myceliumIcon.num} mycelium textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean {
        if (!Config.enabled || !Config.shortGrass.myceliumEnabled) return false
        return ctx.block == Blocks.mycelium &&
        ctx.cameraDistance < Config.shortGrass.distance
    }

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        val isSnowed = ctx.block(up1).material.let {
            it == Material.snow || it == Material.craftedSnow
        }

        if (renderWorldBlockBase(parent, face = alwaysRender)) return true
        if (isSnowed && !Config.shortGrass.snowEnabled) return true
        if (ctx.block(up1).isOpaqueCube) return true

        val rand = ctx.semiRandomArray(2)
        modelRenderer.render(
            myceliumModel[rand[0]],
            Rotation.identity,
            ctx.blockCenter + (if (isSnowed) snowOffset else Double3.zero),
            icon = { ctx, qi, q -> myceliumIcon[rand[qi and 1]]!! },
            rotateUV = { 0 },
            postProcess = if (isSnowed) whitewash else noPost
        )

        return true
    }
}