package mods.betterfoliage.texture

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.get
import mods.betterfoliage.util.getLines
import mods.betterfoliage.util.resourceManager
import mods.betterfoliage.util.stripStart
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import java.util.concurrent.CompletableFuture

object LeafParticleRegistry {
    val targetAtlas = Atlas.PARTICLES
    val typeMappings = TextureMatcher()
//    val particles = hashMapOf<String, SpriteSet>()

    val futures = mutableMapOf<String, List<CompletableFuture<TextureAtlasSprite>>>()

//    operator fun get(type: String) = particles[type] ?: particles["default"]!!

    fun discovery() {
        typeMappings.loadMappings(ResourceLocation(BetterFoliageMod.MOD_ID, "leaf_texture_mappings.cfg"))
        (typeMappings.mappings.map { it.type } + "default").distinct().forEach { leafType ->
            val ids = (0 until 16).map { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "falling_leaf_${leafType}_$idx") }
            val wids = ids.map { Atlas.PARTICLES.file(it) }
//            futures[leafType] = (0 until 16).map { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "falling_leaf_${leafType}_$idx") }
//                .filter { manager.hasResource(Atlas.PARTICLES.wrap(it)) }
//                .map { atlasFuture.sprite(it) }
        }
    }

    fun cleanup() {
//        futures.forEach { leafType, spriteFutures ->
//            val sprites = spriteFutures.filter { !it.isCompletedExceptionally }.map { it.get() }
//            if (sprites.isNotEmpty()) particles[leafType] = FixedSpriteSet(sprites)
//        }
//        if (particles["default"] == null) particles["default"] = FixedSpriteSet(listOf(atlasFuture.missing.get()!!))
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