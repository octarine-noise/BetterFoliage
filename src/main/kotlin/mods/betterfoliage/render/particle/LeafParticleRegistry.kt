package mods.betterfoliage.render.particle

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.resource.model.FixedSpriteSet
import mods.betterfoliage.resource.model.SpriteSet
import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.Identifier

object LeafParticleRegistry : ClientSpriteRegistryCallback {
    val typeMappings = TextureMatcher()

    val ids = mutableMapOf<String, List<Identifier>>()
    val spriteSets = mutableMapOf<String, SpriteSet>()

    override fun registerSprites(atlasTexture: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry) {
        ids.clear()
        spriteSets.clear()
        typeMappings.loadMappings(Identifier(BetterFoliage.MOD_ID, "leaf_texture_mappings.cfg"))
        (typeMappings.mappings.map { it.type } + "default").distinct().forEach { leafType ->
            val validIds = (0 until 16).map { idx -> Identifier(BetterFoliage.MOD_ID, "falling_leaf_${leafType}_$idx") }
                .filter { resourceManager.containsResource(Atlas.PARTICLES.wrap(it)) }
            ids[leafType] = validIds
            validIds.forEach { registry.register(it) }
        }
    }

    operator fun get(type: String): SpriteSet {
        spriteSets[type]?.let { return it }
        ids[type]?.let {
            return FixedSpriteSet(Atlas.PARTICLES, it).apply { spriteSets[type] = this }
        }
        return if (type == "default") FixedSpriteSet(Atlas.PARTICLES, emptyList()).apply { spriteSets[type] = this }
            else get("default")
    }

    init {
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.PARTICLE_ATLAS_TEX).register(this)
    }
}

class TextureMatcher {

    data class Mapping(val domain: String?, val path: String, val type: String) {
        fun matches(iconLocation: Identifier): Boolean {
            return (domain == null || domain == iconLocation.namespace) &&
                iconLocation.path.stripStart("blocks/").contains(path, ignoreCase = true)
        }
    }

    val mappings: MutableList<Mapping> = mutableListOf()

    fun getType(resource: Identifier) = mappings.filter { it.matches(resource) }.map { it.type }.firstOrNull()
    fun getType(iconName: String) = Identifier(iconName).let { getType(it) }

    fun loadMappings(mappingLocation: Identifier) {
        mappings.clear()
        resourceManager[mappingLocation]?.getLines()?.let { lines ->
            lines.filter { !it.startsWith("//") }.filter { it.isNotEmpty() }.forEach { line ->
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