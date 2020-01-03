package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.client.render.lighting.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.random
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.data.IModelData
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class RenderNetherrack : RenderDecorator(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val netherrackIcon = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_netherrack_$idx") }
    val netherrackModel = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yTop = -0.5,
        yBottom = -0.5 - random(Config.netherrack.heightMin, Config.netherrack.heightMax))
        .setAoShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerAo(Axis.Y)))
        .setFlatShader(faceOrientedAuto(overrideFace = DOWN, corner = cornerFlat))
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.shortGrass.hOffset) }.addAll()

    }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${netherrackIcon.num} netherrack textures")
    }

    override fun isEligible(ctx: CombinedContext): Boolean {
        if (!Config.enabled || !Config.netherrack.enabled) return false
        return BlockConfig.netherrack.matchesClass(ctx.state.block)
    }

    override fun render(ctx: CombinedContext) {
        ctx.render()
        if (!ctx.isCutout) return
        if (ctx.offset(DOWN).isNormalCube) return

        val rand = ctx.semiRandomArray(2)
        ctx.render(
            netherrackModel[rand[0]],
            icon = { _, qi, _ -> netherrackIcon[rand[qi and 1]]!! }
        )
    }
}