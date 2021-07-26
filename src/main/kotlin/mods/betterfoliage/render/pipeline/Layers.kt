package mods.betterfoliage.render.pipeline

import mods.betterfoliage.model.SpecialRenderData
import mods.betterfoliage.RenderTypeLookup
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderType
import java.util.function.Predicate

object Layers {
    val tufts = RenderType.cutout()
    val connectedGrass = RenderType.solid()
    val connectedDirt = RenderType.cutoutMipped()
    val coral = RenderType.cutoutMipped()
}

val defaultLayerBehaviour = Predicate<RenderType> { layer -> layer == RenderType.solid() }

class WrappedLayerPredicate(val original: Predicate<RenderType>, val func: (RenderType, Predicate<RenderType>) -> Boolean) : Predicate<RenderType> {
    override fun test(layer: RenderType) = func(layer, original)
}

/**
 * Extension method to access the canRenderInLayer() predicate in [RenderTypeLookup]
 */
var Block.layerPredicate : Predicate<RenderType>?
    get() = RenderTypeLookup.blockRenderChecks.getStatic()[delegate]
    set(value) {
        RenderTypeLookup.blockRenderChecks.getStatic()[delegate] = value!!
    }

/**
 * Add a wrapper to the block's canRenderInLayer() predicate to enable dynamic multi-layer rendering.
 * If the render data for the block implements [SpecialRenderData], the layers it enables will be
 * rendered _in addition to_ the block's normal layers.
 */
fun Block.extendLayers() {
    val original = layerPredicate ?: defaultLayerBehaviour
    if (original !is WrappedLayerPredicate) layerPredicate = WrappedLayerPredicate(original) { layer, original ->
        original.test(layer) ||
        (RenderCtxBase.specialRenderData.get() as? SpecialRenderData)?.canRenderInLayer(layer) ?: false
    }
}
