package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.HalfBakedQuad
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelRenderer
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

    override fun renderQuad(quad: HalfBakedQuad) {
        vertexLighter.updateLightmapAndColor(quad, lightingData)
        buffer.addQuad(
            matrixStack.last, quad.baked,
            lightingData.colorMultiplier,
            lightingData.tint[0], lightingData.tint[1], lightingData.tint[2],
            lightingData.packedLight, combinedOverlay, true
        )
    }

    companion object {
        @JvmStatic
        fun render(
            renderer: BlockModelRenderer,
            world: ILightReader,
            model: SpecialRenderModel,
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

            }
            model.render(ctx, false)
            return ctx.hasRendered
        }


    }
}
