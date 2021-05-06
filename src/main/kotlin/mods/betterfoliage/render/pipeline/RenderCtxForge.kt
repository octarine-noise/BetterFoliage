package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import mods.betterfoliage.render.ISpecialRenderModel
import mods.betterfoliage.render.lighting.ForgeVertexLighter
import mods.betterfoliage.render.lighting.ForgeVertexLighterAccess
import mods.betterfoliage.render.old.HalfBakedQuad
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.directionsAndNull
import mods.betterfoliage.util.get
import mods.octarinecore.VertexLighterFlat_blockInfo
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraftforge.client.model.data.IModelData
import net.minecraftforge.client.model.pipeline.VertexLighterFlat
import java.util.Random

class RenderCtxForge(
    world: ILightReader,
    pos: BlockPos,
    val lighter: VertexLighterFlat,
    matrixStack: MatrixStack,
    checkSides: Boolean,
    random: Random,
    modelData: IModelData
): RenderCtxBase(world, pos, matrixStack, checkSides, random, modelData), ForgeVertexLighterAccess {

    val blockInfo = lighter[VertexLighterFlat_blockInfo]
    override var vertexLighter: ForgeVertexLighter
        get() = (lighter as ForgeVertexLighterAccess).vertexLighter
        set(value) { (lighter as ForgeVertexLighterAccess).vertexLighter = value }

    override fun renderQuad(quad: HalfBakedQuad) { quad.baked.pipe(lighter) }

    override fun renderFallback(model: IBakedModel) {
        directionsAndNull.forEach { face ->
            model.getQuads(state, null, random, modelData).forEach { quad ->
                if (quad.face.shouldRender()) {
                    quad.pipe(lighter)
                    hasRendered = true
                }
            }
        }
    }

    override fun renderMasquerade(offset: Int3, func: () -> Unit) {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmStatic
        fun render(
            lighter: VertexLighterFlat,
            world: ILightReader,
            model: ISpecialRenderModel,
            state: BlockState,
            pos: BlockPos,
            matrixStack: MatrixStack,
            checkSides: Boolean,
            rand: Random, seed: Long,
            modelData: IModelData
        ): Boolean {
            lighter.setWorld(world)
            lighter.setState(state)
            lighter.setBlockPos(pos)
            rand.setSeed(seed)
            lighter.updateBlockInfo()
            return RenderCtxForge(world, pos, lighter, matrixStack, checkSides, rand, modelData).let {
                model.render(it, false)
                lighter.resetBlockInfo()
                it.hasRendered
            }
        }
    }
}