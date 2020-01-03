package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliage
import mods.octarinecore.client.resource.*
import mods.octarinecore.stripStart
import mods.octarinecore.client.resource.Atlas
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object LeafParticleRegistry {
    val targetAtlas = Atlas.PARTICLES
    val typeMappings = TextureMatcher()
    val particles = hashMapOf<String, IconSet>()

    operator fun get(type: String) = particles[type] ?: particles["default"]!!

    init { BetterFoliage.modBus.register(this) }

    @SubscribeEvent
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        if (!targetAtlas.matches(event)) return

        particles.clear()
        typeMappings.loadMappings(ResourceLocation(BetterFoliage.MOD_ID, "leaf_texture_mappings.cfg"))

        val allTypes = (typeMappings.mappings.map { it.type } + "default").distinct()
        allTypes.forEach { leafType ->
            val particleSet = IconSet(Atlas.PARTICLES) {
                idx -> ResourceLocation(BetterFoliage.MOD_ID, "falling_leaf_${leafType}_$idx")
            }.apply { onPreStitch(event) }
            if (leafType == "default" || particleSet.num > 0) particles[leafType] = particleSet
        }
    }

    @SubscribeEvent
    fun handlePostStitch(event: TextureStitchEvent.Post) {
        if (!targetAtlas.matches(event)) return
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