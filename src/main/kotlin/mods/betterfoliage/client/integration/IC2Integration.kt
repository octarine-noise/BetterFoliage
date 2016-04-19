package mods.betterfoliage.client.integration

import cpw.mods.fml.common.Loader
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.config.SimpleBlockMatcher
import mods.octarinecore.client.render.Axis
import net.minecraft.block.Block
import org.apache.logging.log4j.Level

/**
 * Integration for IC2 experimental
 */
@SideOnly(Side.CLIENT)
object IC2Integration {

    @JvmStatic val vanillaLogAxis = Client.logRenderer.axisFunc
    @JvmStatic val isAvailable = Loader.isModLoaded("IC2")

    val ic2Logs = object : SimpleBlockMatcher() {
        override fun matchesClass(block: Block) = Config.blocks.logs.matchesClass(block) &&
            block.javaClass.name.equals("ic2.core.block.BlockRubWood")
    }

    init {
        if (isAvailable) {
            Client.log(Level.INFO, "IndustrialCraft 2 found - setting up compatibility")

            // patch axis detection for log blocks to support IC2 logs
            Client.logRenderer.axisFunc = { block: Block, meta: Int ->
                if (ic2Logs.matchesID(block)) Axis.Y
                else TFCIntegration.vanillaLogAxis(block, meta)
            }
        }
    }
}