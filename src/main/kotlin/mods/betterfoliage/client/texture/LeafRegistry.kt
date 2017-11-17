package mods.betterfoliage.client.texture

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.EventPriority
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCTM
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

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handleTextureReload(event: TextureStitchEvent.Pre) {
        if (event.map.textureType != 0) return
        leaves.clear()
        particles.clear()
        typeMappings.loadMappings(ResourceLocation("betterfoliage", "leafTextureMappings.cfg"))
        Client.log(INFO, "Generating leaf textures")

        IconSet("betterfoliage", "falling_leaf_default_%d").let {
            it.onStitch(event.map)
            particles.put("default", it)
        }

        //OptifineIntegration.dumpCTMData()

        Block.blockRegistry.forEach { block ->
            if (Config.blocks.leaves.matchesClass(block as Block)) {
                block.registerBlockIcons { location ->
                    val original = event.map.getTextureExtry(location)
                    Client.log(INFO, "Found leaf texture: $location")
                    registerLeaf(event.map, original)

                    if (OptifineCTM.isAvailable) OptifineCTM.getAllCTM(original).let { ctmIcons ->
                        if (ctmIcons.isNotEmpty()) {
                            Client.log(INFO, "Found ${ctmIcons.size} CTM variants for texture ${original.iconName}")
                            ctmIcons.forEach { registerLeaf(event.map, it as TextureAtlasSprite) }
                        }
                    }
                    return@registerBlockIcons original
                }

                if (OptifineCTM.isAvailable) OptifineCTM.getAllCTM(block).let { ctmIcons ->
                    if (ctmIcons.isNotEmpty()) {
                        Client.log(INFO, "Found ${ctmIcons.size} CTM variants for block ${Block.getIdFromBlock(block)}")
                        ctmIcons.forEach { registerLeaf(event.map, it as TextureAtlasSprite) }
                    }
                }
            }
        }

        listOf("deciduous", "conifers", "jungle", "willow", "maple", "palm").forEach { leafType ->
            listOf("plain", "fancy", "changed").forEach { renderType ->
                val location = "forestry:leaves/$leafType.$renderType"
                val original = event.map.getTextureExtry(location)
                if (original != null) {
                    Client.log(INFO, "Found Forestry leaf texture: $location")
                    registerLeaf(event.map, original)
                }
            }
        }
    }

    fun registerLeaf(atlas: TextureMap, icon: TextureAtlasSprite) {
        var leafType = typeMappings.getType(icon) ?: "default"
        val generated = atlas.registerIcon(
            Client.genLeaves.generatedResource(icon.iconName, "type" to leafType).toString()
        )
        leafType = registerParticle(atlas, leafType)
        leaves.put(icon, LeafInfo(generated as TextureAtlasSprite, leafType))
    }

    fun registerParticle(atlas: TextureMap, leafType: String): String {
        if (leafType !in particles.keys) {
            val particleSet = IconSet("betterfoliage", "falling_leaf_${leafType}_%d")
            particleSet.onStitch(atlas)
            if (particleSet.num == 0) {
                Client.log(WARN, "Leaf particle textures not found for leaf type: $leafType")
                return "default"
            } else {
                particles.put(leafType, particleSet)
            }
        }
        return leafType
    }
}