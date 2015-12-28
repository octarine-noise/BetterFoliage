package mods.octarinecore.client.render

import mods.octarinecore.client.render.Axis.*
import mods.octarinecore.client.render.Dir.N
import mods.octarinecore.client.render.Dir.P
import mods.octarinecore.cross
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.common.util.ForgeDirection.*

// ================================
// Axes and directions
// ================================
enum class Axis { X, Y, Z }
enum class Dir { P, N }
val axes = listOf(X, Y, Z)
val axisDirs = listOf(P, N)
val forgeDirs = ForgeDirection.VALID_DIRECTIONS
val forgeDirOffsets = forgeDirs.map { Int3(it) }
val ForgeDirection.axis: Axis get() = when(this) {EAST, WEST -> X; UP, DOWN -> Y; else -> Z }
val ForgeDirection.dir: Dir get() = when(this) {UP, SOUTH, EAST -> P; else -> N }
val Pair<Axis, Dir>.face: ForgeDirection get() = when(this) {
    X to P -> EAST; X to N -> WEST; Y to P -> UP; Y to N -> DOWN; Z to P -> SOUTH; Z to N -> NORTH; else -> UNKNOWN
}
val ForgeDirection.perpendiculars: List<ForgeDirection> get() =
    axes.filter { it != this.axis }.cross(axisDirs).map { it.face }
val ForgeDirection.offset: Int3 get() = forgeDirOffsets[ordinal]

// ================================
// Vectors
// ================================
operator fun ForgeDirection.times(scale: Double) =
    Double3(offsetX.toDouble() * scale, offsetY.toDouble() * scale, offsetZ.toDouble() * scale)
val ForgeDirection.vec: Double3 get() = Double3(offsetX.toDouble(), offsetY.toDouble(), offsetZ.toDouble())

/** 3D vector of [Double]s. Offers both mutable operations, and immutable operations in operator notation. */
data class Double3(var x: Double, var y: Double, var z: Double) {
    constructor(x: Float, y: Float, z: Float) : this(x.toDouble(), y.toDouble(), z.toDouble())
    constructor(dir: ForgeDirection) : this(dir.offsetX.toDouble(), dir.offsetY.toDouble(), dir.offsetZ.toDouble())
    companion object {
        val zero: Double3 get() = Double3(0.0, 0.0, 0.0)
        fun weight(v1: Double3, weight1: Double, v2: Double3, weight2: Double) =
            Double3(v1.x * weight1 + v2.x * weight2, v1.y * weight1 + v2.y * weight2, v1.z * weight1 + v2.z * weight2)
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
    val nearestCardinal: ForgeDirection get() = nearestAngle(this, forgeDirs.asIterable()) { it.vec }.first
}

/** 3D vector of [Int]s. Offers both mutable operations, and immutable operations in operator notation. */
data class Int3(var x: Int, var y: Int, var z: Int) {
    constructor(dir: ForgeDirection) : this(dir.offsetX, dir.offsetY, dir.offsetZ)
    constructor(offset: Pair<Int, ForgeDirection>) : this(
        offset.first * offset.second.offsetX,
        offset.first * offset.second.offsetY,
        offset.first * offset.second.offsetZ
    )
    companion object {
        val zero = Int3(0, 0, 0)
    }

