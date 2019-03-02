package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCTM
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.HSB
import mods.octarinecore.client.resource.*
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
import org.apache.logging.log4j.Level
import java.lang.Math.min

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
    operator fun get(state: IBlockState, rand: Int): GrassInfo?
    operator fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing, rand: Int): GrassInfo?
}

/** Collects and manages rendering-related information for grass blocks. */
@SideOnly(Side.CLIENT)
object GrassRegistry : IGrassRegistry {
    val subRegistries: MutableList<IGrassRegistry> = mutableListOf(StandardGrassSupport)

    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing, rand: Int) =
        subRegistries.findFirst { it.get(state, world, pos, face, rand) }

    operator fun get(ctx: BlockContext, face: EnumFacing) = get(ctx.blockState(Int3.zero), ctx.world!!, ctx.pos, face, ctx.random(0))

    override fun get(state: IBlockState, rand: Int) = subRegistries.findFirst { it[state, rand] }
}

object StandardGrassSupport :
    TextureListModelProcessor<TextureAtlasSprite>,
    TextureMediatedRegistry<List<String>, GrassInfo>,
    IGrassRegistry
{
    init { MinecraftForge.EVENT_BUS.register(this) }

    override var variants = mutableMapOf<IBlockState, MutableList<ModelVariant>>()
    override var variantToKey = mutableMapOf<ModelVariant, List<String>>()
    override var variantToValue = mapOf<ModelVariant, TextureAtlasSprite>()
    override var textureToValue = mutableMapOf<TextureAtlasSprite, GrassInfo>()

    override val logger = BetterFoliageMod.logDetail
    override val logName = "StandardGrassSupport"
    override val matchClasses: ConfigurableBlockMatcher get() = Config.blocks.grassClasses
    override val modelTextures: List<ModelTextureList> get() = Config.blocks.grassModels.list

    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing, rand: Int): GrassInfo? {
        val variant = getVariant(state, rand) ?: return null
        val baseTexture = variantToValue[variant] ?: return null
        return textureToValue[baseTexture]
    }

    override fun get(state: IBlockState, rand: Int): GrassInfo? {
        val variant = getVariant(state, rand) ?: return null
        return variantToValue[variant].let { if (it == null) null else textureToValue[it] }
    }

    override fun processStitch(variant: ModelVariant, key: List<String>, atlas: TextureMap) = atlas.registerSprite(key[0])
    override fun processTexture(variants: List<ModelVariant>, texture: TextureAtlasSprite, atlas: TextureMap) { registerGrass(texture, atlas) }

    fun registerGrass(texture: TextureAtlasSprite, atlas: TextureMap) {
        logger.log(Level.DEBUG, "$logName: texture ${texture.iconName}")
        val hsb = HSB.fromColor(texture.averageColor ?: defaultGrassColor)
        val overrideColor = if (hsb.saturation >= Config.shortGrass.saturationThreshold) {
            logger.log(Level.DEBUG, "$logName:         brightness ${hsb.brightness}")
            logger.log(Level.DEBUG, "$logName:         saturation ${hsb.saturation} >= ${Config.shortGrass.saturationThreshold}, using texture color")
            hsb.copy(brightness = min(0.9f, hsb.brightness * 2.0f)).asColor
        } else {
            logger.log(Level.DEBUG, "$logName:         saturation ${hsb.saturation} < ${Config.shortGrass.saturationThreshold}, using block color")
            null
        }

        textureToValue[texture] =  GrassInfo(texture, overrideColor)
    }
}