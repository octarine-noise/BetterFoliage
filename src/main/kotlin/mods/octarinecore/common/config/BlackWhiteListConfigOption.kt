package mods.octarinecore.common.config

import mods.octarinecore.client.gui.NonVerboseArrayEntry
import mods.octarinecore.client.resource.get
import mods.octarinecore.client.resource.getLines
import mods.octarinecore.client.resource.resourceManager
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property

abstract class BlackWhiteListConfigOption<VALUE>(val domain: String, val path: String) : ConfigPropertyBase() {

    val blackList = mutableListOf<VALUE>()
    val whiteList = mutableListOf<VALUE>()
    var blacklistProperty: Property? = null
    var whitelistProperty: Property? = null

    override val hasChanged: Boolean
        get() = blacklistProperty?.hasChanged() ?: false || whitelistProperty?.hasChanged() ?: false

    override val guiProperties: List<Property> get() = listOf(whitelistProperty!!, blacklistProperty!!)

    override fun attach(target: Configuration, langPrefix: String, categoryName: String, propertyName: String) {
        lang = null
        val defaults = readDefaults(domain, path)
        blacklistProperty = target.get(categoryName, "${propertyName}Blacklist", defaults.first)
        whitelistProperty = target.get(categoryName, "${propertyName}Whitelist", defaults.second)
        listOf(blacklistProperty!!, whitelistProperty!!).forEach {
            it.configEntryClass = NonVerboseArrayEntry::class.java
            it.languageKey = "$langPrefix.$categoryName.${it.name}"
        }
        read()
    }

    abstract fun convertValue(line: String): VALUE?

    override fun read() {
        listOf(Pair(blackList, blacklistProperty!!), Pair(whiteList, whitelistProperty!!)).forEach {
            it.first.clear()
            it.second.stringList.forEach { line ->
                val value = convertValue(line)
                if (value != null) it.first.add(value)
            }
        }
    }

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
}