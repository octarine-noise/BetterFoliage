package mods.betterfoliage.render.particle

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.FixedSpriteSet
import mods.betterfoliage.model.SpriteSet
import mods.betterfoliage.resource.VeryEarlyReloadListener
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.get
import mods.betterfoliage.util.getLines
import mods.betterfoliage.util.resourceManager
import mods.betterfoliage.util.stripStart
import net.minecraft.client.renderer.texture.MissingTextureSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.apache.logging.log4j.Level.INFO

interface LeafBlockModel {
    val key: LeafParticleKey
}

interface LeafParticleKey {
    val leafType: String
    val tintIndex: Int
    val avgColor: Color
}

object LeafParticleRegistry : HasLogger(), VeryEarlyReloadListener {
    val typeMappings = TextureMatcher()
    val allTypes get() = (typeMappings.mappings.map { it.type } + "default").distinct()

    val particles = hashMapOf<String, SpriteSet>()

    operator fun get(type: String) = particles[type] ?: particles["default"]!!

    override fun onReloadStarted() {
        typeMappings.loadMappings(ResourceLocation(BetterFoliageMod.MOD_ID, "leaf_texture_mappings.cfg"))
        detailLogger.log(INFO, "Loaded leaf particle mappings, types = [${allTypes.joinToString(", ")}]")
    }

    @SubscribeEvent
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        if (event.map.location() == Atlas.PARTICLES.resourceId) {
            allTypes.forEach { leafType ->
                val locations = (0 until 16).map { idx ->
                    ResourceLocation(BetterFoliageMod.MOD_ID, "particle/falling_leaf_${leafType}_$idx")
                }.filter { resourceManager.hasResource(Atlas.PARTICLES.file(it)) }

                detailLogger.log(INFO, "Registering sprites for leaf particle type [$leafType], ${locations.size} sprites found")
                locations.forEach { event.addSprite(it) }
            }
        }
    }

    @SubscribeEvent
    fun handlePostStitch(event: TextureStitchEvent.Post) {
        if (event.map.location() == Atlas.PARTICLES.resourceId) {
            (typeMappings.mappings.map { it.type } + "default").distinct().forEach { leafType ->
                val sprites = (0 until 16).map { idx ->
                    ResourceLocation(BetterFoliageMod.MOD_ID, "particle/falling_leaf_${leafType}_$idx")
                }
                    .map { event.map.getSprite(it) }
                    .filter { it !is MissingTextureSprite }
                detailLogger.log(INFO, "Leaf particle type [$leafType], ${sprites.size} sprites in atlas")
                particles[leafType] = FixedSpriteSet(sprites)
            }
        }
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
                    else if (mapping.size == 2) mappings.add(
                        Mapping(
                            mapping[0].trim(),
                            mapping[1].trim(),
                            line2[1].trim()
                        )
                    )
                }
            }
        }
    }
}