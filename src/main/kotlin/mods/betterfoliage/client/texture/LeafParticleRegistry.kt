package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliageMod
import mods.octarinecore.client.resource.*
import mods.octarinecore.stripStart
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeafParticleRegistry {
    val typeMappings = TextureMatcher()
    val particles = hashMapOf<String, IconSet>()

    operator fun get(type: String) = particles[type] ?: particles["default"]!!

    init { MinecraftForge.EVENT_BUS.register(this) }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun handleLoadModelData(event: LoadModelDataEvent) {
        particles.clear()
        typeMappings.loadMappings(ResourceLocation(BetterFoliageMod.DOMAIN, "leaf_texture_mappings.cfg"))
    }

    @SubscribeEvent
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        val allTypes = (typeMappings.mappings.map { it.type } + "default").distinct()
        allTypes.forEach { leafType ->
            val particleSet = IconSet("betterfoliage", "blocks/falling_leaf_${leafType}_%d").apply { onPreStitch(event.map) }
            if (leafType == "default" || particleSet.num > 0) particles[leafType] = particleSet
        }
    }

    @SubscribeEvent
    fun handlePostStitch(event: TextureStitchEvent.Post) {
        particles.forEach { (_, particleSet) -> particleSet.onPostStitch(event.map) }
    }
}

class TextureMatcher {

    data class Mapping(val domain: String?, val path: String, val type: String) {
        fun matches(iconLocation: ResourceLocation): Boolean {
            return (domain == null || domain == iconLocation.namespace) &&
                iconLocation.path.stripStart("blocks/").contains(path, ignoreCase = true)
        }
    }

    val mappings: MutableList<Mapping> = mutableListOf()

    fun getType(resource: ResourceLocation) = mappings.filter { it.matches(resource) }.map { it.type }.firstOrNull()
    fun getType(iconName: String) = ResourceLocation(iconName).let { getType(it) }

    fun loadMappings(mappingLocation: ResourceLocation) {
        mappings.clear()
        resourceManager[mappingLocation]?.getLines()?.let { lines ->
            lines.filter { !it.startsWith("//") }.filter { !it.isEmpty() }.forEach { line ->
                val line2 = line.trim().split('=')
                if (line2.size == 2) {
                    val mapping = line2[0].trim().split(':')
                    if (mapping.size == 1) mappings.add(Mapping(null, mapping[0].trim(), line2[1].trim()))
                    else if (mapping.size == 2) mappings.add(Mapping(mapping[0].trim(), mapping[1].trim(), line2[1].trim()))
                }
            }
        }
    }
}