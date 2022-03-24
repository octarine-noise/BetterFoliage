package mods.betterfoliage.util

import net.minecraft.client.MinecraftClient
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

/** Concise getter for the Minecraft resource manager. */
val resourceManager: ReloadableResourceManagerImpl get() =
    MinecraftClient.getInstance().resourceManager as ReloadableResourceManagerImpl

/** Append a string to the [ResourceLocation]'s path. */
operator fun Identifier.plus(str: String) = Identifier(namespace, path + str)

/** Index operator to get a resource. */
operator fun ResourceManager.get(domain: String, path: String): Resource? = get(Identifier(domain, path))
/** Index operator to get a resource. */
operator fun ResourceManager.get(location: Identifier): Resource? = tryDefault(null) { getResource(location) }

/** Get the lines of a text resource. */
fun Resource.getLines(): List<String> {
    val result = arrayListOf<String>()
    inputStream.bufferedReader().useLines { it.forEach { result.add(it) } }
    return result
}




