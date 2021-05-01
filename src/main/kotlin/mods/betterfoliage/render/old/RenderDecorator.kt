@file:JvmName("RendererHolder")
package mods.betterfoliage.render.old

import mods.betterfoliage.resource.ResourceHandler
import net.minecraft.block.BlockState
import net.minecraftforge.eventbus.api.IEventBus

abstract class RenderDecorator(modId: String, modBus: IEventBus) : ResourceHandler(modId, modBus) {

    open val renderOnCutout: Boolean get() = true
    open val onlyOnCutout: Boolean get() = false

    // ============================
    // Custom rendering
    // ============================
    abstract fun isEligible(ctx: CombinedContext): Boolean
    abstract fun render(ctx: CombinedContext)

}

data class BlockData(val state: BlockState, val color: Int, val brightness: Int)


