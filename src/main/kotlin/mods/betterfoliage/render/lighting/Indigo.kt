package mods.betterfoliage.render.lighting

import mods.betterfoliage.util.YarnHelper
import mods.betterfoliage.util.get
import mods.betterfoliage.util.reflectField
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator
import net.fabricmc.fabric.impl.client.indigo.renderer.render.*
import net.minecraft.block.BlockState
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

val AbstractQuadRenderer_blockInfo2 = YarnHelper.requiredField<TerrainBlockRenderInfo>(
    "net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer",
    "blockInfo", "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/TerrainBlockRenderInfo;"
)
val AbstractQuadRenderer_bufferFunc2 = YarnHelper.requiredField<java.util.function.Function<RenderLayer, VertexConsumer>>(
    "net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer",
    "bufferFunc", "Ljava/util/function/Function;"
)
val AbstractQuadRenderer_aoCalc = YarnHelper.requiredField<AoCalculator>(
    "net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer",
    "aoCalc", "Lnet/fabricmc/fabric/impl/client/indigo/renderer/aocalc/AoCalculator;"
)
val AbstractQuadRenderer_transform = YarnHelper.requiredField<RenderContext.QuadTransform>(
    "net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer",
    "transform", "Lnet/fabricmc/fabric/api/renderer/v1/render/RenderContext\$QuadTransform;"
)

val MODIFIED_CONSUMER_POOL = ThreadLocal<ModifiedTerrainMeshConsumer>()

fun AbstractMeshConsumer.modified() = MODIFIED_CONSUMER_POOL.get() ?: let {
    ModifiedTerrainMeshConsumer(this)
}.apply { MODIFIED_CONSUMER_POOL.set(this) }

/**
 * Render the given model at the given position.
 * Mutates the state of the [RenderContext]!!
 */
fun RenderContext.renderMasquerade(model: BakedModel, blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) = when(this) {
    is TerrainRenderContext -> {
        val blockInfo = meshConsumer()[AbstractQuadRenderer_blockInfo2]!!
        blockInfo.prepareForBlock(state, pos, model.useAmbientOcclusion())
        (model as FabricBakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context)
    }
    else -> {
        (model as FabricBakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context)
    }
}

/** Execute the provided block with a mesh consumer using the given custom lighting. */
fun RenderContext.withLighting(lighter: CustomLighting, func: (Consumer<Mesh>)->Unit) = when(this) {
    is TerrainRenderContext -> {
        val consumer = (meshConsumer() as AbstractMeshConsumer).modified()
        consumer.clearLighting()
        consumer.lighter = lighter
        func(consumer)
        consumer.lighter = null
    }
    else -> func(meshConsumer())
}

/** Get the [BufferBuilder] responsible for a given [BlockRenderLayer] */
fun RenderContext.getBufferBuilder(layer: RenderLayer) = when(this) {
    is TerrainRenderContext -> {
        val bufferFunc = meshConsumer()[AbstractQuadRenderer_bufferFunc2]!!
        bufferFunc.apply(layer)
    }
    else -> null
}