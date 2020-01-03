package mods.octarinecore.client.render.lighting

import mods.octarinecore.client.render.BlockCtx
import mods.octarinecore.common.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockModelRenderer
import net.minecraft.util.Direction
import net.minecraft.world.IEnviromentBlockReader
import java.util.*

val Direction.aoMultiplier: Float get() = when(this) {
    Direction.UP -> 1.0f
    Direction.DOWN -> 0.5f
    Direction.NORTH, Direction.SOUTH -> 0.8f
    Direction.EAST, Direction.WEST -> 0.6f
}

interface LightingCtx {
    val modelRotation: Rotation
    val blockContext: BlockCtx
    val aoEnabled: Boolean

    val brightness get() = brightness(Int3.zero)
    val color get() = color(Int3.zero)
    fun brightness(face: Direction) = brightness(face.offset)
    fun color(face: Direction) = color(face.offset)

    fun brightness(offset: Int3) = offset.rotate(modelRotation).let {
        blockContext.state(it).getPackedLightmapCoords(blockContext.world, blockContext.pos + it)
    }
    fun color(offset: Int3) = blockContext.offset(offset.rotate(modelRotation)).let { Minecraft.getInstance().blockColors.getColor(it.state, it.world, it.pos, 0) }

    fun lighting(face: Direction, corner1: Direction, corner2: Direction): CornerLightData
}

class DefaultLightingCtx(blockContext: BlockCtx) : LightingCtx {
    override var modelRotation = Rotation.identity

    override var aoEnabled = false
        protected set
    override var blockContext: BlockCtx = blockContext
        protected set
    override var brightness = brightness(Int3.zero)
        protected set
    override var color = color(Int3.zero)
        protected set

    override fun brightness(face: Direction) = brightness(face.offset)
    override fun color(face: Direction) = color(face.offset)

    // smooth lighting stuff
    val lightingData = Array(6) { FaceLightData(allDirections[it]) }
    override fun lighting(face: Direction, corner1: Direction, corner2: Direction): CornerLightData = lightingData[face.rotate(modelRotation)].let { faceData ->
        if (!faceData.isValid) faceData.update(blockContext, faceData.face.aoMultiplier)
        return faceData[corner1.rotate(modelRotation), corner2.rotate(modelRotation)]
    }

    fun reset(blockContext: BlockCtx) {
        this.blockContext = blockContext
        brightness = brightness(Int3.zero)
        color = color(Int3.zero)
        modelRotation = Rotation.identity
        lightingData.forEach { it.isValid = false }
        aoEnabled = Minecraft.isAmbientOcclusionEnabled()
//        allDirections.forEach { lightingData[it].update(blockContext, it.aoMultiplier) }
    }
}

private val vanillaAOFactory = BlockModelRenderer.AmbientOcclusionFace::class.java.let {
    it.getDeclaredConstructor(BlockModelRenderer::class.java).apply { isAccessible = true }
}.let { ctor -> { ctor.newInstance(Minecraft.getInstance().blockRendererDispatcher.blockModelRenderer) } }

class FaceLightData(val face: Direction) {
    val topDir = boxFaces[face].top
    val leftDir = boxFaces[face].left

    val topLeft = CornerLightData()
    val topRight = CornerLightData()
    val bottomLeft = CornerLightData()
    val bottomRight = CornerLightData()

    val vanillaOrdered = when(face) {
        Direction.DOWN -> listOf(topLeft, bottomLeft, bottomRight, topRight)
        Direction.UP -> listOf(bottomRight, topRight, topLeft, bottomLeft)
        Direction.NORTH -> listOf(bottomLeft, bottomRight, topRight, topLeft)
        Direction.SOUTH -> listOf(topLeft, bottomLeft, bottomRight, topRight)
        Direction.WEST -> listOf(bottomLeft, bottomRight, topRight, topLeft)
        Direction.EAST -> listOf(topRight, topLeft, bottomLeft, bottomRight)
    }

    val delegate = vanillaAOFactory()
    var isValid = false

    fun update(blockCtx: BlockCtx, multiplier: Float) {
        val quadBounds = FloatArray(12)
        val flags = BitSet(3).apply { set(0) }
        delegate.updateVertexBrightness(blockCtx.world, blockCtx.state, blockCtx.pos, face, quadBounds, flags)
        vanillaOrdered.forEachIndexed { idx, corner -> corner.set(delegate.vertexBrightness[idx], delegate.vertexColorMultiplier[idx] * multiplier) }
        isValid = true
    }

    operator fun get(dir1: Direction, dir2: Direction): CornerLightData {
        val isTop = topDir == dir1 || topDir == dir2
        val isLeft = leftDir == dir1 || leftDir == dir2
        return if (isTop) {
            if (isLeft) topLeft else topRight
        } else {
            if (isLeft) bottomLeft else bottomRight
        }
    }
}