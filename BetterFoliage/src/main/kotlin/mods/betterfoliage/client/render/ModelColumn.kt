@file:JvmName("ModelColumn")
package mods.betterfoliage.client.render

import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Double3
import mods.octarinecore.exchange
import net.minecraft.util.EnumFacing.*

/** Weight of the same-side AO values on the outer edges of the 45deg chamfered column faces. */
const val chamferAffinity = 0.9f

/** Amount to shrink column extension bits to stop Z-fighting. */
val zProtectionScale: Double3 get() = Double3(Config.roundLogs.zProtection, 1.0, Config.roundLogs.zProtection)

fun Model.columnSide(radius: Double, yBottom: Double, yTop: Double, transform: (Quad) -> Quad = { it }) {
    val halfRadius = radius * 0.5
    listOf(
        verticalRectangle(x1 = 0.0, z1 = 0.5, x2 = 0.5 - radius, z2 = 0.5, yBottom = yBottom, yTop = yTop)
        .clampUV(minU = 0.0, maxU = 0.5 - radius)
        .setAoShader(faceOrientedInterpolate(overrideFace = SOUTH))
        .setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y)), predicate = { v, vi -> vi == 1 || vi == 2}),

        verticalRectangle(x1 = 0.5 - radius, z1 = 0.5, x2 = 0.5 - halfRadius, z2 = 0.5 - halfRadius, yBottom = yBottom, yTop = yTop)
        .clampUV(minU = 0.5 - radius)
        .setAoShader(
            faceOrientedAuto(overrideFace = SOUTH, corner = cornerInterpolate(Axis.Y, chamferAffinity, Config.roundLogs.dimming))
        )
        .setAoShader(
            faceOrientedAuto(overrideFace = SOUTH, corner = cornerInterpolate(Axis.Y, 0.5f, Config.roundLogs.dimming)),
            predicate = { v, vi -> vi == 1 || vi == 2}
        )
    ).forEach { transform(it.setFlatShader(FaceFlat(SOUTH))).add() }

    listOf(
        verticalRectangle(x1 = 0.5 - halfRadius, z1 = 0.5 - halfRadius, x2 = 0.5, z2 = 0.5 - radius, yBottom = yBottom, yTop = yTop)
        .clampUV(maxU = radius - 0.5)
        .setAoShader(
            faceOrientedAuto(overrideFace = EAST, corner = cornerInterpolate(Axis.Y, chamferAffinity, Config.roundLogs.dimming)))
        .setAoShader(
            faceOrientedAuto(overrideFace = EAST, corner = cornerInterpolate(Axis.Y, 0.5f, Config.roundLogs.dimming)),
            predicate = { v, vi -> vi == 0 || vi == 3}
        ),

        verticalRectangle(x1 = 0.5, z1 = 0.5 - radius, x2 = 0.5, z2 = 0.0, yBottom = yBottom, yTop = yTop)
        .clampUV(minU = radius - 0.5, maxU = 0.0)
        .setAoShader(faceOrientedInterpolate(overrideFace = EAST))
        .setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y)), predicate = { v, vi -> vi == 0 || vi == 3})
    ).forEach { transform(it.setFlatShader(FaceFlat(EAST))).add() }

    quads.exchange(1, 2)
}

/**
 * Create a model of the side of a square column quadrant.
 *
 * @param[transform] transformation to apply to the model
 */
fun Model.columnSideSquare(yBottom: Double, yTop: Double, transform: (Quad) -> Quad = { it }) {
    listOf(
        verticalRectangle(x1 = 0.0, z1 = 0.5, x2 = 0.5, z2 = 0.5, yBottom = yBottom, yTop = yTop)
        .clampUV(minU = 0.0)
        .setAoShader(faceOrientedInterpolate(overrideFace = SOUTH))
        .setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y)), predicate = { v, vi -> vi == 1 || vi == 2}),

        verticalRectangle(x1 = 0.5, z1 = 0.5, x2 = 0.5, z2 = 0.0, yBottom = yBottom, yTop = yTop)
        .clampUV(maxU = 0.0)
        .setAoShader(faceOrientedInterpolate(overrideFace = EAST))
        .setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y)), predicate = { v, vi -> vi == 0 || vi == 3})
    ).forEach {
        transform(it.setFlatShader(faceOrientedAuto(corner = cornerFlat))).add()
    }
}

