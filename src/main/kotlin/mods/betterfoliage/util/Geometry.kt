package mods.betterfoliage.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis.*
import net.minecraft.util.math.Direction.AxisDirection
import net.minecraft.util.math.Direction.AxisDirection.*
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3f

// ================================
// Axes and directions
// ================================
val axes = listOf(X, Y, Z)
val axisDirs = listOf(POSITIVE, NEGATIVE)
val allDirections = Direction.values()
val horizontalDirections = listOf(NORTH, SOUTH, EAST, WEST)
val allDirOffsets = allDirections.map { Int3(it) }
val Pair<Direction.Axis, AxisDirection>.face: Direction get() = when(this) {
    X to POSITIVE -> EAST; X to NEGATIVE -> WEST
    Y to POSITIVE -> UP; Y to NEGATIVE -> DOWN
    Z to POSITIVE -> SOUTH; else -> NORTH
}
val Direction.perpendiculars: List<Direction> get() =
    axes.filter { it != this.axis }.cross(axisDirs).map { it.face }
val Direction.offset: Int3 get() = allDirOffsets[ordinal]

/** Old ForgeDirection rotation matrix yanked from 1.7.10 */
val ROTATION_MATRIX: Array<IntArray> get() = arrayOf(
    intArrayOf(0, 1, 4, 5, 3, 2, 6),
    intArrayOf(0, 1, 5, 4, 2, 3, 6),
    intArrayOf(5, 4, 2, 3, 0, 1, 6),
    intArrayOf(4, 5, 2, 3, 1, 0, 6),
    intArrayOf(2, 3, 1, 0, 4, 5, 6),
    intArrayOf(3, 2, 0, 1, 4, 5, 6)
)

// ================================
// Vectors
// ================================
operator fun Direction.times(scale: Double) =
    Double3(vector.x.toDouble() * scale, vector.y.toDouble() * scale, vector.z.toDouble() * scale)
val Direction.vec: Double3 get() = Double3(vector.x.toDouble(), vector.y.toDouble(), vector.z.toDouble())

operator fun BlockPos.plus(other: Int3) = BlockPos(x + other.x, y + other.y, z + other.z)

/** 3D vector of [Double]s. Offers both mutable operations, and immutable operations in operator notation. */
data class Double3(var x: Double, var y: Double, var z: Double) {
    constructor(x: Float, y: Float, z: Float) : this(x.toDouble(), y.toDouble(), z.toDouble())
    constructor(dir: Direction) : this(dir.vector.x.toDouble(), dir.vector.y.toDouble(), dir.vector.z.toDouble())
    companion object {
        val zero: Double3 get() = Double3(0.0, 0.0, 0.0)
        fun weight(v1: Double3, weight1: Double, v2: Double3, weight2: Double) =
            Double3(v1.x * weight1 + v2.x * weight2, v1.y * weight1 + v2.y * weight2, v1.z * weight1 + v2.z * weight2)
        fun lerp(delta: Double, first: Double3, second: Double3) = first + (second - first) * delta
    }

    // immutable operations
    operator fun plus(other: Double3) = Double3(x + other.x, y + other.y, z + other.z)
    operator fun unaryMinus() = Double3(-x, -y, -z)
    operator fun minus(other: Double3) = Double3(x - other.x, y - other.y, z - other.z)
    operator fun times(scale: Double) = Double3(x * scale, y * scale, z * scale)
    operator fun times(other: Double3) = Double3(x * other.x, y * other.y, z * other.z)

    /** Rotate this vector, and return coordinates in the unrotated frame */
    fun rotate(rot: Rotation) = Double3(
        rot.rotatedComponent(EAST, x, y, z),
        rot.rotatedComponent(UP, x, y, z),
        rot.rotatedComponent(SOUTH, x, y, z)
    )

    /** Rotate vector by the given [Quaternion] */
    fun rotate(quat: Quaternion) =
        quat.copy()
            .apply { hamiltonProduct(Quaternion(this@Double3.x.toFloat(), this@Double3.y.toFloat(), this@Double3.z.toFloat(), 0.0F)) }
            .apply { hamiltonProduct(quat.copy().apply(Quaternion::conjugate)) }
            .let { Double3(it.x, it.y, it.z) }

