package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import org.apache.logging.log4j.Level

class RenderLilypad : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val rootModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -1.5, yTop = -0.5)
        .setFlatShader(FlatOffsetNoColor(Int3.zero))
        .toCross(UP).addAll()
    }
    val flowerModel = model {
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.0, yTop = 1.0)
        .scale(0.5).move(0.5 to DOWN)
        .setFlatShader(FlatOffsetNoColor(Int3.zero))
        .toCross(UP).addAll()
    }
    val rootIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_lilypad_roots_%d")
    val flowerIcon = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_lilypad_flower_%d")
    val perturbs = vectorSet(64) { modelIdx -> xzDisk(modelIdx) * Config.lilypad.hOffset }

    override fun afterStitch() {
        Client.log(Level.INFO, "Registered ${rootIcon.num} lilypad root textures")
        Client.log(Level.INFO, "Registered ${flowerIcon.num} lilypad flower textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean =
        Config.enabled && Config.lilypad.enabled &&
        ctx.cameraDistance < Config.lilypad.distance &&
        Config.blocks.lilypad.matchesID(ctx.block)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        renderWorldBlockBase(ctx, dispatcher, renderer, null)
        modelRenderer.updateShading(Int3.zero, allFaces)

        val rand = ctx.semiRandomArray(5)

        ShadersModIntegration.grass(renderer) {
            modelRenderer.render(
                renderer,
                rootModel.model,
                Rotation.identity,
                ctx.blockCenter.add(perturbs[rand[2]]),
                forceFlat = true,
                icon = { ctx, qi, q -> rootIcon[rand[qi and 1]]!! },
                rotateUV = { 0 },
                postProcess = noPost
            )
        }

        if (rand[3] < Config.lilypad.flowerChance) modelRenderer.render(
            renderer,
            flowerModel.model,
            Rotation.identity,
            ctx.blockCenter.add(perturbs[rand[4]]),
            forceFlat = true,
            icon = { ctx, qi, q -> flowerIcon[rand[0]]!! },
            rotateUV = { 0 },
            postProcess = noPost
        )

        return true
    }
}