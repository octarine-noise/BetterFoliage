package mods.betterfoliage.client.render.column

import mods.octarinecore.client.render.QuadIconResolver
import mods.octarinecore.client.render.blockContext
import mods.octarinecore.client.resource.ModelRenderKey
import mods.octarinecore.client.resource.get
import mods.octarinecore.common.rotate
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Logger

@SideOnly(Side.CLIENT)
interface ColumnTextureInfo {
    val axis: EnumFacing.Axis?
    val top: QuadIconResolver
    val bottom: QuadIconResolver
    val side: QuadIconResolver
}

@SideOnly(Side.CLIENT)
open class SimpleColumnInfo(
    override val axis: EnumFacing.Axis?,
    val topTexture: TextureAtlasSprite,
    val bottomTexture: TextureAtlasSprite,
    val sideTextures: List<TextureAtlasSprite>
) : ColumnTextureInfo {

    // index offsets for EnumFacings, to make it less likely for neighboring faces to get the same bark texture
    val dirToIdx = arrayOf(0, 1, 2, 4, 3, 5)

    override val top: QuadIconResolver = { _, _, _ -> topTexture }
    override val bottom: QuadIconResolver = { _, _, _ -> bottomTexture }
    override val side: QuadIconResolver = { ctx, idx, _ ->
        val worldFace = (if ((idx and 1) == 0) EnumFacing.SOUTH else EnumFacing.EAST).rotate(ctx.rotation)
        val sideIdx = if (sideTextures.size > 1) (blockContext.random(1) + dirToIdx[worldFace.ordinal]) % sideTextures.size else 0
        sideTextures[sideIdx]
    }

    class Key(override val logger: Logger, val axis: EnumFacing.Axis?, val textures: List<String>) : ModelRenderKey<ColumnTextureInfo> {
        override fun resolveSprites(atlas: TextureMap) = SimpleColumnInfo(
            axis,
            atlas[textures[0]] ?: atlas.missingSprite,
            atlas[textures[1]] ?: atlas.missingSprite,
            textures.drop(2).map { atlas[it] ?: atlas.missingSprite }
        )
    }
}