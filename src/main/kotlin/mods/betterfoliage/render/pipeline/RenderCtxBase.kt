package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.render.lighting.VanillaFullBlockLighting
import mods.betterfoliage.render.lighting.VanillaQuadLighting
import mods.betterfoliage.render.lighting.VanillaVertexLighter
import mods.betterfoliage.model.HalfBakedQuad
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.plus
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraftforge.client.model.data.IModelData
import java.util.Random

/**
 * Rendering context for drawing [SpecialRenderModel] models.
 *
 * This class (and others in its constellation) basically form a replacement, highly customizable,
 * push-based partial rendering pipeline for [SpecialRenderModel] instances.
 */
abstract class RenderCtxBase(
    world: ILightReader,
    pos: BlockPos,
    val matrixStack: MatrixStack,
    val checkSides: Boolean,
    val random: Random,
    val modelData: IModelData
) : BlockCtx by BasicBlockCtx(world, pos) {

    abstract fun renderQuad(quad: HalfBakedQuad)

    var hasRendered = false
    val blockModelShapes = Minecraft.getInstance().blockRendererDispatcher.blockModelShapes
    var vertexLighter: VanillaVertexLighter = VanillaFullBlockLighting
    protected val lightingData = RenderCtxBase.lightingData.get().apply {
        calc.reset(this@RenderCtxBase)
        blockColors = Minecraft.getInstance().blockColors
    }

    inline fun Direction?.shouldRender() = this == null || !checkSides || Block.shouldSideBeRendered(state, world, pos, this)

    fun render(quads: Iterable<HalfBakedQuad>) {
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
        val lightingData = ThreadLocal.withInitial { VanillaQuadLighting() }
    }
}