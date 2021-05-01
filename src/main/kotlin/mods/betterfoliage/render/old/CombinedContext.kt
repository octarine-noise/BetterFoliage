package mods.betterfoliage.render.old

import mods.betterfoliage.render.canRenderInCutout
import mods.betterfoliage.render.isCutout
import mods.betterfoliage.render.lighting.DefaultLightingCtx
import mods.betterfoliage.render.lighting.LightingCtx
import mods.betterfoliage.render.lighting.PostProcessLambda
import mods.betterfoliage.render.lighting.QuadIconResolver
import mods.betterfoliage.render.lighting.RenderVertex
import mods.octarinecore.BufferBuilder_setSprite
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.plus
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.Vector3f
import net.minecraft.client.renderer.Vector4f
import net.minecraft.fluid.Fluids
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraft.world.LightType
import net.minecraft.world.level.ColorResolver

class CombinedContext(
    val blockCtx: BlockCtx, val renderCtx: RenderCtx, val lightingCtx: DefaultLightingCtx
) : BlockCtx by blockCtx, LightingCtx by lightingCtx {

    var hasRendered = false

    fun render(force: Boolean = false) = renderCtx.let {
        if (force || RenderTypeLookup.canRenderInLayer(state, it.layer) || (state.canRenderInCutout() && it.layer.isCutout)) {
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
    val blockCenter: Double3 get() = Double3((pos.x and 15) + 0.5, (pos.y and 15) + 0.5, (pos.z and 15) + 0.5)

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
        val cameraTransform = renderCtx.matrixStack.last
        lightingCtx.modelRotation = rotation
        model.quads.forEachIndexed { quadIdx, quad ->
            if (quadFilter(quadIdx, quad)) {
                val normal = quad.normal.let { Vector3f(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
                normal.transform(cameraTransform.normal)

                val drawIcon = icon(this, quadIdx, quad)
                if (drawIcon != null) {
                    // let OptiFine know the texture we're using, so it can
                    // transform UV coordinates to quad-relative
                    BufferBuilder_setSprite.invoke(renderCtx.renderBuffer, drawIcon)

                    quad.verts.forEachIndexed { vertIdx, vert ->
                        temp.init(vert).rotate(lightingCtx.modelRotation)
                            .translate(translation)
                        val vertex = temp.let { Vector4f(it.x.toFloat(), it.y.toFloat(), it.z.toFloat(), 0.0F) }
                            .apply { transform(cameraTransform.matrix) }
                        val shader = if (lightingCtx.aoEnabled && !forceFlat) vert.aoShader else vert.flatShader
                        shader.shade(lightingCtx, temp)
                        temp.postProcess(this, quadIdx, quad, vertIdx, vert)
                        temp.setIcon(drawIcon)

                        renderCtx.renderBuffer
                            .pos(temp.x, temp.y, temp.z)
//                            .pos(vertex.x.toDouble(), vertex.y.toDouble(), vertex.z.toDouble())
                            .color(temp.red, temp.green, temp.blue, 1.0f)
                            .tex(temp.u.toFloat(), temp.v.toFloat())
                            .lightmap(temp.brightness shr 16 and 65535, temp.brightness and 65535)
                            .normal(quad.normal.x.toFloat(), quad.normal.y.toFloat(), quad.normal.z.toFloat())
//                            .normal(normal.x, normal.y, normal.z)
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

object NonNullWorld : ILightReader {
    override fun getBlockState(pos: BlockPos) = Blocks.AIR.defaultState
    override fun getLightFor(type: LightType, pos: BlockPos) = 0
    override fun getFluidState(pos: BlockPos) = Fluids.EMPTY.defaultState
    override fun getTileEntity(pos: BlockPos) = null
    override fun getLightManager() = null
    override fun getBlockColor(p0: BlockPos, p1: ColorResolver) = 0
}