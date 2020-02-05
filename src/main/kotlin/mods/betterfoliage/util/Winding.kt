package mods.betterfoliage.util

import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*

fun Pair<Direction, Direction>.ccwWinding() = arrayOf(first to second, first.opposite to second, first.opposite to second.opposite, first to second.opposite)

typealias BoxCorner = Triple<Direction, Direction, Direction>
fun BoxCorner.equalsUnordered(other: BoxCorner) = contains(other.first) && contains(other.second) && contains(other.third)

fun BoxCorner.contains(dir: Direction) = first == dir || second == dir || third == dir

fun Array<BoxCorner>.findIdx(corner: BoxCorner): Int? {
    forEachIndexed { idx, test -> if (test.contains(corner.first) && test.contains(corner.second) && test.contains(corner.third)) return idx }
    return null
}
fun Array<BoxCorner>.findIdx(predicate: (BoxCorner)->Boolean): Int? {
    forEachIndexed { idx, test -> if (predicate(test)) return idx }
    return null
}

/**
 *  Decribes the winding order for vanilla AO data.
 *   1st index: light face ordinal
 *   2nd index: index in AO data array
 *   value: pair of [Direction]s that along with light face fully describe the corner
 *          the AO data belongs to
 */
val aoWinding = allDirections.map { when(it) {
    DOWN -> (SOUTH to WEST).ccwWinding()
    UP -> (SOUTH to EAST).ccwWinding()
    NORTH -> (WEST to UP).ccwWinding()
    SOUTH -> (UP to WEST).ccwWinding()
    WEST -> (SOUTH to UP).ccwWinding()
    EAST -> (SOUTH to DOWN).ccwWinding()
} }

/**
 * Indexing for undirected box corners (component order does not matter).
 * Array contains [Direction] triplets fully defining the corner.
 */
val cornersUndir = Array(8) { idx -> Triple(
    if (idx and 1 != 0) EAST else WEST,
    if (idx and 2 != 0) UP else DOWN,
    if (idx and 4 != 0) SOUTH else NORTH
) }

/**
 * Reverse lookup for [cornersUndir]. Index 3 times with the corner's cardinal directions.
 *  A null value indicates an invalid corner (multiple indexing along the same axis)
 */
val cornersUndirIdx = Array(6) { idx1 -> Array(6) { idx2 -> Array(6) { idx3 ->
    cornersUndir.findIdx(BoxCorner(byId(idx1), byId(idx2), byId(idx3)))
} } }

/**
 * Get corner index for vertex coordinates
 */
fun getCornerUndir(x: Float, y: Float, z: Float): Int {
    var result = 0
    if (x > 0.5f) result += 1
    if (y > 0.5f) result += 2
    if (z > 0.5f) result += 4
    return result
}

/**
 * Indexing scheme for directed box corners.
 * The first direction - the face - matters, the other two are unordered.
 * 1:1 correspondence with possible AO values.
 * Array contains triplets defining the corner fully.
 */
val cornersDir = Array(24) { idx ->
    val faceIdx = idx / 4
    val windingIdx = idx % 4
    val winding = aoWinding[faceIdx][windingIdx]
    BoxCorner(byId(faceIdx), winding.first, winding.second)
}

/**
 * Reverse lookup for [cornersDir].
 *   1st index: primary face
 *   2nd index: undirected corner index.
 *   value: directed corner index
 *   A null value indicates an invalid corner (primary face not shared by corner).
 */
val cornerDirFromUndir = Array(6) { faceIdx -> Array(8) { undirIdx ->
    val face = byId(faceIdx); val corner = cornersUndir[undirIdx]
    if (!corner.contains(face)) null else cornersDir.findIdx { it.first == face && it.equalsUnordered(corner) }
} }

/**
 * Reverse lookup for [cornersDir].
 *   1st index: primary face
 *   2nd index: AO value index on the face.
 *   value: directed corner index
 */
val cornerDirFromAo = Array(6) { faceIdx -> IntArray(4) { aoIdx ->
    val face = byId(faceIdx); val corner = aoWinding[face.ordinal][aoIdx].let { Triple(face, it.first, it.second) }
    cornersDir.findIdx { it.first == face && it.equalsUnordered(corner) }!!
} }

