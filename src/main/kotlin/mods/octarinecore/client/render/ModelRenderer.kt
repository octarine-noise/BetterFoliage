package mods.octarinecore.client.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.common.util.ForgeDirection.*

typealias QuadIconResolver = (ShadingContext, Int, Quad) -> IIcon
typealias PostProcessLambda = RenderVertex.(ShadingContext, Int, Quad, Int, Vertex) -> Unit

class ModelRenderer() : ShadingContext() {

    /** Holds final vertex data before it goes to the [Tessellator]. */
    val temp = RenderVertex()

    /**
     * Render a [Model].
     * The [blockContext] and [renderBlocks] need to be set up correctly, including first rendering the
     * corresponding block to capture shading values!
     *
     * @param[model] model to render
     * @param[rot] rotation to apply to the model
     * @param[trans] translation to apply to the model
     * @param[forceFlat] force flat shading even if AO is enabled
     * @param[icon] lambda to resolve the texture to use for each quad
     * @param[rotateUV] lambda to get amount of UV rotation for each quad
     * @param[postProcess] lambda to perform arbitrary modifications on the [RenderVertex] just before it goes to the [Tessellator]
     */
    fun render(
        model: Model,
        rot: Rotation = Rotation.identity,
        trans: Double3 = blockContext.blockCenter,
        forceFlat: Boolean = false,
        quadFilter: (Int, Quad) -> Boolean = { qi, q -> true },
        icon: QuadIconResolver,
        rotateUV: (Quad) -> Int,
        postProcess: PostProcessLambda
    ) {
        rotation = rot
        aoEnabled = Minecraft.isAmbientOcclusionEnabled()

        model.quads.forEachIndexed { quadIdx, quad ->
            if (quadFilter(quadIdx, quad)) {
                val drawIcon = icon(this, quadIdx, quad)
                val uvRot = rotateUV(quad)
                quad.verts.forEachIndexed { vertIdx, vert ->
                    temp.init(vert)
                    temp.rotate(rotation).translate(trans).rotateUV(uvRot).setIcon(drawIcon)
                    val shader = if (aoEnabled && !forceFlat) vert.aoShader else vert.flatShader
                    shader.shade(this, temp)
                    temp.postProcess(this, quadIdx, quad, vertIdx, vert)
                    Tessellator.instance.apply {
                        setBrightness(temp.brightness)
                        setColorOpaque_F(temp.red, temp.green, temp.blue)
                        addVertexWithUV(temp.x, temp.y, temp.z, temp.u, temp.v)
                    }
                }
            }
        }
    }
}

/**
 * Queried by [Shader] objects to get rendering-relevant data of the current block in a rotated frame of reference.
 */
open class ShadingContext {
    var rotation = Rotation.identity
    var aoEnabled = Minecraft.isAmbientOcclusionEnabled()

    fun aoShading(face: ForgeDirection, corner1: ForgeDirection, corner2: ForgeDirection) =
        renderBlocks.capture.aoShading(face.rotate(rotation), corner1.rotate(rotation), corner2.rotate(rotation))

    fun blockColor(offset: Int3) = blockContext.blockColor(offset.rotate(rotation))
    fun blockBrightness(offset: Int3) = blockContext.blockBrightness(offset.rotate(rotation))
    fun icon(face: ForgeDirection) = blockContext.icon(face.rotate(rotation))
}

/**
 *
 */
@Suppress("NOTHING_TO_INLINE")
class RenderVertex() {
    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    var u: Double = 0.0
    var v: Double = 0.0
    var brightness: Int = 0
    var red: Float = 0.0f
    var green: Float = 0.0f
    var blue: Float = 0.0f

    fun init(vertex: Vertex, rot: Rotation, trans: Double3): RenderVertex {
        val result = vertex.xyz.rotate(rot) + trans
        x = result.x; y = result.y; z = result.z
        return this
    }
    fun init(vertex: Vertex): RenderVertex {
        x = vertex.xyz.x; y = vertex.xyz.y; z = vertex.xyz.z;
        u = vertex.uv.u; v = vertex.uv.v
        return this
    }
    fun translate(trans: Double3): RenderVertex { x += trans.x; y += trans.y; z += trans.z; return this }
    fun rotate(rot: Rotation): RenderVertex {
        if (rot === Rotation.identity) return this
        val rotX = rot.rotatedComponent(EAST, x, y, z)
        val rotY = rot.rotatedComponent(UP, x, y, z)
        val rotZ = rot.rotatedComponent(SOUTH, x, y, z)
        x = rotX; y = rotY; z = rotZ
        return this
    }
    inline fun rotateUV(n: Int): RenderVertex {
        when (n % 4) {
            1 -> { val t = v; v = -u; u = t; return this }
            2 -> { u = -u; v = -v; return this }
            3 -> { val t = -v; v = u; u = t; return this }
            else -> { return this }
        }
    }
    inline fun setIcon(icon: IIcon): RenderVertex {
        u = (icon.maxU - icon.minU) * (u + 0.5) + icon.minU
        v = (icon.maxV - icon.minV) * (v + 0.5) + icon.minV
        return this
    }

    inline fun setGrey(level: Float) {
        val grey = Math.min((red + green + blue) * 0.333f * level, 1.0f)
        red = grey; green = grey; blue = grey
    }
    inline fun multiplyColor(color: Int) {
        red *= (color shr 16 and 255) / 256.0f
        green *= (color shr 8 and 255) / 256.0f
        blue *= (color and 255) / 256.0f
    }
    inline fun setColor(color: Int) {
        red = (color shr 16 and 255) / 256.0f
        green = (color shr 8 and 255) / 256.0f
        blue = (color and 255) / 256.0f
    }
}

/** Perform no post-processing */
val noPost: RenderVertex.(ShadingContext, Int, Quad, Int, Vertex) -> Unit = { ctx, qi, q, vi, v -> }