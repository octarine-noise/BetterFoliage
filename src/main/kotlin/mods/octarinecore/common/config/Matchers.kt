package mods.octarinecore.common.config

import mods.octarinecore.metaprog.getJavaClass
import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation

class BlockMatcher(domain: String, path: String) : BlackWhiteListConfigOption<Class<*>>(domain, path) {
    override fun convertValue(line: String) = getJavaClass(line)

    fun matchesClass(block: Block): Boolean {
        val blockClass = block.javaClass
        blackList.forEach { if (it.isAssignableFrom(blockClass)) return false }
        whiteList.forEach { if (it.isAssignableFrom(blockClass)) return true }
        return false
    }

    fun matchingClass(block: Block): Class<*>? {
        val blockClass = block.javaClass
        blackList.forEach { if (it.isAssignableFrom(blockClass)) return null }
        whiteList.forEach { if (it.isAssignableFrom(blockClass)) return it }
        return null
    }
}

data class ModelTextureList(val modelLocation: ResourceLocation, val textureNames: List<String>)

class ModelTextureListConfigOption(domain: String, path: String, val minTextures: Int) : StringListConfigOption<ModelTextureList>(domain, path) {
    override fun convertValue(line: String): ModelTextureList? {
        val elements = line.split(",")
        if (elements.size < minTextures + 1) return null
        return ModelTextureList(ResourceLocation(elements.first()), elements.drop(1))
    }
}