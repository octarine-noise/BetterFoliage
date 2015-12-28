package mods.betterfoliage.client.integration

import cpw.mods.fml.common.Loader
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.Client
import mods.octarinecore.client.render.Axis
import net.minecraft.block.Block
import org.apache.logging.log4j.Level

/**
 * Integration for TerraFirmaCraft
 */
@SideOnly(Side.CLIENT)
object TFCIntegration {
    @JvmStatic val vanillaLogAxis = Client.logRenderer.axisFunc

    init {
        if (Loader.isModLoaded("terrafirmacraft")) {
            Client.log(Level.INFO, "TerraFirmaCraft found - setting up compatibility")

            // patch axis detection for log blocks to support TFC logs
            Client.logRenderer.axisFunc = { block: Block, meta: Int ->
                block.javaClass.name.let {
                    if (it.startsWith("com.bioxx.tfc")) {
                        if (it.contains("Horiz"))
                            if (meta shr 3 == 0) Axis.Z else Axis.X
                        else
                            Axis.Y
                    } else {
                        vanillaLogAxis(block, meta)
                    }
                }
            }
        }
    }
}
