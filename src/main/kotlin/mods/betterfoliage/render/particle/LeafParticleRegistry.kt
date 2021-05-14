package mods.betterfoliage.render.particle

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.model.FixedSpriteSet
import mods.betterfoliage.model.SpriteSet
import mods.betterfoliage.resource.VeryEarlyReloadListener
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.get
import mods.betterfoliage.util.getLines
import mods.betterfoliage.util.resourceManager
import mods.betterfoliage.util.stripStart
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import kotlin.collections.MutableList
import kotlin.collections.distinct
import kotlin.collections.filter
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.plus
import kotlin.collections.set

object LeafParticleRegistry : HasLogger(), ClientSpriteRegistryCallback, VeryEarlyReloadListener {
    val typeMappings = TextureMatcher()
    val allTypes get() = (typeMappings.mappings.map { it.type } + "default").distinct()

    val spriteSets = mutableMapOf<String, SpriteSet>()

    override fun getFabricId() = Identifier(BetterFoliage.MOD_ID, "leaf-particles")

    override fun onReloadStarted(resourceManager: ResourceManager) {
        typeMappings.loadMappings(Identifier(BetterFoliage.MOD_ID, "leaf_texture_mappings.cfg"))
        detailLogger.log(Level.INFO, "Loaded leaf particle mappings, types = [${allTypes.joinToString(", ")}]")
    }

    override fun registerSprites(atlasTexture: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry) {
        spriteSets.clear()
        allTypes.forEach { leafType ->
            val validIds = (0 until 16).map { idx -> Identifier(BetterFoliage.MOD_ID, "particle/falling_leaf_${leafType}_$idx") }
                .filter { resourceManager.containsResource(Atlas.PARTICLES.file(it)) }
            validIds.forEach { registry.register(it) }
        }
    }

    operator fun get(leafType: String): SpriteSet {
        spriteSets[leafType]?.let { return it }

        val sprites = (0 until 16)
            .map { idx -> Identifier(BetterFoliage.MOD_ID, "particle/falling_leaf_${leafType}_$idx") }
            .map { Atlas.PARTICLES[it] }
            .filter { it !is MissingSprite }

        detailLogger.log(Level.INFO, "Leaf particle type [$leafType], ${sprites.size} sprites in atlas")
        if (sprites.isNotEmpty()) return FixedSpriteSet(sprites).apply { spriteSets[leafType] = this }

        return if (leafType == "default")
            FixedSpriteSet(listOf(Atlas.PARTICLES[MissingSprite.getMissingSpriteId()])).apply { spriteSets[leafType] = this }
        else
            get("default").apply { spriteSets[leafType] = this }
    }

    init {
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE).register(this)
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