package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.IColumnRegistry
import mods.betterfoliage.client.render.IColumnTextureInfo
import mods.betterfoliage.client.render.LogRegistry
import mods.betterfoliage.client.render.StaticColumnInfo
import mods.betterfoliage.client.texture.LeafRegistry
import mods.betterfoliage.client.texture.StandardLeafSupport
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.render.Quad
import mods.octarinecore.client.render.ShadingContext
import mods.octarinecore.client.render.blockContext
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.rotate
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.MethodRef
import mods.octarinecore.metaprog.allAvailable
import mods.octarinecore.tryDefault
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModel
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

@SideOnly(Side.CLIENT)
object IC2Integration {

    val BlockRubWood = ClassRef("ic2.core.block.BlockRubWood")

    init {
        if (Loader.isModLoaded("IC2") && allAvailable(BlockRubWood)) {
            Client.log(Level.INFO, "IC2 support initialized")
            LogRegistry.subRegistries.add(IC2LogSupport)
        }
    }
}

@SideOnly(Side.CLIENT)
object TechRebornIntegration {

    val BlockRubberLog = ClassRef("techreborn.blocks.BlockRubberLog")
    val ITexturedBlock = ClassRef("me.modmuss50.jsonDestroyer.api.ITexturedBlock")
    val getTextureNameFromState = MethodRef(ITexturedBlock, "getTextureNameFromState", Refs.String, Refs.IBlockState, Refs.EnumFacing)

    val rubberLogTextureNames = listOf(
        "techreborn:blocks/rubber_log_top",
        "techreborn:blocks/rubber_log_top",
        "techreborn:blocks/rubber_log_side",
        "techreborn:blocks/rubber_log_sap"
    )

    init {
        if (Loader.isModLoaded("techreborn") && allAvailable(BlockRubberLog, ITexturedBlock, getTextureNameFromState)) {
            Client.log(Level.INFO, "TechReborn support initialized")
            LogRegistry.subRegistries.add(TechRebornLogSupport)

            // initialize object but don't add to registry
            TechRebornLeafSupport.toString()
        }
    }
}

@SideOnly(Side.CLIENT)
data class RubberLogModelInfo(
    val axis: EnumFacing.Axis?,
    val spotDir: EnumFacing?,
    val textures: List<String>
)

// TODO avoid copy-paste pattern with regards to StaticColumnInfo
@SideOnly(Side.CLIENT)
data class RubberLogColumnInfo(override val axis: EnumFacing.Axis?,
                               val spotDir: EnumFacing,
                               val topTexture: TextureAtlasSprite,
                               val bottomTexture: TextureAtlasSprite,
                               val sideTexture: TextureAtlasSprite,
                               val spotTexture: TextureAtlasSprite): IColumnTextureInfo {
    override val top = { ctx: ShadingContext, idx: Int, quad: Quad ->
        OptifineCTM.override(topTexture, blockContext, EnumFacing.UP.rotate(ctx.rotation))
    }
    override val bottom = { ctx: ShadingContext, idx: Int, quad: Quad ->
        OptifineCTM.override(bottomTexture, blockContext, EnumFacing.DOWN.rotate(ctx.rotation))
    }
    override val side = { ctx: ShadingContext, idx: Int, quad: Quad ->
        val worldRelativeSide = (if ((idx and 1) == 0) EnumFacing.SOUTH else EnumFacing.EAST).rotate(ctx.rotation)
        val texture = if (worldRelativeSide == spotDir) spotTexture else sideTexture
        OptifineCTM.override(texture, blockContext, worldRelativeSide)
    }
}

@SideOnly(Side.CLIENT)
abstract class RubberLogSupportBase : ModelProcessor<RubberLogModelInfo, IColumnTextureInfo>, IColumnRegistry {

    override var stateToKey = mutableMapOf<IBlockState, RubberLogModelInfo>()
    override var stateToValue = mapOf<IBlockState, IColumnTextureInfo>()

    override val logger = BetterFoliageMod.logDetail

    init { MinecraftForge.EVENT_BUS.register(this) }

    override fun processStitch(state: IBlockState, key: RubberLogModelInfo, atlas: TextureMap): IColumnTextureInfo? {
        val topTex = atlas.registerSprite(key.textures[0])
        val bottomTex = atlas.registerSprite(key.textures[1])
        val sideTex = atlas.registerSprite(key.textures[2])
        if (key.spotDir == null)
            return StaticColumnInfo(key.axis, topTex, bottomTex, sideTex)
        else {
            val spotTex = atlas.registerSprite(key.textures[3])
            return RubberLogColumnInfo(key.axis, key.spotDir, topTex, bottomTex, sideTex, spotTex)
        }
    }