/**
 * Create a model of the top lid of a chamfered column quadrant.
 *
 * @param[radius] the chamfer radius
 * @param[transform] transformation to apply to the model
 */
fun Model.columnLid(radius: Double, transform: (Quad)->Quad = { it }) {
    val v1 = Vertex(Double3(0.0, 0.5, 0.0), UV(0.0, 0.0))
    val v2 = Vertex(Double3(0.0, 0.5, 0.5), UV(0.0, 0.5))
    val v3 = Vertex(Double3(0.5 - radius, 0.5, 0.5), UV(0.5 - radius, 0.5))
    val v4 = Vertex(Double3(0.5 - radius * 0.5, 0.5, 0.5 - radius * 0.5), UV(0.5, 0.5))
    val v5 = Vertex(Double3(0.5, 0.5, 0.5 - radius), UV(0.5, 0.5 - radius))
    val v6 = Vertex(Double3(0.5, 0.5, 0.0), UV(0.5, 0.0))

    val q1 = Quad(v1, v2, v3, v4).setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
             .transformVI { vertex, idx -> vertex.copy(aoShader = when(idx) {
                 0 -> FaceCenter(UP)
                 1 -> EdgeInterpolateFallback(UP, SOUTH, 0.0)
                 else -> vertex.aoShader
             })}
    val q2 = Quad(v1, v4, v5, v6).setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
             .transformVI { vertex, idx -> vertex.copy(aoShader = when(idx) {
                 0 -> FaceCenter(UP)
                 3 -> EdgeInterpolateFallback(UP, EAST, 0.0)
                 else -> vertex.aoShader
             })}
    listOf(q1, q2).forEach { transform(it.setFlatShader(FaceFlat(UP))).add() }
}

/**
 * Create a model of the top lid of a square column quadrant.
 *
 * @param[transform] transformation to apply to the model
 */
fun Model.columnLidSquare(transform: (Quad)-> Quad = { it }) {
    transform(
        horizontalRectangle(x1 = 0.0, x2 = 0.5, z1 = 0.0, z2 = 0.5, y = 0.5)
        .transformVI { vertex, idx -> vertex.copy(uv = UV(vertex.xyz.x, vertex.xyz.z), aoShader = when(idx) {
            0 -> FaceCenter(UP)
            1 -> EdgeInterpolateFallback(UP, SOUTH, 0.0)
            2 -> CornerSingleFallback(UP, SOUTH, EAST, UP)
            else -> EdgeInterpolateFallback(UP, EAST, 0.0)
        }) }
        .setFlatShader(FaceFlat(UP))
    ).add()
}

/**
 * Transform a chamfered side quadrant model of a column that extends from the top of the block.
 * (clamp UV coordinates, apply some scaling to avoid Z-fighting).
 *
 * @param[size] amount that the model extends from the top
 */
fun topExtension(size: Double) = { q: Quad ->
    q.clampUV(minV = 0.5 - size).transformVI { vertex, idx ->
        if (idx < 2) vertex else vertex.copy(xyz = vertex.xyz * zProtectionScale)
    }
}
/**
 * Transform a chamfered side quadrant model of a column that extends from the bottom of the block.
 * (clamp UV coordinates, apply some scaling to avoid Z-fighting).
 *
 * @param[size] amount that the model extends from the bottom
 */
fun bottomExtension(size: Double) = { q: Quad ->
    q.clampUV(maxV = -0.5 + size).transformVI { vertex, idx ->
        if (idx > 1) vertex else vertex.copy(xyz = vertex.xyz * zProtectionScale)
    }
}
