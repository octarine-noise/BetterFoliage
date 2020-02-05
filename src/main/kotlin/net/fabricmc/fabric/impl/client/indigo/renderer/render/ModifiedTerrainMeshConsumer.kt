package net.fabricmc.fabric.impl.client.indigo.renderer.render

import mods.betterfoliage.render.lighting.CustomLightingMeshConsumer
import mods.betterfoliage.render.lighting.CustomLighting
import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl
import net.minecraft.util.math.Direction

val AoCalculator_computeFace = YarnHelper.requiredMethod<Any>(
    "net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator", "computeFace",
    "(Lnet/minecraft/util/math/Direction;Z)Lnet/fabricmc/fabric/impl/client/indigo/renderer/aocalc/AoFaceData;"
)
val AoFaceData_toArray = YarnHelper.requiredMethod<Unit>(
    "net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoFaceData", "toArray",
    "([F[I[I)V"
)

open class ModifiedTerrainMeshConsumer(
    blockInfo: TerrainBlockRenderInfo,
    chunkInfo: ChunkRenderInfo,
    aoCalc: AoCalculator,
    transform: RenderContext.QuadTransform
) : TerrainMeshConsumer(blockInfo, chunkInfo, aoCalc, transform), CustomLightingMeshConsumer {

    /** Custom lighting to use */
    var lighter: CustomLighting? = null

    /** Cache validity for AO/light values */
    protected val aoValid = Array(6) { false }
    override val aoFull = FloatArray(24)
    override val lightFull = IntArray(24)

    /** Cached block brightness values for all neighbors */
    val brNeighbor = IntArray(6)
    /** Cache validity for block brightness values (neighbors + self) */
    val brValid = Array(7) { false }

    override var brSelf: Int = -1
        get() { if (brValid[6]) return field else
            field = blockInfo.blockView.getBlockState(blockInfo.blockPos).getBlockBrightness(blockInfo.blockView, blockInfo.blockPos)
            brValid[6] = true
            return field
        }
        protected set

    override fun brNeighbor(dir: Direction): Int {
        if (brValid[dir.ordinal]) return brNeighbor[dir.ordinal]
        blockInfo.blockView.getBlockState(blockInfo.blockPos)
            .getBlockBrightness(blockInfo.blockView, blockInfo.blockPos + dir.offset)
            .let { brNeighbor[dir.ordinal] = it; brValid[dir.ordinal] = true; return it }
    }

    override fun clearLighting() {
        for (idx in 0 until 6) {
            aoValid[idx] = false
            brValid[idx] = false
        }
        brValid[6] = false
    }

    override fun fillAoData(lightFace: Direction) {
        if (!aoValid[lightFace.ordinal]) {
            AoFaceData_toArray.invoke(AoCalculator_computeFace.invoke(aoCalc, lightFace, true), aoFull, lightFull, cornerDirFromAo[lightFace.ordinal])
            aoValid[lightFace.ordinal] = true
        }
    }

    override fun setLighting(vIdx: Int, ao: Float, light: Int) {
        aoCalc.ao[vIdx] = ao
        aoCalc.light[vIdx] = light
    }

    override fun applyOffsets(quad: MutableQuadViewImpl) {
        // Moved farther back in the pipeline, after custom lighting is applied
        // Might possibly mess emissive multitexturing up, but seems to be OK for now
    }

    override fun tesselateFlat(q: MutableQuadViewImpl, renderLayer: Int, blockColorIndex: Int) {
        lighter?.applyLighting(this, q, flat = true, emissive = false)
        super.applyOffsets(q)
        super.tesselateSmooth(q, renderLayer, blockColorIndex)
    }

    override fun tesselateFlatEmissive(q: MutableQuadViewImpl, renderLayer: Int, blockColorIndex: Int, lightmaps: IntArray) {
        lighter?.applyLighting(this, q, flat = true, emissive = true)
        super.applyOffsets(q)
        super.tesselateSmoothEmissive(q, renderLayer, blockColorIndex)
    }

    override fun tesselateSmooth(q: MutableQuadViewImpl, renderLayer: Int, blockColorIndex: Int) {
        lighter?.applyLighting(this, q, flat = false, emissive = false)
        super.applyOffsets(q)
        super.tesselateSmooth(q, renderLayer, blockColorIndex)
    }

    override fun tesselateSmoothEmissive(q: MutableQuadViewImpl, renderLayer: Int, blockColorIndex: Int) {
        lighter?.applyLighting(this, q, flat = false, emissive = true)
        super.applyOffsets(q)
        super.tesselateSmoothEmissive(q, renderLayer, blockColorIndex)
    }
}