package mods.betterfoliage.resource.model

import mods.betterfoliage.render.block.vanilla.getColorOverride
import mods.betterfoliage.render.old.Color
import mods.betterfoliage.render.old.HalfBakedQuad
import mods.betterfoliage.render.old.Quad
import mods.betterfoliage.render.old.bake
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.PI2
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.averageColor
import mods.betterfoliage.util.random
import mods.betterfoliage.util.randomB
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import mods.betterfoliage.util.rot
import mods.betterfoliage.util.vec
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import kotlin.math.cos
import kotlin.math.sin

fun xzDisk(modelIdx: Int) = (PI2 * modelIdx.toDouble() / 64.0).let { Double3(cos(it), 0.0, sin(it)) }


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
    Quad.verticalRectangle(
        x1 = -0.5 * size,
        z1 = 0.5 * size,
        x2 = 0.5 * size,
        z2 = -0.5 * size,
        yBottom = 0.5,
        yTop = 0.5 + height
    )
        .mirrorUV(flipU, false)

fun tuftModelSet(shapes: Array<TuftShapeKey>, overrideColor: Color?, spriteGetter: (Int) -> TextureAtlasSprite) =
    shapes.mapIndexed { idx, shape ->
        listOf(
            tuftQuadSingle(shape.size, shape.height, shape.flipU1),
            tuftQuadSingle(shape.size, shape.height, shape.flipU2).rotate(rot(UP))
        ).map { it.move(shape.offset) }
            .map { it.colorAndIndex(overrideColor) }
            .map { it.sprite(spriteGetter(idx)) }
    }

fun fullCubeTextured(
    spriteLocation: ResourceLocation,
    overrideColor: Color?,
    scrambleUV: Boolean = true
): List<HalfBakedQuad> {
    val sprite = Atlas.BLOCKS[spriteLocation]
    return allDirections.map { Quad.faceQuad(it) }
        .map { if (!scrambleUV) it else it.rotateUV(randomI(max = 4)) }
        .map { it.sprite(sprite) }
        .map { it.colorAndIndex(overrideColor) }
        .bake(true)
}

fun fullCubeTinted(spriteLocation: ResourceLocation, threshold: Double, scrambleUV: Boolean = true): List<HalfBakedQuad> {
    val overrideColor = Atlas.BLOCKS[spriteLocation].getColorOverride(threshold)
    return fullCubeTextured(spriteLocation, overrideColor, scrambleUV)
}

fun crossModelsRaw(num: Int, size: Double, hOffset: Double, vOffset: Double): Array<List<Quad>> {
    return Array(num) { idx ->
        listOf(
            Quad.verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41),
            Quad.verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
                .rotate(rot(UP))
        ).map { it.scale(size) }
            .map { it.move(xzDisk(idx) * hOffset) }
            .map { it.move(UP.vec * randomD(-1.0, 1.0) * vOffset) }
    }
}

fun crossModelSingle(base: List<Quad>, sprite: TextureAtlasSprite, overrideColor: Color?, scrambleUV: Boolean) =
    base.map { if (scrambleUV) it.scrambleUV(random, canFlipU = true, canFlipV = true, canRotate = true) else it }
        .map { it.colorAndIndex(overrideColor) }
        .mapIndexed { idx, quad -> quad.sprite(sprite) }
        .withOpposites()
        .bake(false)

fun crossModelsTextured(
    leafBase: Array<List<Quad>>,
    overrideColor: Color?,
    scrambleUV: Boolean,
    spriteGetter: (Int) -> ResourceLocation
) = leafBase.mapIndexed { idx, leaf ->
    crossModelSingle(leaf, Atlas.BLOCKS[spriteGetter(idx)], overrideColor, scrambleUV)
}.toTypedArray()

fun crossModelsTinted(
    leafBase: Array<List<Quad>>,
    threshold: Double,
    scrambleUV: Boolean = true,
    spriteGetter: (Int) -> ResourceLocation
) = leafBase.mapIndexed { idx, leaf ->
    val sprite = Atlas.BLOCKS[spriteGetter(idx)]
    val overrideColor = sprite.getColorOverride(threshold)
    crossModelSingle(leaf, sprite, overrideColor, scrambleUV)
}

fun List<Quad>.withOpposites() = flatMap { listOf(it, it.flipped) }
fun List<List<Quad>>.buildTufts(applyDiffuseLighting: Boolean = false) =
    map { it.withOpposites().bake(applyDiffuseLighting) }.toTypedArray()