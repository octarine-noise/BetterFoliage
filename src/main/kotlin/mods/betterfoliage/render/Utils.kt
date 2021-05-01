@file:JvmName("Utils")

package mods.betterfoliage.render

import mods.betterfoliage.render.lighting.PostProcessLambda
import mods.betterfoliage.render.old.Model
import mods.betterfoliage.render.old.Quad
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.PI2
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.times
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import kotlin.math.cos
import kotlin.math.sin

val up1 = Int3(1 to UP)
val up2 = Int3(2 to UP)
val down1 = Int3(1 to DOWN)
val snowOffset = UP * 0.0625

val normalLeavesRot = arrayOf(Rotation.identity)
val denseLeavesRot = arrayOf(Rotation.identity, Rotation.rot90[EAST.ordinal], Rotation.rot90[SOUTH.ordinal])

val whitewash: PostProcessLambda = { _, _, _, _, _ -> setGrey(1.4f) }
val greywash: PostProcessLambda = { _, _, _, _, _ -> setGrey(1.0f) }

val BlockState.isSnow: Boolean get() = material.let { it == Material.SNOW }

val DIRT_BLOCKS = listOf(Blocks.DIRT, Blocks.COARSE_DIRT)

fun Quad.toCross(rotAxis: Direction, trans: (Quad) -> Quad) =
    (0..3).map { rotIdx ->
        trans(rotate(Rotation.rot90[rotAxis.ordinal] * rotIdx).mirrorUV(rotIdx > 1, false))
    }

fun Quad.toCross(rotAxis: Direction) = toCross(rotAxis) { it }

fun xzDisk(modelIdx: Int) = (PI2 * modelIdx / 64.0).let { Double3(cos(it), 0.0, sin(it)) }

val rotationFromUp = arrayOf(
    Rotation.rot90[EAST.ordinal] * 2,
    Rotation.identity,
    Rotation.rot90[WEST.ordinal],
    Rotation.rot90[EAST.ordinal],
    Rotation.rot90[SOUTH.ordinal],
    Rotation.rot90[NORTH.ordinal]
)

fun Model.mix(first: Model, second: Model, predicate: (Int) -> Boolean) {
    first.quads.forEachIndexed { qi, quad ->
        val otherQuad = second.quads[qi]
        Quad(
            if (predicate(0)) otherQuad.v1.copy() else quad.v1.copy(),
            if (predicate(1)) otherQuad.v2.copy() else quad.v2.copy(),
            if (predicate(2)) otherQuad.v3.copy() else quad.v3.copy(),
            if (predicate(3)) otherQuad.v4.copy() else quad.v4.copy()
        ).add()
    }
}

val RenderType.isCutout: Boolean get() = (this == RenderType.getCutout()) || (this == RenderType.getCutoutMipped())

fun BlockState.canRenderInLayer(layer: RenderType) = RenderTypeLookup.canRenderInLayer(this, layer)
fun BlockState.canRenderInCutout() =
    RenderTypeLookup.canRenderInLayer(this, RenderType.getCutout()) ||
    RenderTypeLookup.canRenderInLayer(this, RenderType.getCutoutMipped())