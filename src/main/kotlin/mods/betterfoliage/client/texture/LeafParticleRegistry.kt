package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.resource.Identifier
import mods.betterfoliage.client.resource.Sprite
import mods.octarinecore.client.resource.*
import mods.octarinecore.stripStart
import mods.octarinecore.client.resource.Atlas
import mods.octarinecore.common.sinkAsync
import net.minecraft.client.particle.ParticleManager
import net.minecraft.resources.IResourceManager
import java.util.concurrent.CompletableFuture

class FixedSpriteSet(val sprites: List<Sprite>) : SpriteSet {
    override val num = sprites.size
    override fun get(idx: Int) = sprites[idx % num]
}

object LeafParticleRegistry : AsyncSpriteProvider<ParticleManager> {
    val targetAtlas = Atlas.PARTICLES
    val typeMappings = TextureMatcher()
    val particles = hashMapOf<String, SpriteSet>()

    operator fun get(type: String) = particles[type] ?: particles["default"]!!

    override fun setup(manager: IResourceManager, particleF: CompletableFuture<ParticleManager>, atlasFuture: AtlasFuture): StitchPhases {
        particles.clear()
        val futures = mutableMapOf<String, List<CompletableFuture<Sprite>>>()

        return StitchPhases(
            discovery = particleF.sinkAsync {
                typeMappings.loadMappings(Identifier(BetterFoliageMod.MOD_ID, "leaf_texture_mappings.cfg"))
                (typeMappings.mappings.map { it.type } + "default").distinct().forEach { leafType ->
                    val ids = (0 until 16).map { idx -> Identifier(BetterFoliageMod.MOD_ID, "falling_leaf_${leafType}_$idx") }
                    val wids = ids.map { Atlas.PARTICLES.wrap(it) }
                    futures[leafType] = (0 until 16).map { idx -> Identifier(BetterFoliageMod.MOD_ID, "falling_leaf_${leafType}_$idx") }
                        .filter { manager.hasResource(Atlas.PARTICLES.wrap(it)) }
                        .map { atlasFuture.sprite(it) }
                }
            },
            cleanup = atlasFuture.runAfter {
                futures.forEach { leafType, spriteFutures ->
                    val sprites = spriteFutures.filter { !it.isCompletedExceptionally }.map { it.get() }
                    if (sprites.isNotEmpty()) particles[leafType] = FixedSpriteSet(sprites)
                }
                if (particles["default"] == null) particles["default"] = FixedSpriteSet(listOf(atlasFuture.missing.get()!!))
            }
        )
    }

    fun init() {
        BetterFoliage.particleSprites.providers.add(this)
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