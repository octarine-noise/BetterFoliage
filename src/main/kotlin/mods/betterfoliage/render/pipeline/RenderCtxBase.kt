package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.model.HalfBakedQuad
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.render.lighting.VanillaFullBlockLighting
import mods.betterfoliage.render.lighting.VanillaQuadLighting
import mods.betterfoliage.render.lighting.VanillaVertexLighter
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.plus
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.model.data.IModelData
import java.util.Random

/**
 * Rendering context for drawing [SpecialRenderModel] models.
 *
 * This class (and others in its constellation) basically form a replacement, highly customizable,
 * push-based partial rendering pipeline for [SpecialRenderModel] instances.
 */
abstract class RenderCtxBase(
    blockCtx: BlockCtx,
    val matrixStack: MatrixStack,
    var checkSides: Boolean,
    val random: Random,
    val modelData: IModelData,
) : BlockCtx by blockCtx {

    var hasRendered = false
    var modelRenderData: Any? = null
    inline fun <reified T> withRenderData(renderFunc: (T)->Boolean) = (modelRenderData as? T?).let {
        if (it == null) false else renderFunc(it)
    }

    val blockModelShapes = Minecraft.getInstance().blockRenderer.blockModelShaper
    var vertexLighter: VanillaVertexLighter = VanillaFullBlockLighting
    protected val lightingData = RenderCtxBase.lightingData.get().apply {
        calc.reset(this@RenderCtxBase)
        blockColors = Minecraft.getInstance().blockColors
    }

    abstract fun renderQuad(quad: HalfBakedQuad)

    inline fun Direction?.shouldRender() = this == null || !checkSides || Block.shouldRenderFace(state, world, pos, this)

    fun renderQuads(quads: Iterable<HalfBakedQuad>) {
        quads.forEach { quad ->
            if (quad.raw.face.shouldRender()) {
                renderQuad(quad)
                hasRendered = true
            }
        }
    }

    fun renderMasquerade(offset: Int3, func: () -> Unit) {
        lightingData.calc.blockPos += offset
        func()
        lightingData.calc.blockPos = pos
    }

    companion object {
        @JvmStatic
        fun reset(chunkRenderCache: ChunkRenderCache, blockRendererDispatcher: BlockRendererDispatcher, pos: BlockPos, random: Random) {
            // prepare render data
            val blockCtx = BasicBlockCtx(chunkRenderCache, pos)
            val model = blockRendererDispatcher.getBlockModel(blockCtx.state)
            random.setSeed(blockCtx.seed)
            val data = if (model is SpecialRenderModel) model.prepare(blockCtx, random) else Unit
            specialRenderData.set(data)
        }

        val lightingData = ThreadLocal.withInitial { VanillaQuadLighting() }
        val specialRenderData = ThreadLocal<Any?>()
    }
}