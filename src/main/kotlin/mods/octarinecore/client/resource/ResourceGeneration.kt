package mods.octarinecore.client.resource

import net.minecraft.client.Minecraft
import net.minecraft.resources.*
import net.minecraft.resources.ResourcePackType.CLIENT_RESOURCES
import net.minecraft.resources.data.IMetadataSectionSerializer
import net.minecraft.resources.data.PackMetadataSection
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.resource.IResourceType
import net.minecraftforge.resource.ISelectiveResourceReloadListener
import java.io.InputStream
import java.util.*
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * [IResourcePack] containing generated resources
 *
 * @param[name] Name of the resource pack
 * @param[generators] List of resource generators
 */
class GeneratorPack(val packName: String, val packDescription: String, val packImage: String) : IResourcePack {

    val generators = mutableListOf<GeneratorBase<*>>()

    val packFinder = Finder(this)
    override fun getName() = packName
    override fun getResourceNamespaces(type: ResourcePackType) = if (type == CLIENT_RESOURCES) generators.map { it.namespace }.toSet() else emptySet()

    override fun <T : Any?> getMetadata(deserializer: IMetadataSectionSerializer<T>): T? {
        if (deserializer.sectionName != "pack") return null
        return PackMetadataSection(StringTextComponent(packDescription), 4) as? T
    }

    override fun resourceExists(type: ResourcePackType, location: ResourceLocation?) =
        location != null &&
        type == CLIENT_RESOURCES &&
        generators.find { it.namespace == location.namespace && it.resourceExists(location) } != null

    override fun getResourceStream(type: ResourcePackType, location: ResourceLocation) =
        if (location != null && type == CLIENT_RESOURCES)
            generators.firstOrNull { it.namespace == location.namespace && it.resourceExists(location) }?.getInputStream(location)
        else
            null

    override fun getAllResourceLocations(type: ResourcePackType, pathIn: String, maxDepth: Int, filter: Predicate<String>) = emptyList<ResourceLocation>()
    override fun getRootResourceStream(fileName: String) = fileName.let { if (it == "pack.png") packImage else it }.let { this::class.java.classLoader.getResourceAsStream(it) }
    override fun close() {}

    class Finder(val pack: GeneratorPack) : IPackFinder {
        override fun <T : ResourcePackInfo> addPackInfosToMap(nameToPackMap: MutableMap<String, T>, packInfoFactory: ResourcePackInfo.IFactory<T>) {
            val packInfo = ResourcePackInfo.createResourcePack(
                pack.packName,
                true,
                Supplier { pack } as Supplier<IResourcePack>,
                packInfoFactory,
                ResourcePackInfo.Priority.BOTTOM
            )
            nameToPackMap[pack.packName] = packInfo!!
        }
    }
}

/**
 * Abstract base class for resource generators
 *
 * @param[namespace] Resource namespace of generator
 * @param[generatedType] IResourceType of generated resources
 */
abstract class GeneratorBase<T>(val namespace: String, val generatedType: IResourceType) : ISelectiveResourceReloadListener {
    val keyToId = mutableMapOf<T, String>()
    val idToKey = mutableMapOf<String, T>()
    open val locationMapper: (ResourceLocation)->ResourceLocation = { it }

    init { resourceManager.addReloadListener(this) }

    abstract fun get(key: T): InputStream?
    abstract fun exists(key: T): Boolean

    fun registerResource(key: T): ResourceLocation {
        keyToId[key]?.let { return ResourceLocation(namespace, it) }
        val id = UUID.randomUUID().toString()
        keyToId[key] = id
        idToKey[id] = key
        return ResourceLocation(namespace, id)
    }

    fun resourceExists(location: ResourceLocation?): Boolean {
        val key = location?.let { locationMapper(it) }?.path?.let { idToKey[it] } ?: return false
        return exists(key)
    }

    fun getInputStream(location: ResourceLocation?): InputStream? {
        val key = location?.let { locationMapper(it) }?.path?.let { idToKey[it] } ?: return null
        return get(key)
    }

    open fun onReload(resourceManager: IResourceManager) {
        keyToId.clear()
        idToKey.clear()
    }

    override fun onResourceManagerReload(resourceManager: IResourceManager, resourcePredicate: Predicate<IResourceType>) {
        if (resourcePredicate.test(generatedType)) onReload(resourceManager)
    }


}

/**
 * Collection of named [String]-valued key-value pairs, with an extra unnamed (keyless) value.
 * Meant to be encoded as a pipe-delimited list, and used as a [ResourceLocation] path
 * to parametrized generated resources.
 *
 * @param[params] key-value pairs
 * @param[value] keyless extra value
 */
/*
class ParameterList(val params: Map<String, String>, val value: String?) {
    override fun toString() =
            params.entries
            .sortedBy { it.key }
            .fold("") { result, entry -> result + "|${entry.key}=${entry.value}"} +
            (value?.let { "|$it" } ?: "")

    /** Return the value of the given parameter. */
    operator fun get(key: String) = params[key]

    /** Check if the given parameter exists in this list. */
    operator fun contains(key: String) = key in params

    /** Return a new [ParameterList] with the given key-value pair appended to it. */
    operator fun plus(pair: Pair<String, String>) = ParameterList(params + pair, this.value)

    companion object {
        /**
         * Recreate the parameter list from the encoded string, i.e. the opposite of [toString].
         *
         * Everything before the first pipe character is dropped, so the decoding works even if
         * something is prepended to the list (like _textures/blocks/_)
         */
        fun fromString(input: String): ParameterList {
            val params = hashMapOf<String, String>()
            var value: String? = null
            val slices = input.dropWhile { it != '|'}.split('|')
            slices.forEach {
                if (it.contains('=')) {
                    val keyValue = it.split('=')
                    if (keyValue.size == 2) params.put(keyValue[0], keyValue[1])
                } else value = it
            }
            return ParameterList(params, value)
        }

    }
}

abstract class ParameterBasedGenerator(domain: String) : GeneratorBase(domain) {
    abstract fun resourceExists(params: ParameterList): Boolean
    abstract fun getInputStream(params: ParameterList): InputStream?

    override fun resourceExists(location: ResourceLocation?) =
            resourceExists(ParameterList.fromString(location?.path ?: ""))
    override fun getInputStream(location: ResourceLocation?) =
            getInputStream(ParameterList.fromString(location?.path ?: ""))
}
 */
