package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.model.HalfBakedQuad
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.render.lighting.ForgeVertexLighter
import mods.betterfoliage.render.lighting.ForgeVertexLighterAccess
import mods.betterfoliage.util.getWithDefault
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.LightTexture
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockDisplayReader
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.model.data.IModelData
import net.minecraftforge.client.model.pipeline.VertexLighterFlat
import java.util.Random

class RenderCtxForge(
    blockCtx: BlockCtx,
    val lighter: VertexLighterFlat,
    matrixStack: MatrixStack,
    checkSides: Boolean,
    random: Random,
    modelData: IModelData,
) : RenderCtxBase(blockCtx, matrixStack, checkSides, random, modelData), ForgeVertexLighter {

    override fun renderQuad(quad: HalfBakedQuad) {
        // set Forge lighter AO calculator to us
        vertexLighter.updateLightmapAndColor(quad, lightingData)
        quad.baked.pipe(lighter)
    }

    // somewhat ugly hack to pipe lighting values into the Forge pipeline
    var vIdx = 0
    override fun updateVertexLightmap(normal: FloatArray, lightmap: FloatArray, x: Float, y: Float, z: Float) {
        lightingData.packedLight[vIdx].let { packedLight ->
            lightmap[0] = LightTexture.block(packedLight) / 0xF.toFloat()
            lightmap[1] = LightTexture.sky(packedLight) / 0xF.toFloat()
        }
    }

    override fun updateVertexColor(
        normal: FloatArray,
        color: FloatArray,
        x: Float,
        y: Float,
        z: Float,
        tint: Float,
        multiplier: Int
    ) {
        color[0] = lightingData.tint[0] * lightingData.colorMultiplier[vIdx]
        color[1] = lightingData.tint[1] * lightingData.colorMultiplier[vIdx]
        color[2] = lightingData.tint[2] * lightingData.colorMultiplier[vIdx]
        vIdx++
    }

    companion object {
        @JvmStatic
        fun render(
            lighter: VertexLighterFlat,
            world: IBlockDisplayReader,
            model: SpecialRenderModel,
            state: BlockState,
            pos: BlockPos,
            matrixStack: MatrixStack,
            checkSides: Boolean,
            random: Random, seed: Long,
            modelData: IModelData
        ): Boolean {
            val blockCtx = BasicBlockCtx(world, pos)
            val ctx = RenderCtxForge(blockCtx, lighter, matrixStack, checkSides, random, modelData).apply {
                lighter.setWorld(world)
                lighter.setState(state)
                lighter.setBlockPos(pos)
                lighter.updateBlockInfo()
            }

            // render layer
            return ctx.let {
                (lighter as ForgeVertexLighterAccess).vertexLighter = it
                model.renderLayer(it, specialRenderData.get()!!, MinecraftForgeClient.getRenderLayer())
                lighter.resetBlockInfo()
                it.hasRendered
            }
        }
    }
}