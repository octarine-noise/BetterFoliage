package mods.octarinecore.client.render

import mods.octarinecore.common.*
import mods.octarinecore.minmax
import mods.octarinecore.replace
import net.minecraft.util.EnumFacing
import java.lang.Math.max
import java.lang.Math.min

/**
 * Vertex UV coordinates
 *
 * Zero-centered: coordinates fall between (-0.5, 0.5) (inclusive)
 */
data class UV(val u: Double, val v: Double) {
    companion object {
        val topLeft = UV(-0.5, -0.5)
        val topRight = UV(0.5, -0.5)
        val bottomLeft = UV(-0.5, 0.5)
        val bottomRight = UV(0.5, 0.5)
    }

    val rotate: UV get() = UV(v, -u)

    fun rotate(n: Int) = when(n % 4) {
        0 -> copy()
        1 -> UV(v, -u)
        2 -> UV(-u, -v)
        else -> UV(-v, u)
    }

    fun clamp(minU: Double = -0.5, maxU: Double = 0.5, minV: Double = -0.5, maxV: Double = 0.5) =
        UV(u.minmax(minU, maxU), v.minmax(minV, maxV))

    fun mirror(mirrorU: Boolean, mirrorV: Boolean) = UV(if (mirrorU) -u else u, if (mirrorV) -v else v)
}

/**
 * Model vertex
 *
 * @param[xyz] x, y, z coordinates
 * @param[uv] u, v coordinates
 * @param[aoShader] [Shader] instance to use with AO rendering
 * @param[flatShader] [Shader] instance to use with non-AO rendering
 */
data class Vertex(val xyz: Double3 = Double3(0.0, 0.0, 0.0),
                  val uv: UV = UV(0.0, 0.0),
                  val aoShader: Shader = NoShader,
                  val flatShader: Shader = NoShader)

/**
 * Model quad
 */
data class Quad(val v1: Vertex, val v2: Vertex, val v3: Vertex, val v4: Vertex) {
    val verts = arrayOf(v1, v2, v3, v4)
    inline fun transformV(trans: (Vertex)->Vertex): Quad = transformVI { vertex, idx -> trans(vertex) }
    inline fun transformVI(trans: (Vertex, Int)->Vertex): Quad =
            Quad(trans(v1, 0), trans(v2, 1), trans(v3, 2), trans(v4, 3))
    val normal: Double3 get() = (v2.xyz - v1.xyz).cross(v4.xyz - v1.xyz).normalize

    fun move(trans: Double3) = transformV { it.copy(xyz = it.xyz + trans) }
    fun move(trans: Pair<Double, EnumFacing>) = move(Double3(trans.second) * trans.first)
    fun scale (scale: Double) = transformV { it.copy(xyz = it.xyz * scale) }
    fun scale (scale: Double3) = transformV { it.copy(xyz = Double3(it.xyz.x * scale.x, it.xyz.y * scale.y, it.xyz.z * scale.z)) }
    fun scaleUV (scale: Double) = transformV { it.copy(uv = UV(it.uv.u * scale, it.uv.v * scale)) }
    fun rotate(rot: Rotation) = transformV {
        it.copy(xyz = it.xyz.rotate(rot), aoShader = it.aoShader.rotate(rot), flatShader = it.flatShader.rotate(rot))
    }
    fun rotateUV(n: Int) = transformV { it.copy(uv = it.uv.rotate(n)) }
    fun clampUV(minU: Double = -0.5, maxU: Double = 0.5, minV: Double = -0.5, maxV: Double = 0.5) =
        transformV { it.copy(uv = it.uv.clamp(minU, maxU, minV, maxV)) }
    fun mirrorUV(mirrorU: Boolean, mirrorV: Boolean) = transformV { it.copy(uv = it.uv.mirror(mirrorU, mirrorV)) }
    fun setAoShader(resolver: (Quad, Vertex)->Shader, predicate: (Vertex, Int)->Boolean = { v, vi -> true }) =
        transformVI { vertex, idx ->
            if (!predicate(vertex, idx)) vertex else vertex.copy(aoShader = resolver(this@Quad, vertex))
        }
    fun setFlatShader(resolver: (Quad, Vertex)->Shader, predicate: (Vertex, Int)->Boolean = { v, vi -> true }) =
        transformVI { vertex, idx ->
            if (!predicate(vertex, idx)) vertex else vertex.copy(flatShader = resolver(this@Quad, vertex))
        }
    fun setFlatShader(shader: Shader) = transformVI { vertex, idx -> vertex.copy(flatShader = shader) }
    val flipped: Quad get() = Quad(v4, v3, v2, v1)

}

/**
 * Model. The basic unit of rendering blocks with OctarineCore.
 *
 * The model should be positioned so that (0,0,0) is the block center.
 * The block extends to (-0.5, 0.5) in all directions (inclusive).
 */
class Model() {
    constructor(other: List<Quad>) : this() { quads.addAll(other) }
    val quads = mutableListOf<Quad>()

    fun Quad.add() = quads.add(this)
    fun Iterable<Quad>.addAll() = forEach { quads.add(it) }

    fun transformQ(trans: (Quad)->Quad) = quads.replace(trans)
    fun transformV(trans: (Vertex)->Vertex) = quads.replace{ it.transformV(trans) }

    fun verticalRectangle(x1: Double, z1: Double, x2: Double, z2: Double, yBottom: Double, yTop: Double) = Quad(
            Vertex(Double3(x1, yBottom, z1), UV.bottomLeft),
            Vertex(Double3(x2, yBottom, z2), UV.bottomRight),
            Vertex(Double3(x2, yTop, z2), UV.topRight),
            Vertex(Double3(x1, yTop, z1), UV.topLeft)
    )

    fun horizontalRectangle(x1: Double, z1: Double, x2: Double, z2: Double, y: Double): Quad {
        val xMin = min(x1, x2); val xMax = max(x1, x2)
        val zMin = min(z1, z2); val zMax = max(z1, z2)
        return Quad(
            Vertex(Double3(xMin, y, zMin), UV.topLeft),
            Vertex(Double3(xMin, y, zMax), UV.bottomLeft),
            Vertex(Double3(xMax, y, zMax), UV.bottomRight),
            Vertex(Double3(xMax, y, zMin), UV.topRight)
        )
    }

    fun faceQuad(face: EnumFacing): Quad {
        val base = face.vec * 0.5
        val top = faceCorners[face.ordinal].topLeft.first.vec * 0.5
        val left = faceCorners[face.ordinal].topLeft.second.vec * 0.5
        return Quad(
                Vertex(base + top + left, UV.topLeft),
                Vertex(base - top + left, UV.bottomLeft),
                Vertex(base - top - left, UV.bottomRight),
                Vertex(base + top - left, UV.topRight)
        )
    }
}

val fullCube = Model().apply {
    forgeDirs.forEach {
        faceQuad(it)
        .setAoShader(faceOrientedAuto(corner = cornerAo(it.axis), edge = null))
        .setFlatShader(faceOrientedAuto(corner = cornerFlat, edge = null))
        .add()
    }
}