package mods.betterfoliage.resource.discovery

import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.getJavaClass
import mods.betterfoliage.util.getLines
import mods.betterfoliage.util.resourceManager
import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.INFO

interface IBlockMatcher {
    fun matchesClass(block: Block): Boolean
    fun matchingClass(block: Block): Class<*>?
}

class SimpleBlockMatcher(vararg val classes: Class<*>) : IBlockMatcher {
    override fun matchesClass(block: Block) = matchingClass(block) != null

    override fun matchingClass(block: Block): Class<*>? {
        val blockClass = block.javaClass
        classes.forEach { if (it.isAssignableFrom(blockClass)) return it }
        return null
    }
}

class ConfigurableBlockMatcher(val location: ResourceLocation) : HasLogger(), IBlockMatcher {
    val blackList = mutableListOf<Class<*>>()
    val whiteList = mutableListOf<Class<*>>()

    override fun matchesClass(block: Block): Boolean {
        val blockClass = block.javaClass
        blackList.forEach { if (it.isAssignableFrom(blockClass)) return false }
        whiteList.forEach { if (it.isAssignableFrom(blockClass)) return true }
        return false
    }

    override fun matchingClass(block: Block): Class<*>? {
        val blockClass = block.javaClass
        blackList.forEach { if (it.isAssignableFrom(blockClass)) return null }
        whiteList.forEach { if (it.isAssignableFrom(blockClass)) return it }
        return null
    }

    fun readDefaults() {
        blackList.clear()
        whiteList.clear()
        resourceManager.getResources(location).forEach { resource ->
            detailLogger.log(INFO, "Reading block class configuration $location from pack ${resource.sourceName}")
            resource.getLines().map{ it.trim() }.filter { !it.startsWith("//") && it.isNotEmpty() }.forEach { line ->
                if (line.startsWith("-")) getJavaClass(line.substring(1))?.let { blackList.add(it) }
                else getJavaClass(line)?.let { whiteList.add(it) }

            }
        }
    }
}

data class ModelTextureList(val modelLocation: ResourceLocation, val textureNames: List<String>) {
    constructor(vararg args: String) : this(ResourceLocation(args[0]), listOf(*args).drop(1))
}

class ModelTextureListConfiguration(val location: ResourceLocation) : HasLogger() {
    val modelList = mutableListOf<ModelTextureList>()
    fun readDefaults() {
        resourceManager.getResources(location).forEach { resource ->
            detailLogger.log(INFO, "Reading model/texture configuration $location from pack ${resource.sourceName}")
            resource.getLines().map{ it.trim() }.filter { !it.startsWith("//") && it.isNotEmpty() }.forEach { line ->
                val elements = line.split(",")
                modelList.add(ModelTextureList(ResourceLocation(elements.first()), elements.drop(1)))
            }
        }
    }
}