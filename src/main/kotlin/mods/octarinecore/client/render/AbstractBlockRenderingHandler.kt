@file:JvmName("RendererHolder")
package mods.octarinecore.client.render

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.client.registry.RenderingRegistry
import mods.betterfoliage.client.integration.OptifineCTM
import mods.betterfoliage.loader.Refs
import mods.octarinecore.ThreadLocalDelegate
import mods.octarinecore.client.resource.ResourceHandler
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.util.IIcon
import net.minecraft.util.MathHelper
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection

/**
 * [ThreadLocal] instance of [ExtendedRenderBlocks] used instead of the vanilla [RenderBlocks] to get the
 * AO values and textures used in rendering without duplicating vanilla code.
 */
val renderBlocks by ThreadLocalDelegate { ExtendedRenderBlocks() }

/**
 * [ThreadLocal] instance of [BlockContext] representing the block being rendered.
 */
val blockContext by ThreadLocalDelegate { BlockContext() }

/**
 * [ThreadLocal] instance of [ModelRenderer].
 */
val modelRenderer by ThreadLocalDelegate { ModelRenderer() }

abstract class AbstractBlockRenderingHandler(modId: String) : ResourceHandler(modId), ISimpleBlockRenderingHandler {

    // ============================
    // Self-registration
    // ============================
    val id = RenderingRegistry.getNextAvailableRenderId()
    init {
        RenderingRegistry.registerBlockHandler(this);
    }

    // ============================
    // Custom rendering
    // ============================
    abstract fun isEligible(ctx: BlockContext): Boolean
    abstract fun render(ctx: BlockContext, parent: RenderBlocks): Boolean

    // ============================
    // Interface implementation
    // ============================
    override fun renderWorldBlock(world: IBlockAccess?, x: Int, y: Int, z: Int, block: Block?, modelId: Int, parentRenderer: RenderBlocks?): Boolean {
        renderBlocks.blockAccess = world
        return render(blockContext, parentRenderer!!)
    }
    override fun renderInventoryBlock(block: Block?, metadata: Int, modelId: Int, renderer: RenderBlocks?) {}
    override fun shouldRender3DInInventory(modelId: Int) = true
    override fun getRenderId(): Int = id

    // ============================
    // Vanilla rendering wrapper
    // ============================
    /**
     * Render the block in the current [BlockContext], and capture shading and texture data.
     *
     * @param[parentRenderer] parent renderer passed in by rendering pipeline, used only for block breaking overlay
     * @param[targetPass] which render pass to save shading and texture data from
     * @param[block] lambda to use to render the block if it does not have a custom renderer
     * @param[face] lambda to determine which faces of the block to render
     */
    fun renderWorldBlockBase(
        parentRenderer: RenderBlocks = renderBlocks,
        targetPass: Int = 1,
        block: () -> Unit = { blockContext.let { ctx -> renderBlocks.renderStandardBlock(ctx.block, ctx.x, ctx.y, ctx.z) } },
        face: (ShadingCapture, ForgeDirection, Int, IIcon?) -> Boolean
    ): Boolean {
        val ctx = blockContext
        val renderBlocks = renderBlocks

        // use original renderer for block breaking overlay
        if (parentRenderer.hasOverrideBlockTexture()) {
            parentRenderer.setRenderBoundsFromBlock(ctx.block);
            parentRenderer.renderStandardBlock(ctx.block, ctx.x, ctx.y, ctx.z);
            return true;
        }

        // render block
        renderBlocks.capture.reset(targetPass)
        renderBlocks.capture.renderCallback = face
        renderBlocks.setRenderBoundsFromBlock(ctx.block);
        val handler = renderingHandlers[ctx.block.renderType];
        if (handler != null && ctx.block.renderType != 0) {
            handler.renderWorldBlock(ctx.world, ctx.x, ctx.y, ctx.z, ctx.block, ctx.block.renderType, renderBlocks);
        } else {
            block()
        }
        return false;
    }

}

/**
 * Represents the block being rendered. Has properties and methods to query the neighborhood of the block in
 * block-relative coordinates.
 */
class BlockContext() {
    var world: IBlockAccess? = null
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0

    fun set(world: IBlockAccess, x: Int, y: Int, z: Int) { this.world = world; this.x = x; this.y = y; this.z = z; }

    /** Get the [Block] at the given offset. */
    val block: Block get() = world!!.getBlock(x, y, z)
    fun block(offset: Int3) = world!!.getBlock(x + offset.x, y + offset.y, z + offset.z)

    /** Get the metadata at the given offset. */
    val meta: Int get() = world!!.getBlockMetadata(x, y, z)
    fun meta(offset: Int3) = world!!.getBlockMetadata(x + offset.x, y + offset.y, z + offset.z)

    /** Get the block color multiplier at the given offset. */
    val blockColor: Int get() = block.colorMultiplier(world, x, y, z)
    fun blockColor(offset: Int3) = block(offset).colorMultiplier(world, x + offset.x, y + offset.y, z + offset.z)

    /** Get the block brightness at the given offset. */
    val blockBrightness: Int get() = block.getMixedBrightnessForBlock(world, x, y, z)
    fun blockBrightness(offset: Int3) = block(offset).getMixedBrightnessForBlock(world, x + offset.x, y + offset.y, z + offset.z)

    fun shouldRenderSide(offset: Int3, side: ForgeDirection) = block.shouldSideBeRendered(world, x + offset.x, y + offset.y, z + offset.z, side.ordinal)

    /** Get the biome ID at the block position. */
    val biomeId: Int get() = world!!.getBiomeGenForCoords(x, z).biomeID

    /** Get the texture on a given face of the block at the given offset. */
    fun icon(face: ForgeDirection, offset: Int3 = Int3.zero) = block(offset).getIcon(world, x + offset.x, y + offset.y, z + offset.z, face.ordinal).let {
        if (!OptifineCTM.isAvailable) it
        else Refs.getConnectedTexture.invokeStatic(world!!, block(offset), x + offset.x, y + offset.y, z + offset.z, face.ordinal, it) as IIcon
    }

    /** Get the centerpoint of the block being rendered. */
    val blockCenter: Double3 get() = Double3(x + 0.5, y + 0.5, z + 0.5)

    /** Is the block surrounded by other blocks that satisfy the predicate on all sides? */
    fun isSurroundedBy(predicate: (Block)->Boolean) = forgeDirOffsets.all { predicate(block(it)) }

    /** Get a semi-random value based on the block coordinate and the given seed. */
    fun random(seed: Int): Int {
        var value = (x * x + y * y + z * z + x * y + y * z + z * x + (seed * seed)) and 63
        value = (3 * x * value + 5 * y * value + 7 * z * value + (11 * seed)) and 63
        return value
    }

    /** Get an array of semi-random values based on the block coordinate. */
    fun semiRandomArray(num: Int): Array<Int> = Array(num) { random(it) }

    /** Get the distance of the block from the camera (player). */
    val cameraDistance: Int get() {
        val camera = Minecraft.getMinecraft().renderViewEntity ?: return 0
        return Math.abs(x - MathHelper.floor_double(camera.posX)) +
               Math.abs(y - MathHelper.floor_double(camera.posY)) +
               Math.abs(z - MathHelper.floor_double(camera.posZ))
    }
}