    // immutable operations
    operator fun plus(other: Int3) = Int3(x + other.x, y + other.y, z + other.z)
    operator fun plus(other: Pair<Int, ForgeDirection>) = Int3(
        x + other.first * other.second.offsetX,
        y + other.first * other.second.offsetY,
        z + other.first * other.second.offsetZ
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
val ForgeDirection.rotations: Array<ForgeDirection> get() =
    Array(6) { idx -> ForgeDirection.values()[ForgeDirection.ROTATION_MATRIX[ordinal][idx]] }
fun ForgeDirection.rotate(rot: Rotation) = rot.forward[ordinal]
fun rot(axis: ForgeDirection) = Rotation.rot90[axis.ordinal]

/**
 * Class representing an arbitrary rotation (or combination of rotations) around cardinal axes by 90 degrees.
 * In effect, a permutation of [ForgeDirection]s.
 */
@Suppress("NOTHING_TO_INLINE")
class Rotation(val forward: Array<ForgeDirection>, val reverse: Array<ForgeDirection>) {
    operator fun plus(other: Rotation) = Rotation(
        Array(6) { idx -> forward[other.forward[idx].ordinal] },
        Array(6) { idx -> other.reverse[reverse[idx].ordinal] }
    )
    operator fun unaryMinus() = Rotation(reverse, forward)
    operator fun times(num: Int) = when(num % 4) { 1 -> this; 2 -> this + this; 3 -> -this; else -> identity }

    inline fun rotatedComponent(dir: ForgeDirection, x: Int, y: Int, z: Int) =
        when(reverse[dir.ordinal]) { EAST -> x; WEST -> -x; UP -> y; DOWN -> -y; SOUTH -> z; NORTH -> -z; else -> 0 }
    inline fun rotatedComponent(dir: ForgeDirection, x: Double, y: Double, z: Double) =
        when(reverse[dir.ordinal]) { EAST -> x; WEST -> -x; UP -> y; DOWN -> -y; SOUTH -> z; NORTH -> -z; else -> 0.0 }

    companion object {
        // Forge rotation matrix is left-hand
        val rot90 = Array(6) { idx -> Rotation(forgeDirs[idx].opposite.rotations, forgeDirs[idx].rotations) }
        val identity = Rotation(forgeDirs, forgeDirs)
    }

}

// ================================
// Miscellaneous
// ================================
/** List of all 12 box edges, represented as a [Pair] of [ForgeDirection]s */
val boxEdges = forgeDirs.flatMap { face1 -> forgeDirs.filter { it.axis > face1.axis }.map { face1 to it } }

/**
 * Get the closest object to the specified point from a list of objects.
 *
 * @param[vertex] the reference point
 * @param[objs] list of geomertric objects
 * @param[objPos] lambda to calculate the position of an object
 * @return [Pair] of (object, distance)
 */
fun <T> nearestPosition(vertex: Double3, objs: Iterable<T>, objPos: (T)->Double3): Pair<T, Double>  =
        objs.map { it to (objPos(it) - vertex).length }.minBy { it.second }!!

/**
 * Get the object closest in orientation to the specified vector from a list of objects.
 *
 * @param[vector] the reference vector (direction)
 * @param[objs] list of geomertric objects
 * @param[objAngle] lambda to calculate the orientation of an object
 * @return [Pair] of (object, normalized dot product)
 */
fun <T> nearestAngle(vector: Double3, objs: Iterable<T>, objAngle: (T)->Double3): Pair<T, Double> =
        objs.map { it to objAngle(it).dot(vector) }.maxBy { it.second }!!

data class FaceCorners(val topLeft: Pair<ForgeDirection, ForgeDirection>,
                       val topRight: Pair<ForgeDirection, ForgeDirection>,
                       val bottomLeft: Pair<ForgeDirection, ForgeDirection>,
                       val bottomRight: Pair<ForgeDirection, ForgeDirection>) {
    constructor(top: ForgeDirection, left: ForgeDirection) :
    this(top to left, top to left.opposite, top.opposite to left, top.opposite to left.opposite)

    val asArray = arrayOf(topLeft, topRight, bottomLeft, bottomRight)
    val asList = listOf(topLeft, topRight, bottomLeft, bottomRight)
}

val faceCorners = forgeDirs.map { when(it) {
    DOWN -> FaceCorners(SOUTH, WEST)
    UP -> FaceCorners(SOUTH, EAST)
    NORTH -> FaceCorners(WEST, UP)
    SOUTH -> FaceCorners(UP, WEST)
    WEST -> FaceCorners(SOUTH, UP)
    EAST ->FaceCorners(SOUTH, DOWN)
    else -> FaceCorners(UNKNOWN, UNKNOWN)
}}

