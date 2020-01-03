package mods.octarinecore.client.render

import mods.betterfoliage.client.render.canRenderInCutout
import mods.betterfoliage.client.render.isCutout
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.render.lighting.*
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.plus
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.fluid.Fluids
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IEnviromentBlockReader
import net.minecraft.world.LightType
import net.minecraft.world.biome.Biomes
import java.util.*

class CombinedContext(
    val blockCtx: BlockCtx, val renderCtx: RenderCtx, val lightingCtx: DefaultLightingCtx
) : BlockCtx by blockCtx, LightingCtx by lightingCtx {

    var hasRendered = false

    fun render(force: Boolean = false) = renderCtx.let {
        if (force || state.canRenderInLayer(it.layer) || (state.canRenderInCutout() && it.layer.isCutout)) {
            it.render(blockCtx)
            hasRendered = true
        }
        Unit
    }

    fun exchange(moddedOffset: Int3, targetOffset: Int3) = CombinedContext(
        BasicBlockCtx(OffsetEnvBlockReader(blockCtx.world, pos + moddedOffset, pos + targetOffset), pos),
        renderCtx,
        lightingCtx
    )

    val isCutout = renderCtx.layer.isCutout

    /** Get the centerpoint of the block being rendered. */
    val blockCenter: Double3 get() = Double3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    /** Holds final vertex data before it goes to the [Tessellator]. */
    val temp = RenderVertex()

    fun render(
        model: Model,
        rotation: Rotation = Rotation.identity,
        translation: Double3 = blockCenter,
        forceFlat: Boolean = false,
        quadFilter: (Int, Quad) -> Boolean = { _, _ -> true },
        icon: QuadIconResolver,
        postProcess: PostProcessLambda = noPost
    ) {
        lightingCtx.modelRotation = rotation
        model.quads.forEachIndexed { quadIdx, quad ->
            if (quadFilter(quadIdx, quad)) {
                val drawIcon = icon(this, quadIdx, quad)
                if (drawIcon != null) {
                    // let OptiFine know the texture we're using, so it can
                    // transform UV coordinates to quad-relative
                    Refs.quadSprite.set(renderCtx!!.renderBuffer, drawIcon)

                    quad.verts.forEachIndexed { vertIdx, vert ->
                        temp.init(vert).rotate(lightingCtx.modelRotation).translate(translation)
                        val shader = if (lightingCtx.aoEnabled && !forceFlat) vert.aoShader else vert.flatShader
                        shader.shade(lightingCtx, temp)
                        temp.postProcess(this, quadIdx, quad, vertIdx, vert)
                        temp.setIcon(drawIcon)

                        renderCtx.renderBuffer
                            .pos(temp.x, temp.y, temp.z)
                            .color(temp.red, temp.green, temp.blue, 1.0f)
                            .tex(temp.u, temp.v)
                            .lightmap(temp.brightness shr 16 and 65535, temp.brightness and 65535)
                            .endVertex()
                    }
                }
            }
        }
        hasRendered = true
    }
}

val allFaces: (Direction) -> Boolean = { true }
val topOnly: (Direction) -> Boolean = { it == Direction.UP }

/** Perform no post-processing */
val noPost: PostProcessLambda = { _, _, _, _, _ -> }

object NonNullWorld : IEnviromentBlockReader {
    override fun getBlockState(pos: BlockPos) = Blocks.AIR.defaultState
    override fun getLightFor(type: LightType, pos: BlockPos) = 0
    override fun getFluidState(pos: BlockPos) = Fluids.EMPTY.defaultState
    override fun getTileEntity(pos: BlockPos) = null
    override fun getBiome(pos: BlockPos) = Biomes.THE_VOID
}