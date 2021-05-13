package mods.betterfoliage.resource.discovery

import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.getLines
import mods.betterfoliage.util.INTERMEDIARY
import mods.betterfoliage.util.getJavaClass
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Logger

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

class ConfigurableBlockMatcher(val location: Identifier) : HasLogger(), IBlockMatcher {

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

    fun readDefaults(manager: ResourceManager) {
        blackList.clear()
        whiteList.clear()
        manager.getAllResources(location).forEach { resource ->
            detailLogger.log(INFO, "Reading class list $location from pack ${resource.resourcePackName}")
            resource.getLines().map{ it.trim() }.filter { !it.startsWith("//") && it.isNotEmpty() }.forEach { line ->
                val name = if (line.startsWith("-")) line.substring(1) else line
                val mappedName = FabricLoader.getInstance().mappingResolver.mapClassName(INTERMEDIARY, name)
                if (name != mappedName) logger.debug("    found yarn mapping for class: $name -> $mappedName")
                val klass = getJavaClass(mappedName)

                val list = if (line.startsWith("-")) "blacklist" to blackList else "whitelist" to whiteList

                if (klass != null) {
                    logger.debug("    ${list.first} class $name found")
                    list.second.add(klass)
                } else {
                    logger.debug("    ${list.first} class $name not found")
                }
            }
        }
    }
}

data class ModelTextureList(val modelLocation: Identifier, val textureNames: List<String>) {
    constructor(vararg args: String) : this(Identifier(args[0]), listOf(*args).drop(1))
}

class ModelTextureListConfiguration(val location: Identifier) : HasLogger() {
    val modelList = mutableListOf<ModelTextureList>()
    fun readDefaults(manager: ResourceManager) {
        manager.getAllResources(location).forEach { resource ->
            detailLogger.log(INFO, "Reading model configuration $location from pack ${resource.resourcePackName}")
            resource.getLines().map{ it.trim() }.filter { !it.startsWith("//") && it.isNotEmpty() }.forEach { line ->
                val elements = line.split(",")
                modelList.add(ModelTextureList(Identifier(elements.first()), elements.drop(1)))
            }
        }
    }
}