package mods.betterfoliage.client.texture

import mods.octarinecore.client.resource.resourceManager
import mods.octarinecore.client.resource.get
import mods.octarinecore.client.resource.getLines
import mods.octarinecore.stripStart
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation

class TextureMatcher() {

    data class Mapping(val domain: String?, val path: String, val type: String) {
        fun matches(icon: TextureAtlasSprite): Boolean {
            val iconLocation = ResourceLocation(icon.iconName)
            return (domain == null || domain == iconLocation.resourceDomain) &&
                iconLocation.resourcePath.stripStart("blocks/").contains(path, ignoreCase = true)
        }
    }

    val mappings: MutableList<Mapping> = mutableListOf()

    fun getType(icon: TextureAtlasSprite): String? = mappings.filter { it.matches(icon) }.map { it.type }.firstOrNull()

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