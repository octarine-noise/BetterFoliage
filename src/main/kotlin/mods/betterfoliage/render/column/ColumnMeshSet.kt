package mods.betterfoliage.render.column

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.INVISIBLE
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.LARGE_RADIUS
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.SMALL_RADIUS
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.SQUARE
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.Quad
import mods.betterfoliage.model.UV
import mods.betterfoliage.model.Vertex
import mods.betterfoliage.model.build
import mods.betterfoliage.model.horizontalRectangle
import mods.betterfoliage.model.verticalRectangle
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Rotation
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode.SOLID
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.Direction.EAST
import net.minecraft.util.math.Direction.SOUTH
import net.minecraft.util.math.Direction.UP

/**
 * Collection of dynamically generated meshes used to render rounded columns.
 */
class ColumnMeshSet(
    radiusSmall: Double,
    radiusLarge: Double,
    zProtection: Double,
    val axis: Axis,
    val spriteLeft: Sprite,
    val spriteRight: Sprite,
    val spriteTop: Sprite,
    val spriteBottom: Sprite
) {
    protected fun sideRounded(radius: Double, yBottom: Double, yTop: Double): List<Quad> {
        val halfRadius = radius * 0.5
        return listOf(
            // left side of the diagonal
            verticalRectangle(0.0, 0.5, 0.5 - radius, 0.5, yBottom, yTop).clampUV(minU = 0.0, maxU = 0.5 - radius),
            verticalRectangle(0.5 - radius, 0.5, 0.5 - halfRadius, 0.5 - halfRadius, yBottom, yTop).clampUV(minU = 0.5 - radius),
            // right side of the diagonal
            verticalRectangle(0.5 - halfRadius, 0.5 - halfRadius, 0.5, 0.5 - radius, yBottom, yTop).clampUV(maxU = radius - 0.5),
            verticalRectangle(0.5, 0.5 - radius, 0.5, 0.0, yBottom, yTop).clampUV(minU = radius - 0.5, maxU = 0.0)
        )
    }

    protected fun sideRoundedTransition(radiusBottom: Double, radiusTop: Double, yBottom: Double, yTop: Double): List<Quad> {
        val ySplit = 0.5 * (yBottom + yTop)
        val modelTop = sideRounded(radiusTop, yBottom, yTop)
        val modelBottom = sideRounded(radiusBottom, yBottom, yTop)
        return (modelBottom zip modelTop).map { (quadBottom, quadTop) ->
            Quad.mix(quadBottom, quadTop) { vBottom, vTop -> if (vBottom.xyz.y < ySplit) vBottom.copy() else vTop.copy() }
        }
    }

    protected fun sideSquare(yBottom: Double, yTop: Double) = listOf(
        verticalRectangle(0.0, 0.5, 0.5, 0.5, yBottom, yTop).clampUV(minU = 0.0),
        verticalRectangle(0.5, 0.5, 0.5, 0.0, yBottom, yTop).clampUV(maxU = 0.0)
    )

    protected fun lidRounded(radius: Double, y: Double, isBottom: Boolean) = Array(4) { quadrant ->
        val rotation = baseRotation(axis) + quadrantRotations[quadrant]
        val v1 = Vertex(Double3(0.0, y, 0.0), UV(0.0, 0.0))
        val v2 = Vertex(Double3(0.0, y, 0.5), UV(0.0, 0.5))
        val v3 = Vertex(Double3(0.5 - radius, y, 0.5), UV(0.5 - radius, 0.5))
        val v4 = Vertex(Double3(0.5 - radius * 0.5, y, 0.5 - radius * 0.5), UV(0.5, 0.5))
        val v5 = Vertex(Double3(0.5, y, 0.5 - radius), UV(0.5, 0.5 - radius))
        val v6 = Vertex(Double3(0.5, y, 0.0), UV(0.5, 0.0))
        listOf(Quad(v1, v2, v3, v4), Quad(v1, v4, v5, v6))
            .map { it.cycleVertices(if (isBottom xor BetterFoliage.config.nVidia) 0 else 1) }
            .map { it.rotate(rotation).rotateUV(quadrant) }
            .map { it.sprite(if (isBottom) spriteBottom else spriteTop).colorAndIndex(Color.white.asInt) }
            .map { if (isBottom) it.flipped else it }
    }

    protected fun lidSquare(y: Double, isBottom: Boolean) = Array(4) { quadrant ->
        val rotation = baseRotation(axis) + quadrantRotations[quadrant]
        listOf(
            horizontalRectangle(x1 = 0.0, x2 = 0.5, z1 = 0.0, z2 = 0.5, y = y).clampUV(minU = 0.0, minV = 0.0)
                .rotate(rotation).rotateUV(quadrant)
                .sprite(if (isBottom) spriteBottom else spriteTop).colorAndIndex(Color.white.asInt)
                .let { if (isBottom) it.flipped else it }
        )
    }

    protected val zProtectionScale = zProtection.let { Double3(it, 1.0, it) }

    protected fun List<Quad>.extendTop(size: Double) = map { q -> q.clampUV(minV = 0.5 - size).transformV { v ->
        if (v.xyz.y > 0.501) v.copy(xyz = v.xyz * zProtectionScale) else v }
    }
    protected fun List<Quad>.extendBottom(size: Double) = map { q -> q.clampUV(maxV = -0.5 + size).transformV { v ->
        if (v.xyz.y < -0.501) v.copy(xyz = v.xyz * zProtectionScale) else v }
    }
    protected fun List<Quad>.buildSides(quadsPerSprite: Int) = Array(4) { quadrant ->
        val rotation = baseRotation(axis) + quadrantRotations[quadrant]
        this.map { it.rotate(rotation).colorAndIndex(Color.white.asInt) }
            .mapIndexed { idx, q -> if (idx % (2 * quadsPerSprite) >= quadsPerSprite) q.sprite(spriteRight) else q.sprite(spriteLeft) }
            .build(SOLID, flatLighting = false)
    }

    companion object {
        fun baseRotation(axis: Axis) = when(axis) {
            Axis.X -> Rotation.fromUp[EAST.ordinal]
            Axis.Y -> Rotation.fromUp[UP.ordinal]
            Axis.Z -> Rotation.fromUp[SOUTH.ordinal]
        }
        val quadrantRotations = Array(4) { Rotation.rot90[UP.ordinal] * it }
    }

    //
    // Mesh definitions
    // 4-element arrays hold prebuild meshes for each of the rotations around the axis
    //
    val sideSquare = sideSquare(-0.5, 0.5).buildSides(quadsPerSprite = 1)
    val sideRoundSmall = sideRounded(radiusSmall, -0.5, 0.5).buildSides(quadsPerSprite = 2)
    val sideRoundLarge = sideRounded(radiusLarge, -0.5, 0.5).buildSides(quadsPerSprite = 2)

    val sideExtendTopSquare = sideSquare(0.5, 0.5 + radiusLarge).extendTop(radiusLarge).buildSides(quadsPerSprite = 1)
    val sideExtendTopRoundSmall = sideRounded(radiusSmall, 0.5, 0.5 + radiusLarge).extendTop(radiusLarge).buildSides(quadsPerSprite = 2)
    val sideExtendTopRoundLarge = sideRounded(radiusLarge, 0.5, 0.5 + radiusLarge).extendTop(radiusLarge).buildSides(quadsPerSprite = 2)

    val sideExtendBottomSquare = sideSquare(-0.5 - radiusLarge, -0.5).extendBottom(radiusLarge).buildSides(quadsPerSprite = 1)
    val sideExtendBottomRoundSmall = sideRounded(radiusSmall, -0.5 - radiusLarge, -0.5).extendBottom(radiusLarge).buildSides(quadsPerSprite = 2)
    val sideExtendBottomRoundLarge = sideRounded(radiusLarge, -0.5 - radiusLarge, -0.5).extendBottom(radiusLarge).buildSides(quadsPerSprite = 2)

    val lidTopSquare = lidSquare(0.5, false).build(SOLID, flatLighting = false)
    val lidTopRoundSmall = lidRounded(radiusSmall, 0.5, false).build(SOLID, flatLighting = false)
    val lidTopRoundLarge = lidRounded(radiusLarge, 0.5, false).build(SOLID, flatLighting = false)

    val lidBottomSquare = lidSquare(-0.5, true).build(SOLID, flatLighting = false)
    val lidBottomRoundSmall = lidRounded(radiusSmall, -0.5, true).build(SOLID, flatLighting = false)
    val lidBottomRoundLarge = lidRounded(radiusLarge, -0.5, true).build(SOLID, flatLighting = false)

    val transitionTop = sideRoundedTransition(radiusLarge, radiusSmall, -0.5, 0.5).buildSides(quadsPerSprite = 2)
    val transitionBottom = sideRoundedTransition(radiusSmall, radiusLarge, -0.5, 0.5).buildSides(quadsPerSprite = 2)

    //
    // Helper fuctions for lids (block ends)
    //
    fun flatTop(quadrantTypes: Array<QuadrantType>, quadrant: Int) = when(quadrantTypes[quadrant]) {
        SMALL_RADIUS -> lidTopRoundSmall[quadrant]
        LARGE_RADIUS -> lidTopRoundLarge[quadrant]
        SQUARE -> lidTopSquare[quadrant]
        INVISIBLE -> lidTopSquare[quadrant]
    }

    fun flatBottom(quadrantTypes: Array<QuadrantType>, quadrant: Int) = when(quadrantTypes[quadrant]) {
        SMALL_RADIUS -> lidBottomRoundSmall[quadrant]
        LARGE_RADIUS -> lidBottomRoundLarge[quadrant]
        SQUARE -> lidBottomSquare[quadrant]
        INVISIBLE -> lidBottomSquare[quadrant]
    }

    fun extendTop(quadrantTypes: Array<QuadrantType>, quadrant: Int) = when(quadrantTypes[quadrant]) {
        SMALL_RADIUS -> sideExtendTopRoundSmall[quadrant]
        LARGE_RADIUS -> sideExtendTopRoundLarge[quadrant]
        SQUARE -> sideExtendTopSquare[quadrant]
        INVISIBLE -> sideExtendTopSquare[quadrant]
    }

    fun extendBottom(quadrantTypes: Array<QuadrantType>, quadrant: Int) = when(quadrantTypes[quadrant]) {
        SMALL_RADIUS -> sideExtendBottomRoundSmall[quadrant]
        LARGE_RADIUS -> sideExtendBottomRoundLarge[quadrant]
        SQUARE -> sideExtendBottomSquare[quadrant]
        INVISIBLE -> sideExtendBottomSquare[quadrant]
    }
}