    // mutable operations
    fun setTo(other: Double3): Double3 { x = other.x; y = other.y; z = other.z; return this }
    fun setTo(x: Double, y: Double, z: Double): Double3 { this.x = x; this.y = y; this.z = z; return this }
    fun setTo(x: Float, y: Float, z: Float) = setTo(x.toDouble(), y.toDouble(), z.toDouble())
    fun add(other: Double3): Double3 { x += other.x; y += other.y; z += other.z; return this }
    fun add(x: Double, y: Double, z: Double): Double3 { this.x += x; this.y += y; this.z += z; return this }
    fun sub(other: Double3): Double3 { x -= other.x; y -= other.y; z -= other.z; return this }
    fun sub(x: Double, y: Double, z: Double): Double3 { this.x -= x; this.y -= y; this.z -= z; return this }
    fun invert(): Double3 { x = -x; y = -y; z = -z; return this }
    fun mul(scale: Double): Double3 { x *= scale; y *= scale; z *= scale; return this }
    fun mul(other: Double3): Double3 { x *= other.x; y *= other.y; z *= other.z; return this }
    fun rotateMut(rot: Rotation): Double3 {
        val rotX = rot.rotatedComponent(EAST, x, y, z)
        val rotY = rot.rotatedComponent(UP, x, y, z)
        val rotZ = rot.rotatedComponent(SOUTH, x, y, z)
        return setTo(rotX, rotY, rotZ)
    }

    // misc operations
    infix fun dot(other: Double3) = x * other.x + y * other.y + z * other.z
    infix fun cross(o: Double3) = Double3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    val length: Double get() = Math.sqrt(x * x + y * y + z * z)
    val normalize: Double3 get() = (1.0 / length).let { Double3(x * it, y * it, z * it) }
    val nearestCardinal: Direction get() = nearestAngle(this, allDirections.asIterable()) { it.vec }.first
    val asVec3f: Vec3f get() = Vec3f(x.toFloat(), y.toFloat(), z.toFloat())
}

/** 3D vector of [Int]s. Offers both mutable operations, and immutable operations in operator notation. */
data class Int3(var x: Int, var y: Int, var z: Int) {
    constructor(dir: Direction) : this(dir.vector.x, dir.vector.y, dir.vector.z)
    constructor(offset: Pair<Int, Direction>) : this(
        offset.first * offset.second.vector.x,
        offset.first * offset.second.vector.y,
        offset.first * offset.second.vector.z
    )
    companion object {
        val zero = Int3(0, 0, 0)
    }

    // immutable operations
    operator fun plus(other: Int3) = Int3(x + other.x, y + other.y, z + other.z)
    operator fun plus(other: Pair<Int, Direction>) = Int3(
        x + other.first * other.second.vector.x,
        y + other.first * other.second.vector.y,
        z + other.first * other.second.vector.z
    )
    operator fun unaryMinus() = Int3(-x, -y, -z)
    operator fun minus(other: Int3) = Int3(x - other.x, y - other.y, z - other.z)
    operator fun times(scale: Int) = Int3(x * scale, y * scale, z * scale)
    operator fun times(other: Int3) = Int3(x * other.x, y * other.y, z * other.z)

    /** Rotate this vector, and return coordinates in the unrotated frame */
    fun rotate(rot: Rotation) = Int3(
        rot.rotatedComponent(EAST, x, y, z),
        rot.rotatedComponent(UP, x, y, z),
        rot.rotatedComponent(SOUTH, x, y, z)
    )

    // mutable operations
    fun setTo(other: Int3): Int3 { x = other.x; y = other.y; z = other.z; return this }
    fun setTo(x: Int, y: Int, z: Int): Int3 { this.x = x; this.y = y; this.z = z; return this }
    fun add(other: Int3): Int3 { x += other.x; y += other.y; z += other.z; return this }
    fun sub(other: Int3): Int3 { x -= other.x; y -= other.y; z -= other.z; return this }
    fun invert(): Int3 { x = -x; y = -y; z = -z; return this }
    fun mul(scale: Int): Int3 { x *= scale; y *= scale; z *= scale; return this }
    fun mul(other: Int3): Int3 { x *= other.x; y *= other.y; z *= other.z; return this }
    fun rotateMut(rot: Rotation): Int3 {
        val rotX = rot.rotatedComponent(EAST, x, y, z)
        val rotY = rot.rotatedComponent(UP, x, y, z)
        val rotZ = rot.rotatedComponent(SOUTH, x, y, z)
        return setTo(rotX, rotY, rotZ)
    }
}

