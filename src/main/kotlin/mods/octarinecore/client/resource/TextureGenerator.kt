package mods.octarinecore.client.resource

import mods.octarinecore.client.resource.ResourceType.*
import net.minecraft.client.resources.IResource
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.InputStream

/** Type of generated texture resource */
enum class ResourceType {
    COLOR,          // regular diffuse map
    METADATA,       // texture metadata
    NORMAL,         // ShadersMod normal map
    SPECULAR        // ShadersMod specular map
}

/**
 * Generator returning textures based on a single other texture. This texture is located with the
 * _dom_ and _path_ parameters of a [ParameterList].
 *
 * @param[domain] Resource domain of generator
 */
abstract class TextureGenerator(domain: String) : ParameterBasedGenerator(domain) {

    /**
     * Obtain a [ResourceLocation] to a generated texture
     *
     * @param[iconName] the name of the [TextureAtlasSprite] (not the full location) backing the generated texture
     * @param[extraParams] additional parameters of the generated texture
     */
    fun generatedResource(iconName: String, vararg extraParams: Pair<String, Any>) = ResourceLocation(
        domain,
        textureLocation(iconName).let {
            ParameterList(
                mapOf("dom" to it.resourceDomain, "path" to it.resourcePath) +
                    extraParams.map { Pair(it.first, it.second.toString()) },
                "generate"
            ).toString()
        }
    )

    /** Get the type and location of the texture resource encoded by the given [ParameterList]. */
    fun targetResource(params: ParameterList): Pair<ResourceType, ResourceLocation>? {
        val baseTexture =
            if (listOf("dom", "path").all { it in params }) ResourceLocation(params["dom"]!!, params["path"]!!)
            else return null
        return when(params.value?.toLowerCase()) {
            "generate.png" -> COLOR to baseTexture + ".png"
            "generate.png.mcmeta" -> METADATA to baseTexture + ".png.mcmeta"
            "generate_n.png" -> NORMAL to baseTexture + "_n.png"
            "generate_s.png" -> SPECULAR to baseTexture + "_s.png"
            else -> null
        }
    }

    override fun resourceExists(params: ParameterList) =
        targetResource(params)?.second?.let { resourceManager[it] != null } ?: false

    override fun getInputStream(params: ParameterList): InputStream? {
        val target = targetResource(params)
        return when(target?.first) {
            null -> null
            METADATA -> resourceManager[target!!.second]?.inputStream
            else -> generate(params)?.asStream
        }
    }

    /**
     * Generate image data from the parameter list.
     */
    abstract fun generate(params: ParameterList): BufferedImage?

    /**
     * Get a texture resource when multiple sizes may exist.
     *
     * @param[maxSize] Maximum size to consider. This value is progressively halved when searching for smaller versions.
     * @param[maskPath] Location of the texture of the given size
     *
     */
    fun getMultisizeTexture(maxSize: Int, maskPath: (Int)->ResourceLocation): IResource? {
        var size = maxSize
        val sizes = mutableListOf<Int>()
        while(size > 2) { sizes.add(size); size /= 2 }
        return sizes.map { resourceManager[maskPath(it)] }.filterNotNull().firstOrNull()
    }
}