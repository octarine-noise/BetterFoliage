package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.data.IModelData
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class RenderLilypad : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

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
    val rootIcon = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_lilypad_roots_$idx") }
    val flowerIcon = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_lilypad_flower_$idx") }
    val perturbs = vectorSet(64) { modelIdx -> xzDisk(modelIdx) * Config.lilypad.hOffset }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${rootIcon.num} lilypad root textures")
        Client.log(DEBUG, "Registered ${flowerIcon.num} lilypad flower textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean =
        Config.enabled && Config.lilypad.enabled &&
        BlockConfig.lilypad.matchesClass(ctx.block)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        // render the whole block on the cutout layer
        if (!layer.isCutout) return false

        renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, null)
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
                postProcess = noPost
            )
        }

        if (rand[3] < Config.lilypad.flowerChance) modelRenderer.render(
            renderer,
            flowerModel.model,
            Rotation.identity,
            ctx.blockCenter.add(perturbs[rand[4]]),
            forceFlat = true,
            icon = { _, _, _ -> flowerIcon[rand[0]]!! },
            postProcess = noPost
        )

        return true
    }
}