    override fun get(state: IBlockState) = stateToValue[state]
}

@SideOnly(Side.CLIENT)
object IC2LogSupport : RubberLogSupportBase() {

    override fun processModelLoad(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): RubberLogModelInfo? {
        // check for proper block class, existence of ModelBlock, and "state" blockstate property
        if (!IC2Integration.BlockRubWood.isInstance(state.block)) return null
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
            logger.log(Level.DEBUG, "IC2LogSupport: block state ${state.toString()}")
            logger.log(Level.DEBUG, "IC2LogSupport:             axis=$axis, end=${textureNames[0]}, side=${textureNames[2]}")
            return if (textureNames.all { it != "missingno" }) RubberLogModelInfo(axis, null, textureNames) else null
        }

        // logs with rubber spot
        val spotDir = when(type) {
            "dry_north", "wet_north" -> EnumFacing.NORTH
            "dry_south", "wet_south" -> EnumFacing.SOUTH
            "dry_west", "wet_west" -> EnumFacing.WEST
            "dry_east", "wet_east" -> EnumFacing.EAST
            else -> null
        }
        val textureNames = listOf("up", "down", "south", "north").map { blockLoc.first.resolveTextureName(it) }
        logger.log(Level.DEBUG, "IC2LogSupport: block state ${state.toString()}")
        logger.log(Level.DEBUG, "IC2LogSupport:             spotDir=$spotDir, up=${textureNames[0]}, down=${textureNames[1]}, side=${textureNames[2]}, spot=${textureNames[3]}")
        return if (textureNames.all { it != "missingno" }) RubberLogModelInfo(EnumFacing.Axis.Y, spotDir, textureNames) else null
    }
}

@SideOnly(Side.CLIENT)
object TechRebornLogSupport : RubberLogSupportBase() {

    override fun processModelLoad(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): RubberLogModelInfo? {
        // check for proper block class, existence of ModelBlock
        if (!TechRebornIntegration.BlockRubberLog.isInstance(state.block)) return null

        val hasSap = state.properties.entries.find { it.key.getName() == "hassap" }?.value as? Boolean ?: return null
        val sapSide = state.properties.entries.find { it.key.getName() == "sapside" }?.value as? EnumFacing ?: return null

        logger.log(Level.DEBUG, "TechRebornLogSupport: block state ${state.toString()}")
        if (hasSap) {
            val textureNames = listOf(EnumFacing.UP, EnumFacing.DOWN, sapSide.opposite, sapSide).map {
                TechRebornIntegration.getTextureNameFromState.invoke(state.block, state, it) as String
            }
            logger.log(Level.DEBUG, "TechRebornLogSupport:             spotDir=$sapSide, up=${textureNames[0]}, down=${textureNames[1]}, side=${textureNames[2]}, spot=${textureNames[3]}")
            return if (textureNames.all { it != "missingno" }) RubberLogModelInfo(EnumFacing.Axis.Y, sapSide, textureNames) else null
        } else {
            val textureNames = listOf(EnumFacing.UP, EnumFacing.DOWN, sapSide).map {
                TechRebornIntegration.getTextureNameFromState.invoke(state.block, state, it) as String
            }
            logger.log(Level.DEBUG, "TechRebornLogSupport:             up=${textureNames[0]}, down=${textureNames[1]}, side=${textureNames[2]}")
            return if (textureNames.all { it != "missingno" }) RubberLogModelInfo(EnumFacing.Axis.Y, null, textureNames) else null
        }
    }
}

@SideOnly(Side.CLIENT)
object TechRebornLeafSupport : ModelProcessor<Nothing, Nothing> {

    init { MinecraftForge.EVENT_BUS.register(this) }

    override var stateToKey = mutableMapOf<IBlockState, Nothing>()
    override var stateToValue = mapOf<IBlockState, Nothing>()
    override val logger: Logger get() = BetterFoliageMod.logDetail

    override fun processModelLoad(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): Nothing? {
        if (Config.blocks.leavesClasses.matchesClass(state.block) && TechRebornIntegration.ITexturedBlock.isInstance(state.block)) {
            val textureName = TechRebornIntegration.getTextureNameFromState.invoke(state.block, state, EnumFacing.UP) as String
            logger.log(Level.DEBUG, "TechRebornLeafSupport: block state ${state.toString()}")
            logger.log(Level.DEBUG, "TechRebornLeafSupport:             texture=$textureName")
            // register directly into StandardLeafSupport for the sake of simplicity
            StandardLeafSupport.stateToKey[state] = listOf(textureName)
        }
        return null
    }

    // no-op
    override fun processStitch(state: IBlockState, key: Nothing, atlas: TextureMap) = null
}