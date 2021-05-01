package mods.betterfoliage.render.column

import mods.betterfoliage.render.lighting.QuadIconResolver
import mods.betterfoliage.util.rotate
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction.*

interface ColumnTextureInfo {
    val axis: Axis?
    val top: QuadIconResolver
    val bottom: QuadIconResolver
    val side: QuadIconResolver
}

open class SimpleColumnInfo(
    override val axis: Axis?,
    val topTexture: TextureAtlasSprite,
    val bottomTexture: TextureAtlasSprite,
    val sideTextures: List<TextureAtlasSprite>
) : ColumnTextureInfo {

    // index offsets for EnumFacings, to make it less likely for neighboring faces to get the same bark texture
    val dirToIdx = arrayOf(0, 1, 2, 4, 3, 5)

    override val top: QuadIconResolver = { _, _, _ -> topTexture }
    override val bottom: QuadIconResolver = { _, _, _ -> bottomTexture }
    override val side: QuadIconResolver = { ctx, idx, _ ->
        val worldFace = (if ((idx and 1) == 0) SOUTH else EAST).rotate(ctx.modelRotation)
        val sideIdx = if (sideTextures.size > 1) (ctx.semiRandom(1) + dirToIdx[worldFace.ordinal]) % sideTextures.size else 0
        sideTextures[sideIdx]
    }
}