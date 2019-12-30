package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.render.LogRegistry
import mods.betterfoliage.client.render.column.ColumnTextureInfo
import mods.betterfoliage.client.render.column.SimpleColumnInfo
import mods.octarinecore.client.render.Quad
import mods.octarinecore.client.render.QuadIconResolver
import mods.octarinecore.client.render.ShadingContext
import mods.octarinecore.client.render.blockContext
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.rotate
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModel
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

@SideOnly(Side.CLIENT)
object IC2RubberIntegration {

    val BlockRubWood = ClassRef("ic2.core.block.BlockRubWood")

    init {
        if (Loader.isModLoaded("ic2") && allAvailable(BlockRubWood)) {
            Client.log(Level.INFO, "IC2 rubber support initialized")
            LogRegistry.addRegistry(IC2LogSupport)
        }
    }
}

@SideOnly(Side.CLIENT)
object TechRebornRubberIntegration {

    val BlockRubberLog = ClassRef("techreborn.blocks.BlockRubberLog")

    init {
        if (Loader.isModLoaded("techreborn") && allAvailable(BlockRubberLog)) {
            Client.log(Level.INFO, "TechReborn rubber support initialized")
            LogRegistry.addRegistry(TechRebornLogSupport)
        }
    }
}

class RubberLogInfo(
    axis: EnumFacing.Axis?,
    val spotDir: EnumFacing,
    topTexture: TextureAtlasSprite,
    bottomTexture: TextureAtlasSprite,
    val spotTexture: TextureAtlasSprite,
    sideTextures: List<TextureAtlasSprite>
) : SimpleColumnInfo(axis, topTexture, bottomTexture, sideTextures) {

    override val side: QuadIconResolver = { ctx: ShadingContext, idx: Int, quad: Quad ->
        val worldFace = (if ((idx and 1) == 0) EnumFacing.SOUTH else EnumFacing.EAST).rotate(ctx.rotation)
        if (worldFace == spotDir) spotTexture else {
            val sideIdx = if (this.sideTextures.size > 1) (blockContext.random(1) + dirToIdx[worldFace.ordinal]) % this.sideTextures.size else 0
            this.sideTextures[sideIdx]
        }
    }

    class Key(override val logger: Logger, val axis: EnumFacing.Axis?, val spotDir: EnumFacing, val textures: List<String>): ModelRenderKey<ColumnTextureInfo> {
        override fun resolveSprites(atlas: TextureMap) = RubberLogInfo(
            axis,
            spotDir,
            atlas[textures[0]] ?: atlas.missingSprite,
            atlas[textures[1]] ?: atlas.missingSprite,
            atlas[textures[2]] ?: atlas.missingSprite,
            textures.drop(3).map { atlas[it] ?: atlas.missingSprite }
        )
    }
}

object IC2LogSupport : ModelRenderRegistryBase<ColumnTextureInfo>() {
    override val logger = BetterFoliageMod.logDetail

    override fun processModel(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): ModelRenderKey<ColumnTextureInfo>? {
        // check for proper block class, existence of ModelBlock, and "state" blockstate property
        if (!IC2RubberIntegration.BlockRubWood.isInstance(state.block)) return null
        val blockLoc = model.modelBlockAndLoc.firstOrNull() ?: return null
        val type = state.properties.entries.find { it.key.getName() == "state" }?.value?.toString() ?: return null

        // logs with no rubber spot
        if (blockLoc.derivesFrom(ResourceLocation("block/cube_column"))) {
            val axis = when(type) {
                "plain_y" -> EnumFacing.Axis.Y
                "plain_x" -> EnumFacing.Axis.X
                "plain_z" -> EnumFacing.Axis.Z
                else -> null
            }
            val textureNames = listOf("end", "end", "side").map { blockLoc.first.resolveTextureName(it) }
            if (textureNames.any { it == "missingno" }) return null
            logger.log(Level.DEBUG, "IC2LogSupport: block state ${state.toString()}")
            logger.log(Level.DEBUG, "IC2LogSupport:             axis=$axis, end=${textureNames[0]}, side=${textureNames[2]}")
            return SimpleColumnInfo.Key(logger, axis, textureNames)
        }

        // logs with rubber spot
        val spotDir = when(type) {
            "dry_north", "wet_north" -> EnumFacing.NORTH
            "dry_south", "wet_south" -> EnumFacing.SOUTH
            "dry_west", "wet_west" -> EnumFacing.WEST
            "dry_east", "wet_east" -> EnumFacing.EAST
            else -> null
        }
        val textureNames = listOf("up", "down", "north", "south").map { blockLoc.first.resolveTextureName(it) }
        if (textureNames.any { it == "missingno" }) return null
        logger.log(Level.DEBUG, "IC2LogSupport: block state ${state.toString()}")
        logger.log(Level.DEBUG, "IC2LogSupport:             spotDir=$spotDir, up=${textureNames[0]}, down=${textureNames[1]}, side=${textureNames[2]}, spot=${textureNames[3]}")
        return if (spotDir != null) RubberLogInfo.Key(logger, EnumFacing.Axis.Y, spotDir, textureNames) else SimpleColumnInfo.Key(logger, EnumFacing.Axis.Y, textureNames)
    }
}

object TechRebornLogSupport : ModelRenderRegistryBase<ColumnTextureInfo>() {
    override val logger = BetterFoliageMod.logDetail

    override fun processModel(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): ModelRenderKey<ColumnTextureInfo>? {
        // check for proper block class, existence of ModelBlock
        if (!TechRebornRubberIntegration.BlockRubberLog.isInstance(state.block)) return null
        val blockLoc = model.modelBlockAndLoc.firstOrNull() ?: return null

        val hasSap = state.properties.entries.find { it.key.getName() == "hassap" }?.value as? Boolean ?: return null
        val sapSide = state.properties.entries.find { it.key.getName() == "sapside" }?.value as? EnumFacing ?: return null

        logger.log(Level.DEBUG, "$logName: block state $state")
        if (hasSap) {
            val textureNames = listOf("end", "end", "sapside", "side").map { blockLoc.first.resolveTextureName(it) }
            logger.log(Level.DEBUG, "$logName:             spotDir=$sapSide, end=${textureNames[0]}, side=${textureNames[2]}, spot=${textureNames[3]}")
            if (textureNames.all { it != "missingno" }) return RubberLogInfo.Key(logger, EnumFacing.Axis.Y, sapSide, textureNames)
        } else {
            val textureNames = listOf("end", "end", "side").map { blockLoc.first.resolveTextureName(it) }
            logger.log(Level.DEBUG, "$logName:             end=${textureNames[0]}, side=${textureNames[2]}")
            if (textureNames.all { it != "missingno" })return SimpleColumnInfo.Key(logger, EnumFacing.Axis.Y, textureNames)
        }
        return null
    }
}