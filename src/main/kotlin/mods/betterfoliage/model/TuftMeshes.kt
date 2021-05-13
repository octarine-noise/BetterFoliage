package mods.betterfoliage.model

import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.texture.Sprite
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction.UP

data class TuftShapeKey(
    val size: Double,
    val height: Double,
    val offset: Double3,
    val flipU1: Boolean,
    val flipU2: Boolean
)

fun tuftShapeSet(size: Double, heightMin: Double, heightMax: Double, hOffset: Double): Array<TuftShapeKey> {
    return Array(64) { idx ->
        TuftShapeKey(
            size,
            randomD(heightMin, heightMax),
            xzDisk(idx) * randomD(hOffset / 2.0, hOffset),
            randomB(),
            randomB()
        )
    }
}

fun tuftQuadSingle(size: Double, height: Double, flipU: Boolean) =
    verticalRectangle(x1 = -0.5 * size, z1 = 0.5 * size, x2 = 0.5 * size, z2 = -0.5 * size, yBottom = 0.5, yTop = 0.5 + height)
        .mirrorUV(flipU, false)

fun tuftModelSet(shapes: Array<TuftShapeKey>, tintIndex: Int, spriteGetter: (Int)->Sprite) = shapes.mapIndexed { idx, shape ->
    listOf(
        tuftQuadSingle(shape.size, shape.height, shape.flipU1),
        tuftQuadSingle(shape.size, shape.height, shape.flipU2).rotate(rot(UP))
    ).map { it.move(shape.offset) }
        .map { it.colorIndex(tintIndex) }
        .map { it.sprite(spriteGetter(idx)) }
}.toTypedArray()

fun fullCubeTextured(spriteId: Identifier, tintIndex: Int, scrambleUV: Boolean = true): Mesh {
    val sprite = Atlas.BLOCKS[spriteId]!!
    return allDirections.map { faceQuad(it) }
        .map { if (!scrambleUV) it else it.rotateUV(randomI(max = 4)) }
        .map { it.sprite(sprite) }
        .map { it.colorIndex(tintIndex) }
        .build(BlendMode.SOLID)
}

fun crossModelsRaw(num: Int, size: Double, hOffset: Double, vOffset: Double): Array<List<Quad>> {
    return Array(num) { idx ->
        listOf(
            verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41),
            verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
                .rotate(rot(UP))
        ).map { it.scale(size) }
        .map { it.move(xzDisk(idx) * hOffset) }
        .map { it.move(UP.vec * randomD(-1.0, 1.0) * vOffset) }
    }
}

fun crossModelSingle(base: List<Quad>, sprite: Sprite, tintIndex: Int,scrambleUV: Boolean) =
    base.map { if (scrambleUV) it.scrambleUV(random, canFlipU = true, canFlipV = true, canRotate = true) else it }
        .map { it.colorIndex(tintIndex) }
        .mapIndexed { idx, quad -> quad.sprite(sprite) }
        .withOpposites()
        .build(BlendMode.CUTOUT_MIPPED)

fun crossModelsTextured(
    leafBase: Array<List<Quad>>,
    tintIndex: Int,
    scrambleUV: Boolean,
    spriteGetter: (Int) -> Identifier
) = leafBase.mapIndexed { idx, leaf ->
    crossModelSingle(leaf, Atlas.BLOCKS[spriteGetter(idx)], tintIndex, scrambleUV)
}.toTypedArray()

    fun Array<List<Quad>>.buildTufts() = withOpposites().build(BlendMode.CUTOUT_MIPPED)