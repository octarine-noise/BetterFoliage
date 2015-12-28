package mods.betterfoliage.client.texture

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.resource.IconSet
import mods.octarinecore.client.resource.averageColor
import net.minecraft.block.Block
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.IIcon
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Level.*

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
object LeafRegistry {

    val leaves: MutableMap<IIcon, LeafInfo> = hashMapOf()
    val particles: MutableMap<String, IconSet> = hashMapOf()
    val typeMappings = TextureMatcher()

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun handleTextureReload(event: TextureStitchEvent.Pre) {
        if (event.map.textureType != 0) return
        leaves.clear()
        particles.clear()
        typeMappings.loadMappings(ResourceLocation("betterfoliage", "leafTypeMappings.cfg"))
        Client.log(INFO, "Generating leaf textures")

        IconSet("betterfoliage", "falling_leaf_default_%d").let {
            it.onStitch(event.map)
            particles.put("default", it)
        }

        Block.blockRegistry.forEach { block ->
            if (Config.blocks.leaves.matchesClass(block as Block)) {
                block.registerBlockIcons { location ->
                    val original = event.map.getTextureExtry(location)
                    Client.log(DEBUG, "Found leaf texture: $location")
                    registerLeaf(event.map, original)
                    return@registerBlockIcons original
                }
            }
        }
    }

    fun registerLeaf(atlas: TextureMap, icon: TextureAtlasSprite) {
        var leafType = typeMappings.getType(icon) ?: "default"
        val generated = atlas.registerIcon(
            Client.genLeaves.generatedResource(icon.iconName, "type" to leafType).toString()
        )

        if (leafType !in particles.keys) {
            val particleSet = IconSet("betterfoliage", "falling_leaf_${leafType}_%d")
            particleSet.onStitch(atlas)
            if (particleSet.num == 0) {
                Client.log(WARN, "Leaf particle textures not found for leaf type: $leafType")
                leafType == "default"
            } else {
                particles.put(leafType, particleSet)
            }
        }

        val leafInfo = LeafInfo(generated as TextureAtlasSprite, leafType)
        leaves.put(icon, leafInfo)
    }
}