@file:JvmName("Utils")
package mods.betterfoliage.client.render

import mods.octarinecore.PI2
import mods.octarinecore.client.render.Model
import mods.octarinecore.client.render.lighting.PostProcessLambda
import mods.octarinecore.client.render.Quad
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.times
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
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

fun Quad.toCross(rotAxis: Direction, trans: (Quad)->Quad) =
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

fun Model.mix(first: Model, second: Model, predicate: (Int)->Boolean) {
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

val BlockRenderLayer.isCutout: Boolean get() = (this == BlockRenderLayer.CUTOUT) || (this == BlockRenderLayer.CUTOUT_MIPPED)

fun BlockState.canRenderInLayer(layer: BlockRenderLayer) = this.block.canRenderInLayer(this, layer)
fun BlockState.canRenderInCutout() = this.block.canRenderInLayer(this, BlockRenderLayer.CUTOUT) || this.block.canRenderInLayer(this, BlockRenderLayer.CUTOUT_MIPPED)