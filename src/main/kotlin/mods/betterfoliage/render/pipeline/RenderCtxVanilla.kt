package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.render.ISpecialRenderModel
import mods.betterfoliage.render.lighting.VanillaFullBlockLighting
import mods.betterfoliage.render.lighting.VanillaVertexLighter
import mods.betterfoliage.render.lighting.VanillaQuadLighting
import mods.betterfoliage.render.old.HalfBakedQuad
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.ThreadLocalDelegate
import mods.betterfoliage.util.plus
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelRenderer
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraftforge.client.model.data.IModelData
import java.util.Random

class RenderCtxVanilla(
    val renderer: BlockModelRenderer,
    world: ILightReader,
    pos: BlockPos,
    val buffer: IVertexBuilder,
    val combinedOverlay: Int,
    matrixStack: MatrixStack,
    checkSides: Boolean,
    random: Random,
    val seed: Long,
    modelData: IModelData,
    val useAO: Boolean
): RenderCtxBase(world, pos, matrixStack, checkSides, random, modelData) {

    private val blockColors = renderer.blockColors
    var vertexLighter: VanillaVertexLighter = VanillaFullBlockLighting

    override fun renderQuad(quad: HalfBakedQuad) {
        lightingData.let { lighting ->
            vertexLighter.updateLightmapAndColor(quad, lighting)
            buffer.addQuad(
                matrixStack.last, quad.baked,
                lighting.colorMultiplier,
                lighting.tint[0], lighting.tint[1], lighting.tint[2],
                lighting.packedLight, combinedOverlay, true
            )
        }
    }

    override fun renderFallback(model: IBakedModel) {
        if (useAO) renderer.renderModelSmooth(world, model, state, pos, matrixStack, buffer, checkSides, random, seed, combinedOverlay, modelData)
        else renderer.renderModelFlat(world, model, state, pos, matrixStack, buffer, checkSides, random, seed, combinedOverlay, modelData)
    }

    override fun renderMasquerade(offset: Int3, func: () -> Unit) {
        lightingData.calc.blockPos += offset
        func()
        lightingData.calc.blockPos = pos
    }

    companion object {
        @JvmStatic
        fun render(
            renderer: BlockModelRenderer,
            world: ILightReader,
            model: ISpecialRenderModel,
            state: BlockState,
            pos: BlockPos,
            matrixStack: MatrixStack,
            buffer: IVertexBuilder,
            checkSides: Boolean,
            random: Random,
            rand: Long,
            combinedOverlay: Int,
            modelData: IModelData,
            smooth: Boolean
        ): Boolean {
            random.setSeed(rand)
            val ctx = RenderCtxVanilla(renderer, world, pos, buffer, combinedOverlay, matrixStack, checkSides, random, rand, modelData, smooth)
            lightingData.apply {
                calc.reset(ctx)
                blockColors = renderer.blockColors
            }
            model.render(ctx, false)
            return ctx.hasRendered
        }

        val lightingData by ThreadLocalDelegate { VanillaQuadLighting() }
    }
}