// ================================
// Rotation
// ================================
val Direction.rotations: Array<Direction> get() =
    Array(6) { idx -> Direction.values()[ROTATION_MATRIX[ordinal][idx]] }
fun Direction.rotate(rot: Rotation) = rot.forward[ordinal]
fun rot(axis: Direction) = Rotation.rot90[axis.ordinal]

/**
 * Class representing an arbitrary rotation (or combination of rotations) around cardinal axes by 90 degrees.
 * In effect, a permutation of [ForgeDirection]s.
 */
@Suppress("NOTHING_TO_INLINE")
class Rotation(val forward: Array<Direction>, val reverse: Array<Direction>) {
    operator fun plus(other: Rotation) = Rotation(
        Array(6) { idx -> forward[other.forward[idx].ordinal] },
        Array(6) { idx -> other.reverse[reverse[idx].ordinal] }
    )
    operator fun unaryMinus() = Rotation(reverse, forward)
    operator fun times(num: Int) = when(num % 4) { 1 -> this; 2 -> this + this; 3 -> -this; else -> identity
    }

    inline fun rotatedComponent(dir: Direction, x: Int, y: Int, z: Int) =
        when(reverse[dir.ordinal]) { EAST -> x; WEST -> -x; UP -> y; DOWN -> -y; SOUTH -> z; NORTH -> -z; else -> 0 }
    inline fun rotatedComponent(dir: Direction, x: Double, y: Double, z: Double) =
        when(reverse[dir.ordinal]) { EAST -> x; WEST -> -x; UP -> y; DOWN -> -y; SOUTH -> z; NORTH -> -z; else -> 0.0 }

    companion object {
        // Forge rotation matrix is left-hand
        val rot90 = Array(6) { idx -> Rotation(allDirections[idx].opposite.rotations, allDirections[idx].rotations) }
        val identity = Rotation(allDirections, allDirections)
        val fromUp = arrayOf(
            rot90[EAST.ordinal] * 2,
            identity,
            rot90[WEST.ordinal],
            rot90[EAST.ordinal],
            rot90[SOUTH.ordinal],
            rot90[NORTH.ordinal]
        )
    }
}

// ================================
// Miscellaneous
// ================================

inline operator fun <reified T> Array<T>.get(face: Direction): T = get(face.ordinal)

data class BoxFace(val top: Direction, val left: Direction) {
    val bottom get() = top.opposite
    val right get() = left.opposite
    val allCorners = listOf(top to left, top to left.opposite, top.opposite to left, top.opposite to left.opposite)

    val tl get() = top to left
    val tr get() = top to right
    val bl get() = bottom to left
    val br get() = bottom to right
}
val boxFaces = allDirections.map { when(it) {
    DOWN -> BoxFace(SOUTH, WEST)
    UP -> BoxFace(SOUTH, EAST)
    NORTH -> BoxFace(WEST, UP)
    SOUTH -> BoxFace(UP, WEST)
    WEST -> BoxFace(SOUTH, UP)
    EAST -> BoxFace(SOUTH, DOWN)
}}.toTypedArray()

/** List of all 12 box edges, represented as a [Pair] of [ForgeDirection]s */
val boxEdges = allDirections.flatMap { face1 -> allDirections.filter { it.axis > face1.axis }.map { face1 to it } }

/**
 * Get the closest object to the specified point from a list of objects.
 *
 * @param[vertex] the reference point
 * @param[objs] list of geomertric objects
 * @param[objPos] lambda to calculate the position of an object
 * @return [Pair] of (object, distance)
 */
fun <T> nearestPosition(vertex: Double3, objs: Iterable<T>, objPos: (T)-> Double3): Pair<T, Double>  =
        objs.map { it to (objPos(it) - vertex).length }.minByOrNull { it.second }!!

/**
 * Get the object closest in orientation to the specified vector from a list of objects.
 *
 * @param[vector] the reference vector (direction)
 * @param[objs] list of geomertric objects
 * @param[objAngle] lambda to calculate the orientation of an object
 * @return [Pair] of (object, normalized dot product)
 */
fun <T> nearestAngle(vector: Double3, objs: Iterable<T>, objAngle: (T)-> Double3): Pair<T, Double> =
        objs.map { it to objAngle(it).dot(vector) }.maxByOrNull { it.second }!!
