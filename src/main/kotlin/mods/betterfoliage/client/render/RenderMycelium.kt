package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.client.render.noPost
import mods.octarinecore.common.Double3
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.DEBUG

class RenderMycelium : RenderDecorator(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val myceliumIcon = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_mycel_$idx") }
    val myceliumModel = modelSet(64) { idx -> RenderGrass.grassTopQuads(Config.shortGrass.heightMin, Config.shortGrass.heightMax)(idx) }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${myceliumIcon.num} mycelium textures")
    }

    override fun isEligible(ctx: CombinedContext): Boolean {
        if (!Config.enabled || !Config.shortGrass.myceliumEnabled) return false
        return BlockConfig.mycelium.matchesClass(ctx.state.block)
    }

    override fun render(ctx: CombinedContext) {
        ctx.render()
        if (!ctx.isCutout) return

        val isSnowed = ctx.state(UP).isSnow
        if (isSnowed && !Config.shortGrass.snowEnabled) return
        if (ctx.offset(UP).isNormalCube) return
        val rand = ctx.semiRandomArray(2)

        ctx.render(
            myceliumModel[rand[0]],
            translation = ctx.blockCenter + (if (isSnowed) snowOffset else Double3.zero),
            icon = { _, qi, _ -> myceliumIcon[rand[qi and 1]]!! },
            postProcess = if (isSnowed) whitewash else noPost
        )
    }
}