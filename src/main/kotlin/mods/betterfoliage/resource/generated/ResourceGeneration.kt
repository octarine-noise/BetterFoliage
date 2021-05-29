package mods.betterfoliage.resource.generated

import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.HasLogger
import net.minecraft.client.Minecraft
import net.minecraft.resources.*
import net.minecraft.resources.ResourcePackType.CLIENT_RESOURCES
import net.minecraft.resources.data.IMetadataSectionSerializer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.StringTextComponent
import org.apache.logging.log4j.Level.INFO
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * [IResourcePack] containing generated resources
 *
 * @param[name] Name of the resource pack
 * @param[generators] List of resource generators
 */
class GeneratedTexturePack(
    val nameSpace: String, val packName: String
) : HasLogger(), IResourcePack {
    override fun getName() = packName
    override fun getNamespaces(type: ResourcePackType) = setOf(nameSpace)
    override fun <T : Any?> getMetadataSection(deserializer: IMetadataSectionSerializer<T>) = null
    override fun getRootResource(id: String) = null
    override fun getResources(type: ResourcePackType, namespace:String, path: String, maxDepth: Int, filter: Predicate<String>) = emptyList<ResourceLocation>()

    override fun close() {}

    protected var manager: IResourceManager = Minecraft.getInstance().resourceManager
    val identifiers = Collections.synchronizedMap(mutableMapOf<Any, ResourceLocation>())
    val resources = Collections.synchronizedMap(mutableMapOf<ResourceLocation, ByteArray>())

    fun register(atlas: Atlas, key: Any, func: (IResourceManager)->ByteArray): ResourceLocation {
        identifiers[key]?.let { return it }

        val id = ResourceLocation(nameSpace, UUID.randomUUID().toString())
        val fileName = atlas.file(id)
        val resource = func(manager)

        identifiers[key] = id
        resources[fileName] = resource
        detailLogger.log(INFO, "generated resource $key -> $fileName")
        return id
    }

    override fun getResource(type: ResourcePackType, id: ResourceLocation) =
        if (type != CLIENT_RESOURCES) null else resources[id]?.inputStream()

    override fun hasResource(type: ResourcePackType, id: ResourceLocation) =
        type == CLIENT_RESOURCES && resources.containsKey(id)

    val finder = object : IPackFinder {
        val packInfo = ResourcePackInfo(
            packName, true, Supplier { this@GeneratedTexturePack },
            StringTextComponent(packName),
            StringTextComponent("Generated block textures resource pack"),
            PackCompatibility.COMPATIBLE, ResourcePackInfo.Priority.TOP, true, IPackNameDecorator.DEFAULT, true
        )

        override fun loadPacks(p0: Consumer<ResourcePackInfo>, p1: ResourcePackInfo.IFactory) {
            p0.accept(packInfo)
        }
    }
}
