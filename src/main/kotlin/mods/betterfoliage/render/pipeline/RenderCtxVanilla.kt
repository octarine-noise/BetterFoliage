package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.model.HalfBakedQuad
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.util.getWithDefault
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelRenderer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockDisplayReader
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.model.data.IModelData
import java.util.Random

class RenderCtxVanilla(
    val renderer: BlockModelRenderer,
    blockCtx: BlockCtx,
    val buffer: IVertexBuilder,
    val combinedOverlay: Int,
    matrixStack: MatrixStack,
    checkSides: Boolean,
    random: Random,
    val randomSeed: Long,
    modelData: IModelData,
    val useAO: Boolean
): RenderCtxBase(blockCtx, matrixStack, checkSides, random, modelData) {

    override fun renderQuad(quad: HalfBakedQuad) {
        vertexLighter.updateLightmapAndColor(quad, lightingData)
        buffer.putBulkData(
            matrixStack.last(), quad.baked,
            lightingData.colorMultiplier,
            lightingData.tint[0], lightingData.tint[1], lightingData.tint[2],
            lightingData.packedLight, combinedOverlay, true
        )
    }

    companion object {
        @JvmStatic
        fun render(
            renderer: BlockModelRenderer,
            world: IBlockDisplayReader,
            model: SpecialRenderModel,
            state: BlockState,
            pos: BlockPos,
            matrixStack: MatrixStack,
            buffer: IVertexBuilder,
            checkSides: Boolean,
            random: Random,
            seed: Long,
            combinedOverlay: Int,
            modelData: IModelData,
            smooth: Boolean
        ): Boolean {
            val blockCtx = BasicBlockCtx(world, pos)
            // init context if missing (this is the first render layer)
            val ctx = RenderCtxVanilla(renderer, blockCtx, buffer, combinedOverlay, matrixStack, checkSides, random, seed, modelData, smooth)
            model.renderLayer(ctx, specialRenderData.get()!!, MinecraftForgeClient.getRenderLayer())
            return ctx.hasRendered
        }
    }
}
