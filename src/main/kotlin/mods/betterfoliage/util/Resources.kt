package mods.betterfoliage.util

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.Material
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.resources.IReloadableResourceManager
import net.minecraft.resources.IResource
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation

/** Concise getter for the Minecraft resource manager. */
val resourceManager: IReloadableResourceManager
    get() = Minecraft.getInstance().resourceManager as IReloadableResourceManager

/** Append a string to the [ResourceLocation]'s path. */
operator fun ResourceLocation.plus(str: String) = ResourceLocation(namespace, path + str)

/** Prepend a string to the [ResourceLocation]'s path. */
fun ResourceLocation.prependLocation(basePath: String) =
    ResourceLocation(namespace, basePath.stripEnd("/").let { "$it/$path" })

val ResourceLocation.asBlockMaterial: Material get() = Material(
    AtlasTexture.LOCATION_BLOCKS_TEXTURE,
    this
)

/** Index operator to get a resource. */
operator fun IResourceManager.get(domain: String, path: String): IResource? = get(ResourceLocation(domain, path))
/** Index operator to get a resource. */
operator fun IResourceManager.get(location: ResourceLocation): IResource? = tryDefault(null) { getResource(location) }

/** Get the lines of a text resource. */
fun IResource.getLines(): List<String> {
    val result = arrayListOf<String>()
    inputStream.bufferedReader().useLines { it.forEach { result.add(it) } }
    return result
}
