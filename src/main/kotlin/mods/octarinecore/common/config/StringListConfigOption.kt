package mods.octarinecore.common.config

import mods.octarinecore.client.gui.NonVerboseArrayEntry
import mods.octarinecore.client.resource.get
import mods.octarinecore.client.resource.getLines
import mods.octarinecore.client.resource.resourceManager
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property

abstract class StringListConfigOption<VALUE>(val domain: String, val path: String) : ConfigPropertyBase() {

    val list = mutableListOf<VALUE>()
    lateinit var listProperty: Property

    override val hasChanged: Boolean get() = listProperty.hasChanged() ?: false
    override val guiProperties: List<Property> get() = listOf(listProperty)

    override fun attach(target: Configuration, langPrefix: String, categoryName: String, propertyName: String) {
        lang = null
        val defaults = readDefaults(domain, path)
        listProperty = target.get(categoryName, "${propertyName}", defaults)
        listProperty.configEntryClass = NonVerboseArrayEntry::class.java
        listProperty.languageKey = "$langPrefix.$categoryName.${listProperty.name}"
        read()
    }

    abstract fun convertValue(line: String): VALUE?

    override fun read() {
        list.clear()
        listProperty.stringList.forEach { line ->
            val value = convertValue(line)
            if (value != null) list.add(value)
        }
    }

    fun readDefaults(domain: String, path: String): Array<String> {
        val list = arrayListOf<String>()
        val defaults = resourceManager[domain, path]?.getLines()
        defaults?.map { it.trim() }?.filter { !it.startsWith("//") && it.isNotEmpty() }?.forEach { list.add(it) }
        return list.toTypedArray()
    }
}