package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.render.brightnessComponents
import org.apache.logging.log4j.Level.*

/**
 * Integration for Colored Lights Core.
 */
object CLCIntegration {

    init {
        if (Refs.CLCLoadingPlugin.element != null) {
            Client.log(INFO, "Colored Lights Core integration enabled")
            brightnessComponents = listOf(4, 8, 12, 16, 20)
        }
    }
}