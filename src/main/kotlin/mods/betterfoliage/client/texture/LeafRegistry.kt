package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
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
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.leafBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.leafModels.modelList
    override fun processModel(state: BlockState, textures: List<String>) = StandardLeafKey(logger, textures[0])
    init { BetterFoliage.modBus.register(this) }
}

class StandardLeafKey(override val logger: Logger, val textureName: String) : ModelRenderKey<LeafInfo> {
    lateinit var leafType: String
    lateinit var generated: ResourceLocation

    override fun onPreStitch(event: TextureStitchEvent.Pre) {
        val logName = "StandardLeafKey"
        leafType = LeafParticleRegistry.typeMappings.getType(textureName) ?: "default"
        generated = Client.genLeaves.register(ResourceLocation(textureName), leafType)
        event.addSprite(generated)

        logger.log(Level.DEBUG, "$logName: leaf texture   $textureName")
        logger.log(Level.DEBUG, "$logName:      particle $leafType")
    }

    override fun resolveSprites(atlas: AtlasTexture) = LeafInfo(atlas[generated] ?: missingSprite, leafType)
}