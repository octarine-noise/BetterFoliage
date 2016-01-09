package mods.betterfoliage.client.texture

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.resource.BlockTextureInspector
import mods.octarinecore.client.resource.IconSet
import mods.octarinecore.client.resource.averageColor
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Level.WARN

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
    val particleTextures: IconSet get() = LeafRegistry.particles[leafType]!!
}

/** Collects and manages rendering-related information for leaf blocks. */
@SideOnly(Side.CLIENT)
object LeafRegistry : BlockTextureInspector<LeafInfo>() {

    val particles: MutableMap<String, IconSet> = hashMapOf()
    val typeMappings = TextureMatcher().apply { loadMappings(ResourceLocation("betterfoliage", "leafTypeMappings.cfg")) }

    init {
        matchClassAndModel(Config.blocks.leaves, "minecraft:block/leaves", listOf("all"))
    }

    override fun processTextures(textures: List<TextureAtlasSprite>, atlas: TextureMap): LeafInfo {
        val texture = textures[0]
        var leafType = typeMappings.getType(texture) ?: "default"
        val generated = atlas.registerSprite(
            Client.genLeaves.generatedResource(texture.iconName, "type" to leafType)
        )

        if (leafType !in particles.keys) {
            val particleSet = IconSet("betterfoliage", "blocks/falling_leaf_${leafType}_%d")
            particleSet.onStitch(atlas)
            if (particleSet.num == 0) {
                Client.log(WARN, "Leaf particle textures not found for leaf type: $leafType")
                leafType == "default"
            } else {
                particles.put(leafType, particleSet)
            }
        }

        return LeafInfo(generated as TextureAtlasSprite, leafType)
    }
}