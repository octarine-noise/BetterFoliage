package mods.octarinecore.client.resource

import mods.octarinecore.metaprog.reflectField
import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.data.IMetadataSection
import net.minecraft.client.resources.data.IMetadataSerializer
import net.minecraft.client.resources.data.PackMetadataSection
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.fml.client.FMLClientHandler
import java.io.InputStream
import java.util.*

/**
 * [IResourcePack] containing generated resources. Adds itself to the default resource pack list
 * of Minecraft, so it is invisible and always active.
 *
 * @param[name] Name of the resource pack
 * @param[generators] List of resource generators
 */
class GeneratorPack(val name: String, vararg val generators: GeneratorBase) : IResourcePack {

    init {
        FMLClientHandler.instance().reflectField<MutableList<IResourcePack>>("resourcePackList")!!.add(this)
    }

    override fun getPackName() = name
    override fun getPackImage() = null
    override fun getResourceDomains() = HashSet(generators.map { it.domain })
    override fun <T: IMetadataSection> getPackMetadata(serializer: IMetadataSerializer?, type: String?) =
        if (type == "pack") PackMetadataSection(TextComponentString("Generated resources"), 1) as? T else null

    override fun resourceExists(location: ResourceLocation?): Boolean =
            if (location == null) false
            else generators.find {
                it.domain == location.resourceDomain && it.resourceExists(location)
            } != null

    override fun getInputStream(location: ResourceLocation?): InputStream? =
            if (location == null) null
            else generators.filter {
                it.domain == location.resourceDomain && it.resourceExists(location)
            }.map { it.getInputStream(location) }
            .filterNotNull().first()

//    override fun <T : IMetadataSection?> getPackMetadata(p_135058_1_: IMetadataSerializer?, p_135058_2_: String?): T {
//        return if (type == "pack") PackMetadataSection(ChatComponentText("Generated resources"), 1) else null
//    }
}

/**
 * Abstract base class for resource generators
 *
 * @param[domain] Resource domain of generator
 */
abstract class GeneratorBase(val domain: String) {
    /** @see [IResourcePack.resourceExists] */
    abstract fun resourceExists(location: ResourceLocation?): Boolean

    /** @see [IResourcePack.getInputStream] */
    abstract fun getInputStream(location: ResourceLocation?): InputStream?
}

/**
 * Collection of named [String]-valued key-value pairs, with an extra unnamed (keyless) value.
 * Meant to be encoded as a pipe-delimited list, and used as a [ResourceLocation] path
 * to parametrized generated resources.
 *
 * @param[params] key-value pairs
 * @param[value] keyless extra value
 */
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
            resourceExists(ParameterList.fromString(location?.resourcePath ?: ""))
    override fun getInputStream(location: ResourceLocation?) =
            getInputStream(ParameterList.fromString(location?.resourcePath ?: ""))
}