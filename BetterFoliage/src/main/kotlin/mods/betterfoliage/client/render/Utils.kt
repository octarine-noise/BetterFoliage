@file:JvmName("Utils")
package mods.betterfoliage.client.render

import mods.octarinecore.PI2
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.times
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*

val up1 = Int3(1 to UP)
val up2 = Int3(2 to UP)
val down1 = Int3(1 to DOWN)
val snowOffset = UP * 0.0625

val normalLeavesRot = arrayOf(Rotation.identity)
val denseLeavesRot = arrayOf(Rotation.identity, Rotation.rot90[EAST.ordinal], Rotation.rot90[SOUTH.ordinal])

val whitewash: RenderVertex.(ShadingContext, Int, Quad, Int, Vertex)->Unit = { ctx, qi, q, vi, v -> setGrey(1.4f) }
val greywash: RenderVertex.(ShadingContext, Int, Quad, Int, Vertex)->Unit = { ctx, qi, q, vi, v -> setGrey(1.0f) }

val IBlockState.isSnow: Boolean get() = material.let { it == Material.snow || it == Material.craftedSnow }

fun Quad.toCross(rotAxis: EnumFacing, trans: (Quad)->Quad) =
    (0..3).map { rotIdx ->
        trans(rotate(Rotation.rot90[rotAxis.ordinal] * rotIdx).mirrorUV(rotIdx > 1, false))
    }
fun Quad.toCross(rotAxis: EnumFacing) = toCross(rotAxis) { it }

fun xzDisk(modelIdx: Int) = (PI2 * modelIdx / 64.0).let { Double3(Math.cos(it), 0.0, Math.sin(it)) }

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