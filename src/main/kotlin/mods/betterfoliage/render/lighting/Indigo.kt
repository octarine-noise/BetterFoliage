package mods.betterfoliage.render.lighting

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import mods.betterfoliage.util.reflectField
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessBufferBuilder
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator
import net.fabricmc.fabric.impl.client.indigo.renderer.render.*
import net.minecraft.block.BlockRenderLayer
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ExtendedBlockView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

val MODIFIED_CONSUMER_POOL = ThreadLocal<ModifiedTerrainMeshConsumer>()

fun TerrainMeshConsumer.modified() = MODIFIED_CONSUMER_POOL.get() ?: let {
    val blockInfo = reflectField<TerrainBlockRenderInfo>("blockInfo")
    val chunkInfo = reflectField<ChunkRenderInfo>("chunkInfo")
    val aoCalc = reflectField<AoCalculator>("aoCalc")
    val transform = reflectField<RenderContext.QuadTransform>("transform")
    ModifiedTerrainMeshConsumer(blockInfo, chunkInfo, aoCalc, transform)
}.apply { MODIFIED_CONSUMER_POOL.set(this) }

val TerrainRenderContext_blockInfo = TerrainRenderContext::class.java.declaredFields.find { it.name == "blockInfo" }?.apply { isAccessible = true }
val BlockRenderInfo_layerIndexOrDefault = BlockRenderInfo::class.java.declaredMethods.find { it.name == "layerIndexOrDefault" }?.apply { isAccessible = true }
val AbstractQuadRenderer_bufferFunc = AbstractQuadRenderer::class.java.declaredFields.find { it.name == "bufferFunc" }?.apply { isAccessible = true }

/**
 * Render the given model at the given position.
 * Mutates the state of the [RenderContext]!!
 */
fun RenderContext.renderMasquerade(model: BakedModel, blockView: ExtendedBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) = when(this) {
    is TerrainRenderContext -> {
        val blockInfo = TerrainRenderContext_blockInfo!!.get(this) as BlockRenderInfo
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
        val consumer = (meshConsumer() as TerrainMeshConsumer).modified()
        consumer.clearLighting()
        consumer.lighter = lighter
        func(consumer)
        consumer.lighter = null
    }
    else -> func(meshConsumer())
}

/** Get the [BufferBuilder] responsible for a given [BlockRenderLayer] */
fun RenderContext.getBufferBuilder(layer: BlockRenderLayer) = when(this) {
    is TerrainRenderContext -> {
        val blockInfo = TerrainRenderContext_blockInfo!!.get(this) as BlockRenderInfo
        val layerIdx = BlockRenderInfo_layerIndexOrDefault!!.invoke(blockInfo, layer) as Int
        val bufferFunc = AbstractQuadRenderer_bufferFunc!!.get(meshConsumer()) as Int2ObjectFunction<AccessBufferBuilder>
        bufferFunc[layerIdx]
    }
    else -> null
}