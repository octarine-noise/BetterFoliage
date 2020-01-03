package mods.betterfoliage.client.render.column

import mods.octarinecore.client.render.lighting.QuadIconResolver
import mods.octarinecore.client.resource.ModelRenderKey
import mods.octarinecore.client.resource.get
import mods.octarinecore.client.resource.missingSprite
import mods.octarinecore.common.rotate
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction.*
import org.apache.logging.log4j.Logger

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

    class Key(override val logger: Logger, val axis: Axis?, val textures: List<String>) : ModelRenderKey<ColumnTextureInfo> {
        override fun resolveSprites(atlas: AtlasTexture) = SimpleColumnInfo(
            axis,
            atlas[textures[0]] ?: missingSprite,
            atlas[textures[1]] ?: missingSprite,
            textures.drop(2).map { atlas[it] ?: missingSprite }
        )
    }
}