package mods.betterfoliage.render.lighting

import mods.betterfoliage.util.get
import mods.betterfoliage.util.mapArray
import mods.betterfoliage.util.perpendiculars
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*

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

class AoSideHelper private constructor(face: Direction) {
    val sides = faceSides[face]
    val cornerSideDirections = faceCorners[face]
    val aoIndex = faceCornersIdx.mapArray { corner ->
        boxCornersDirIdx[face][sides[corner.first]][sides[corner.second]]!!
    }

    companion object {
        /**
         * Indexing for undirected box corners (component order does not matter).
         * Array contains [Direction] triplets fully defining the corner.
         */
        @JvmField
        val boxCornersUndir = Array(8) { idx -> Triple(
            if (idx and 1 != 0) EAST else WEST,
            if (idx and 2 != 0) UP else DOWN,
            if (idx and 4 != 0) SOUTH else NORTH
        ) }

        /**
         * Reverse lookup for [boxCornersUndir]. Index 3 times with the corner's cardinal directions.
         *  A null value indicates an invalid corner (multiple indexing along the same axis)
         */
        @JvmField
        val boxCornersUndirIdx = Array(6) { idx1 -> Array(6) { idx2 -> Array(6) { idx3 ->
            boxCornersUndir.findIdx(BoxCorner(
                Direction.values()[idx1],
                Direction.values()[idx2],
                Direction.values()[idx3]
            ))
        } } }

        /**
         * Indexing for directed face sides
         * First index is the face, second is index of side on face
         */
        @JvmField
        val faceSides = Array(6) { faceIdx -> Array(4) { sideIdx ->
            Direction.values()[faceIdx].perpendiculars[sideIdx]
        } }

        /**
         * Pairs of [faceSides] side indexes that form a valid pair describing a corner
         */
        @JvmField
        val faceCornersIdx = arrayOf(0 to 2, 0 to 3, 1 to 2, 1 to 3)

        /**
         * Indexing for directed face corners
         * First index is the face, second is index of corner on face
         */
        @JvmField
        val faceCorners = Array(6) { faceIdx -> Array(4) { cornerIdx ->
            faceCornersIdx[cornerIdx].let { faceSides[faceIdx][it.first] to faceSides[faceIdx][it.second] }
        } }

        /**
         * Indexing scheme for directed box corners.
         * The first direction - the face - matters, the other two are unordered.
         * 1:1 correspondence with possible AO values.
         * Array contains triplets defining the corner fully.
         */
        @JvmField
        val boxCornersDir = Array(24) { idx ->
            val faceIdx = idx / 4; val face = Direction.values()[faceIdx]
            val cornerIdx = idx % 4; val corner = faceCorners[faceIdx][cornerIdx]
            BoxCorner(face, corner.first, corner.second)
        }

        /**
         * Reverse lookup for [boxCornersDir]. Index 3 times with the corner's cardinal directions.
         * The first direction - the face - matters, the other two are unordered.
         *  A null value indicates an invalid corner (multiple indexing along the same axis)
         */
        @JvmField
        val boxCornersDirIdx = Array(6) { face -> Array(6) { side1 -> Array(6) { side2 ->
            boxCornersDir.findIdx { boxCorner ->
                boxCorner.first.ordinal == face && boxCorner.equalsUnordered(BoxCorner(
                    Direction.values()[face],
                    Direction.values()[side1],
                    Direction.values()[side2]
                ))
            }
        } } }

        /**
         * Reverse lookup for [cornersDir].
         *   1st index: primary face
         *   2nd index: undirected corner index.
         *   value: directed corner index
         *   A null value indicates an invalid corner (primary face not shared by corner).
         */
        @JvmField
        val boxCornersDirFromUndir = Array(6) { faceIdx -> Array(8) { undirIdx ->
            val face = Direction.values()[faceIdx]
            val corner = boxCornersUndir[undirIdx]
            if (!corner.contains(face)) null
            else boxCornersDir.findIdx { it.first == face && it.equalsUnordered(corner) }
        } }

        @JvmField
        val forSide = Direction.values().mapArray { AoSideHelper(it) }

        /**
         * Get corner index for vertex coordinates
         */
        @JvmStatic
        fun getCornerUndir(x: Double, y: Double, z: Double): Int {
            var result = 0
            if (x > 0.0) result += 1
            if (y > 0.0) result += 2
            if (z > 0.0) result += 4
            return result
        }
    }
}