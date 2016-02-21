package mods.betterfoliage.client.config

import mods.octarinecore.client.gui.NonVerboseArrayEntry
import mods.octarinecore.client.resource.get
import mods.octarinecore.client.resource.getLines
import mods.octarinecore.client.resource.resourceManager
import mods.octarinecore.common.config.ConfigPropertyBase
import mods.octarinecore.metaprog.getJavaClass
import net.minecraft.block.Block
import net.minecraft.client.multiplayer.WorldClient
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Match blocks based on their class names. Caches block IDs for faster lookup.
 *
 * @param[domain] resource domain for defaults file
 * @param[path] resource path for defaults file
 */
class BlockMatcher(val domain: String, val path: String) : ConfigPropertyBase() {

    val blackList = mutableListOf<Class<*>>()
    val whiteList = mutableListOf<Class<*>>()
    val blockIDs = hashSetOf<Int>()
    var blacklistProperty: Property? = null
    var whitelistProperty: Property? = null

    fun matchesClass(block: Block): Boolean {
        val blockClass = block.javaClass
        blackList.forEach { if (it.isAssignableFrom(blockClass)) return false }
        whiteList.forEach { if (it.isAssignableFrom(blockClass)) return true }
        return false
    }
    fun matchesID(block: Block) = blockIDs.contains(Block.blockRegistry.getIDForObject(block))
    fun matchesID(blockId: Int) = blockIDs.contains(blockId)

    override fun attach(target: Configuration, langPrefix: String, categoryName: String, propertyName: String) {
        lang = null
        val defaults = readDefaults(domain, path)
        blacklistProperty = target.get(categoryName, "${propertyName}Blacklist", defaults.first)
        whitelistProperty = target.get(categoryName, "${propertyName}Whitelist", defaults.second)
        listOf(blacklistProperty!!, whitelistProperty!!).forEach {
            it.setConfigEntryClass(NonVerboseArrayEntry::class.java)
            it.setLanguageKey("$langPrefix.$categoryName.${it.name}")
        }
        read()
    }

    override fun read() {
        listOf(Pair(blackList, blacklistProperty!!), Pair(whiteList, whitelistProperty!!)).forEach {
            it.first.clear()
            it.first.addAll(it.second.stringList.map { getJavaClass(it) }.filterNotNull())
        }
        updateIDs()
    }

    fun updateIDs() {
        blockIDs.clear()
        Block.blockRegistry.forEach {
            if (matchesClass(it as Block)) blockIDs.add(Block.blockRegistry.getIDForObject(it))
        }
    }

    override val hasChanged: Boolean
        get() = blacklistProperty?.hasChanged() ?: false || whitelistProperty?.hasChanged() ?: false

    override val guiProperties: List<Property> get() = listOf(whitelistProperty!!, blacklistProperty!!)

    fun readDefaults(domain: String, path: String): Pair<Array<String>, Array<String>> {
        val blackList = arrayListOf<String>()
        val whiteList = arrayListOf<String>()
        val defaults = resourceManager[domain, path]?.getLines()
        defaults?.map{ it.trim() }?.filter { !it.startsWith("//") && it.isNotEmpty() }?.forEach {
            if (it.startsWith("-")) { blackList.add(it.substring(1)) }
            else { whiteList.add(it) }
        }
        return (blackList.toTypedArray() to whiteList.toTypedArray())
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) { if (event.world is WorldClient) updateIDs() }

    init { MinecraftForge.EVENT_BUS.register(this) }

}