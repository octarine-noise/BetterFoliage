package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCTM
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.HSB
import mods.octarinecore.client.resource.TextureListModelProcessor
import mods.octarinecore.client.resource.TextureMediatedRegistry
import mods.octarinecore.client.resource.averageColor
import mods.octarinecore.client.resource.get
import mods.octarinecore.common.Int3
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.findFirst
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

const val defaultGrassColor = 0

/** Rendering-related information for a grass block. */
class GrassInfo(
    /** Top texture of the grass block. */
    val grassTopTexture: TextureAtlasSprite,

    /**
     * Color to use for Short Grass rendering instead of the biome color.
     *
     * Value is null if the texture is mostly grey (the saturation of its average color is under a configurable limit),
     * the average color of the texture (significantly ) otherwise.
     */
    val overrideColor: Int?
)

interface IGrassRegistry {
    fun get(state: IBlockState): GrassInfo?
    fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing): GrassInfo?
}

/** Collects and manages rendering-related information for grass blocks. */
@SideOnly(Side.CLIENT)
object GrassRegistry : IGrassRegistry {
    val subRegistries: MutableList<IGrassRegistry> = mutableListOf(StandardGrassSupport)

    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing) =
        subRegistries.findFirst { it.get(state, world, pos, face) }

    operator fun get(ctx: BlockContext, face: EnumFacing) = get(ctx.blockState(Int3.zero), ctx.world!!, ctx.pos, face)

    override fun get(state: IBlockState) = subRegistries.findFirst { it.get(state) }
}

object StandardGrassSupport :
    TextureListModelProcessor<TextureAtlasSprite>,
    TextureMediatedRegistry<List<String>, GrassInfo>,
    IGrassRegistry
{
    init { MinecraftForge.EVENT_BUS.register(this) }

    override var stateToKey = mutableMapOf<IBlockState, List<String>>()
    override var stateToValue = mapOf<IBlockState, TextureAtlasSprite>()
    override var textureToValue = mutableMapOf<TextureAtlasSprite, GrassInfo>()

    override val logger = BetterFoliageMod.logDetail
    override val logName = "StandardGrassSupport"
    override val matchClasses: ConfigurableBlockMatcher get() = Config.blocks.grassClasses
    override val modelTextures: List<ModelTextureList> get() = Config.blocks.grassModels.list

    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing): GrassInfo? {
        val baseTexture = stateToValue[state] ?: return null
        return textureToValue[OptifineCTM.override(baseTexture, world, pos, face)] ?: textureToValue[baseTexture]
    }

    override fun get(state: IBlockState) = StandardLeafSupport.stateToValue[state].let {
        if (it == null) null else textureToValue[it]
    }

    override fun processStitch(state: IBlockState, key: List<String>, atlas: TextureMap) = atlas[key[0]]

    override fun processTexture(states: List<IBlockState>, texture: TextureAtlasSprite, atlas: TextureMap) {
        registerGrass(texture, atlas)
        OptifineCTM.getAllCTM(states, texture).forEach {
            registerGrass(it, atlas)
        }
    }

    fun registerGrass(texture: TextureAtlasSprite, atlas: TextureMap) {
        val hsb = HSB.fromColor(texture.averageColor ?: defaultGrassColor)
        val overrideColor = if (hsb.saturation > Config.shortGrass.saturationThreshold) hsb.copy(brightness = 0.8f).asColor else null
        textureToValue[texture] =  GrassInfo(texture, overrideColor)
    }
}