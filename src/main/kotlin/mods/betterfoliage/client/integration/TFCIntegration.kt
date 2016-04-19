package mods.betterfoliage.client.integration

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.EventPriority
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockMatcher
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.config.SimpleBlockMatcher
import mods.octarinecore.client.render.Axis
import net.minecraft.block.Block
import net.minecraft.client.multiplayer.WorldClient
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import org.apache.logging.log4j.Level

/**
 * Integration for TerraFirmaCraft
 */
@SideOnly(Side.CLIENT)
object TFCIntegration {

    @JvmStatic val vanillaLogAxis = Client.logRenderer.axisFunc
    @JvmStatic val isAvailable = Loader.isModLoaded("terrafirmacraft")

    val horizontalLogs = object : SimpleBlockMatcher() {
        override fun matchesClass(block: Block) = Config.blocks.logs.matchesClass(block) &&
            block.javaClass.name.let { it.startsWith("com.bioxx.tfc") && it.contains("Horiz") }
    }
    val verticalLogs = object : SimpleBlockMatcher() {
        override fun matchesClass(block: Block) = Config.blocks.logs.matchesClass(block) &&
            block.javaClass.name.let { it.startsWith("com.bioxx.tfc") && !it.contains("Horiz") }
    }
    val grass = object : SimpleBlockMatcher() {
        override fun matchesClass(block: Block) = Config.blocks.grass.matchesClass(block) &&
            block.javaClass.name.let{ it.startsWith("com.bioxx.tfc") }
    }

    init {
        if (isAvailable) {
            Client.log(Level.INFO, "TerraFirmaCraft found - setting up compatibility")

            // patch axis detection for log blocks to support TFC logs
            Client.logRenderer.axisFunc = { block: Block, meta: Int ->
                if (horizontalLogs.matchesID(block)) { if (meta shr 3 == 0) Axis.Z else Axis.X }
                else if (verticalLogs.matchesID(block)) Axis.Y
                else vanillaLogAxis(block, meta)
            }
        }
    }
}
