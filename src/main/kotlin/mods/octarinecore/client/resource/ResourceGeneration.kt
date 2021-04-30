package mods.octarinecore.client.resource

import mods.octarinecore.HasLogger
import mods.octarinecore.common.completedVoid
import mods.octarinecore.common.map
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.resources.ClientResourcePackInfo
import net.minecraft.resources.*
import net.minecraft.resources.ResourcePackType.CLIENT_RESOURCES
import net.minecraft.resources.data.IMetadataSectionSerializer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.StringTextComponent
import org.apache.logging.log4j.Logger
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * [IResourcePack] containing generated resources
 *
 * @param[name] Name of the resource pack
 * @param[generators] List of resource generators
 */
class GeneratedBlockTexturePack(val nameSpace: String, val packName: String, override val logger: Logger) : HasLogger, IResourcePack, AsyncSpriteProvider<ModelBakery> {

    override fun getName() = packName
    override fun getResourceNamespaces(type: ResourcePackType) = setOf(nameSpace)
    override fun <T : Any?> getMetadata(deserializer: IMetadataSectionSerializer<T>) = null
    override fun getRootResourceStream(id: String) = null
    override fun getAllResourceLocations(type: ResourcePackType, path: String, maxDepth: Int, filter: Predicate<String>) = emptyList<ResourceLocation>()
    override fun close() {}

    protected var manager: CompletableFuture<IResourceManager>? = null
    val identifiers = Collections.synchronizedMap(mutableMapOf<Any, ResourceLocation>())
    val resources = Collections.synchronizedMap(mutableMapOf<ResourceLocation, CompletableFuture<ByteArray>>())

    fun register(key: Any, func: (IResourceManager)->ByteArray): ResourceLocation {
        if (manager == null) throw IllegalStateException("Cannot register resources unless block textures are being reloaded")
        identifiers[key]?.let { return it }

        val id = ResourceLocation(nameSpace, UUID.randomUUID().toString())
        val resource = manager!!.map { func(it) }

        identifiers[key] = id
        resources[Atlas.BLOCKS.wrap(id)] = resource
        log("generated resource $key -> $id")
        return id
    }

    override fun getResourceStream(type: ResourcePackType, id: ResourceLocation) =
        if (type != CLIENT_RESOURCES) null else
            try { resources[id]!!.get().inputStream() }
            catch (e: ExecutionException) { (e.cause as? IOException)?.let { throw it } }   // rethrow wrapped IOException if present

    override fun resourceExists(type: ResourcePackType, id: ResourceLocation) =
        type == CLIENT_RESOURCES && resources.containsKey(id)

    override fun setup(manager: IResourceManager, bakeryF: CompletableFuture<ModelBakery>, atlas: AtlasFuture): StitchPhases {
        this.manager = CompletableFuture.completedFuture(manager)
        return StitchPhases(
            completedVoid(),
            atlas.runAfter {
                this.manager = null
                identifiers.clear()
                resources.clear()
            }
        )
    }

    val finder = object : IPackFinder {
        val packInfo = ClientResourcePackInfo(
            packName, true, Supplier { this@GeneratedBlockTexturePack },
            StringTextComponent(packName),
            StringTextComponent("Generated block textures resource pack"),
            PackCompatibility.COMPATIBLE, ResourcePackInfo.Priority.TOP, true, null, true
        )
        override fun <T : ResourcePackInfo> addPackInfosToMap(nameToPackMap: MutableMap<String, T>, packInfoFactory: ResourcePackInfo.IFactory<T>) {
            (nameToPackMap as MutableMap<String, ResourcePackInfo>).put(packName, packInfo)
        }
    }
}