package mods.octarinecore.client.render

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.client.registry.RenderingRegistry
import mods.octarinecore.metaprog.reflectField
import mods.octarinecore.metaprog.reflectStaticField
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.common.util.ForgeDirection.*

/** Reference to the handler list in Forge [RenderingRegistry]. */
val renderingHandlers: Map<Int, ISimpleBlockRenderingHandler> = RenderingRegistry::class.java
    .reflectStaticField<RenderingRegistry>("INSTANCE")!!
    .reflectField<Map<Int, ISimpleBlockRenderingHandler>>("blockRenderers")!!

/**
 * Used instead of the vanilla [RenderBlocks] to get to the AO values and textures used in rendering
 * without duplicating vanilla code.
 */
class ExtendedRenderBlocks : RenderBlocks() {

    /** Captures the AO values and textures used in a specific rendering pass when rendering a block. */
    val capture = ShadingCapture()

    override fun renderFaceXPos(block: Block?, x: Double, y: Double, z: Double, icon: IIcon?) = renderFace(EAST, block, x, y, z, icon)
    override fun renderFaceXNeg(block: Block?, x: Double, y: Double, z: Double, icon: IIcon?) = renderFace(WEST, block, x, y, z, icon)
    override fun renderFaceYPos(block: Block?, x: Double, y: Double, z: Double, icon: IIcon?) = renderFace(UP, block, x, y, z, icon)
    override fun renderFaceYNeg(block: Block?, x: Double, y: Double, z: Double, icon: IIcon?) = renderFace(DOWN, block, x, y, z, icon)
    override fun renderFaceZPos(block: Block?, x: Double, y: Double, z: Double, icon: IIcon?) = renderFace(SOUTH, block, x, y, z, icon)
    override fun renderFaceZNeg(block: Block?, x: Double, y: Double, z: Double, icon: IIcon?) = renderFace(NORTH, block, x, y, z, icon)

    /**
     * Render a block face, saving relevant data if appropriate.
     */
    @Suppress("NON_EXHAUSTIVE_WHEN")
    fun renderFace(face: ForgeDirection, block: Block?, x: Double, y: Double, z: Double, icon: IIcon?) {
        if (capture.isCorrectPass(face)) {
            saveAllShading(face); capture.icons[face.ordinal] = icon
        }
        if (capture.renderCallback(capture, face, capture.passes[face.ordinal], icon)) when (face) {
            EAST -> super.renderFaceXPos(block, x, y, z, icon)
            WEST -> super.renderFaceXNeg(block, x, y, z, icon)
            UP -> super.renderFaceYPos(block, x, y, z, icon)
            DOWN -> super.renderFaceYNeg(block, x, y, z, icon)
            SOUTH -> super.renderFaceZPos(block, x, y, z, icon)
            NORTH -> super.renderFaceZNeg(block, x, y, z, icon)
        }
    }

    fun saveTopLeft(face: ForgeDirection, corner: Pair<ForgeDirection, ForgeDirection>) =
        capture.aoShading(face, corner.first, corner.second)
            .set(brightnessTopLeft, colorRedTopLeft, colorGreenTopLeft, colorBlueTopLeft)

    fun saveTopRight(face: ForgeDirection, corner: Pair<ForgeDirection, ForgeDirection>) =
        capture.aoShading(face, corner.first, corner.second)
            .set(brightnessTopRight, colorRedTopRight, colorGreenTopRight, colorBlueTopRight)

    fun saveBottomLeft(face: ForgeDirection, corner: Pair<ForgeDirection, ForgeDirection>) =
        capture.aoShading(face, corner.first, corner.second)
            .set(brightnessBottomLeft, colorRedBottomLeft, colorGreenBottomLeft, colorBlueBottomLeft)

    fun saveBottomRight(face: ForgeDirection, corner: Pair<ForgeDirection, ForgeDirection>) =
        capture.aoShading(face, corner.first, corner.second)
            .set(brightnessBottomRight, colorRedBottomRight, colorGreenBottomRight, colorBlueBottomRight)

    fun saveAllShading(face: ForgeDirection) {
        saveTopLeft(face, faceCorners[face.ordinal].topLeft)
        saveTopRight(face, faceCorners[face.ordinal].topRight)
        saveBottomLeft(face, faceCorners[face.ordinal].bottomLeft)
        saveBottomRight(face, faceCorners[face.ordinal].bottomRight)
    }
}

/**
 * Captures the AO values and textures used in a specific rendering pass when rendering a block.
 */
class ShadingCapture {
    /** Sparse array of stored AO data. */
    val aoShadings = arrayOfNulls<AoData>(6 * 6 * 6)

    /** List of stored AO data (only valid instances). */
    var shadingsList = listOf<AoData>()

    /** List of stored texture data. */
    val icons = arrayOfNulls<IIcon>(6)

    /** Number of passes to go on a given face. */
    val passes = Array(6) { 0 }

    /** lambda to determine which faces to render. */
    var renderCallback = alwaysRender

    init {
        (0..5).forEach { i1 ->
            (0..5).forEach { i2 ->
                (i2..5).forEach { i3 ->
                    aoShadings[cornerId(i1, i2, i3)] = AoData()
                }
            }
        }
        shadingsList = aoShadings.filterNotNull()
    }

    /**
     * Get the AO data of a specific corner.
     *
     * The two corner directions are interchangeable. All 3 parameters must lie on different axes.
     *
     * @param[face] block face
     * @param[corner1] first direction of corner on face
     * @param[corner2] second direction of corner on face
     */
    fun aoShading(face: ForgeDirection, corner1: ForgeDirection, corner2: ForgeDirection) =
        aoShadings[cornerId(face, corner1, corner2)]!!

    /** Returns true if the AO and texture data should be saved. Mutates state. */
    fun isCorrectPass(face: ForgeDirection) = (passes[face.ordinal]-- > 0)

    /**
     * Reset all data and pass counters.
     *
     * @param[targetPass] which render pass to save
     */
    fun reset(targetPass: Int) {
        shadingsList.forEach { it.reset() }
        (0..5).forEach { idx -> icons[idx] = null; passes[idx] = targetPass }
    }

    /** One-dimensional index of a specific corner. */
    protected fun cornerId(face: Int, corner1: Int, corner2: Int) = when (corner2 > corner1) {
        true -> 36 * face + 6 * corner1 + corner2
        false -> 36 * face + 6 * corner2 + corner1
    }

    /** One-dimensional index of a specific corner. */
    protected fun cornerId(face: ForgeDirection, corner1: ForgeDirection, corner2: ForgeDirection) =
        cornerId(face.ordinal, corner1.ordinal, corner2.ordinal)
}

/** Lambda to render all faces of a block */
val alwaysRender: (ShadingCapture, ForgeDirection, Int, IIcon?) -> Boolean = { ctx, face, pass, icon -> true }

/** Lambda to render no faces of a block */
val neverRender: (ShadingCapture, ForgeDirection, Int, IIcon?) -> Boolean = { ctx, face, pass, icon -> false }

