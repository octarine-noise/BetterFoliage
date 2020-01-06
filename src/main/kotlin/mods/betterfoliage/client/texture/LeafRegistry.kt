package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.resource.Identifier
import mods.octarinecore.HasLogger
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import java.util.concurrent.CompletableFuture

const val defaultLeafColor = 0

/** Rendering-related information for a leaf block. */
class LeafInfo(
    /** The generated round leaf texture. */
    val roundLeafTexture: TextureAtlasSprite,

    /** Type of the leaf block (configurable by user). */
    val leafType: String,

    /** Average color of the round leaf texture. */
    val averageColor: Int = roundLeafTexture.averageColor
) {
    /** [IconSet] of the textures to use for leaf particles emitted from this block. */
    val particleTextures: SpriteSet get() = LeafParticleRegistry[leafType]
}

object LeafRegistry : ModelRenderRegistryRoot<LeafInfo>()

object AsyncLeafDiscovery : ConfigurableModelDiscovery<LeafInfo>() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.leafBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.leafModels.modelList

    override fun processModel(state: BlockState, textures: List<String>, atlas: AtlasFuture) = defaultRegisterLeaf(Identifier(textures[0]), atlas)

    fun init() {
        LeafRegistry.registries.add(this)
        BetterFoliage.blockSprites.providers.add(this)
    }
}

fun HasLogger.defaultRegisterLeaf(sprite: Identifier, atlas: AtlasFuture): CompletableFuture<LeafInfo> {
    val leafType = LeafParticleRegistry.typeMappings.getType(sprite) ?: "default"
    val generated = GeneratedLeaf(sprite, leafType).register(BetterFoliage.asyncPack)
    val roundLeaf = atlas.sprite(generated)

    log(" leaf texture $sprite")
    log("     particle $leafType")
    return atlas.mapAfter {
        LeafInfo(roundLeaf.get(), leafType)
    }
}