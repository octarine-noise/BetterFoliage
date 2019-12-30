package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.IBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.findFirst
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

const val defaultLeafColor = 0

/** Rendering-related information for a leaf block. */
class LeafInfo(
    /** The generated round leaf texture. */
    val roundLeafTexture: TextureAtlasSprite,

    /** Type of the leaf block (configurable by user). */
    val leafType: String,

    /** Average color of the round leaf texture. */
    val averageColor: Int = roundLeafTexture.averageColor ?: defaultLeafColor
) {
    /** [IconSet] of the textures to use for leaf particles emitted from this block. */
    val particleTextures: IconSet get() = LeafParticleRegistry[leafType]
}

object LeafRegistry : ModelRenderRegistryRoot<LeafInfo>()

object StandardLeafRegistry : ModelRenderRegistryConfigurable<LeafInfo>() {
    override val logger = BetterFoliageMod.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = Config.blocks.leavesClasses
    override val modelTextures: List<ModelTextureList> get() = Config.blocks.leavesModels.list
    override fun processModel(state: IBlockState, textures: List<String>) = StandardLeafKey(logger, textures[0])
}

class StandardLeafKey(override val logger: Logger, val textureName: String) : ModelRenderKey<LeafInfo> {
    lateinit var leafType: String
    lateinit var generated: ResourceLocation

    override fun onPreStitch(atlas: TextureMap) {
        val logName = "StandardLeafKey"
        leafType = LeafParticleRegistry.typeMappings.getType(textureName) ?: "default"
        generated = Client.genLeaves.generatedResource(textureName, "type" to leafType)
        atlas.registerSprite(generated)

        logger.log(Level.DEBUG, "$logName: leaf texture   $textureName")
        logger.log(Level.DEBUG, "$logName:      particle $leafType")
    }

    override fun resolveSprites(atlas: TextureMap) = LeafInfo(atlas[generated] ?: atlas.missingSprite, leafType)
}