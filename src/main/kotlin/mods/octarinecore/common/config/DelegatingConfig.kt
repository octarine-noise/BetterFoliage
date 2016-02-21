package mods.octarinecore.common.config

import com.google.common.collect.LinkedListMultimap
import mods.octarinecore.metaprog.reflectField
import mods.octarinecore.metaprog.reflectFieldsOfType
import mods.octarinecore.metaprog.reflectNestedObjects
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import net.minecraftforge.fml.client.config.GuiConfigEntries
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.reflect.KProperty

// ============================
// Configuration object base
// ============================
/**
 * Base class for declarative configuration handling.
 *
 * Subclasses should be singleton objects, containing one layer of further singleton objects representing
 * config categories (nesting is not supported).
 *
 * Both the root object (maps to the category _global_) and category objects can contain [ConfigPropertyBase]
 * instances (either directly or as a delegate), which handle the Forge [Configuration] itself.
 *
 * Config properties map to language keys by their field names.
 *
 * @param[modId] mod ID this configuration is linked to
 * @param[langPrefix] prefix to use for language keys
 */
abstract class DelegatingConfig(val modId: String, val langPrefix: String) {

    init { MinecraftForge.EVENT_BUS.register(this) }

    /** The [Configuration] backing this config object. */
    var config: Configuration? = null
    val rootGuiElements = mutableListOf<IConfigElement>()

    /** Attach this config object to the given [Configuration] and update all properties. */
    fun attach(config: Configuration) {
        this.config = config
        val subProperties = LinkedListMultimap.create<String, String>()
        rootGuiElements.clear()

        forEachProperty { category, name, property ->
            property.lang = property.lang ?: "$category.$name"
            property.attach(config, langPrefix, category, name)
            property.guiProperties.forEach { guiProperty ->
                property.guiClass?.let { guiProperty.setConfigEntryClass(it) }
                if (category == "global") rootGuiElements.add(ConfigElement(guiProperty))
                else subProperties.put(category, guiProperty.name)
            }
        }
        for (category in subProperties.keySet()) {
            val configCategory = config.getCategory(category)
            configCategory.setLanguageKey("$langPrefix.$category")
            configCategory.setPropertyOrder(subProperties[category])
            rootGuiElements.add(ConfigElement(configCategory))
        }
        save()
    }

    /**
     * Execute the given lambda for all config properties.
     * Lambda params: (category name, property name, property instance)
     */
    inline fun forEachProperty(init: (String, String, ConfigPropertyBase)->Unit) {
        reflectFieldsOfType(ConfigPropertyBase::class.java).forEach { property ->
            init("global", property.first.split("$")[0], property.second as ConfigPropertyBase)
        }
        for (category in reflectNestedObjects) {
            category.second.reflectFieldsOfType(ConfigPropertyBase::class.java).forEach { property ->
                init(category.first, property.first.split("$")[0], property.second as ConfigPropertyBase)
            }
        }
    }

    /** Save changes to the [Configuration]. */
    fun save() { if (config?.hasChanged() ?: false) config!!.save() }

    /**
     * Returns true if any of the given configuration elements have changed.
     * Supports both categories and
     */
    fun hasChanged(vararg elements: Any?): Boolean {
        reflectNestedObjects.forEach { category ->
            if (category.second in elements && config?.getCategory(category.first)?.hasChanged() ?: false) return true
        }
        forEachProperty { category, name, property ->
            if (property in elements && property.hasChanged) return true
        }
        return false
    }

    /** Called when the configuration for the mod changes. */
    open fun onChange(event: ConfigChangedEvent.OnConfigChangedEvent) {
        save()
        forEachProperty { c, n, prop -> prop.read() }
    }

    @SubscribeEvent
    fun handleConfigChange(event: ConfigChangedEvent.OnConfigChangedEvent) { if (event.modID == modId) onChange(event) }

    /** Extension to get the underlying delegate of a field */
    operator fun Any.get(name: String) = this.reflectField<ConfigPropertyBase>("$name\$delegate")
}

// ============================
// Property delegates
// ============================

/** Base class for config property delegates. */
abstract class ConfigPropertyBase {
    /** Language key of the property. */
    var lang: String? = null

    /** GUI class to use. */
    var guiClass: Class<out GuiConfigEntries.IConfigEntry>? = null

    /** @return true if the property has changed. */
    abstract val hasChanged: Boolean

    /** Attach this delegate to a Forge [Configuration]. */
    abstract fun attach(target: Configuration, langPrefix: String, categoryName: String, propertyName: String)

    /** List of [Property] instances backing this delegate. */
    abstract val guiProperties: List<Property>

    /** Re-read the property value from the [Configuration]. */
    open fun read() {}
}

/** Delegate for a property backed by a single [Property] instance. */
abstract class ConfigPropertyDelegate<T>() : ConfigPropertyBase() {
    /** Cached value of the property. */
    var cached: T? = null
    /** The [Property] backing this delegate. */
    var property: Property? = null

    override val guiProperties: List<Property> get() = listOf(property!!)
    override val hasChanged: Boolean  get() =  property?.hasChanged() ?: false

    /** Chained setter for the language key. */
    fun lang(lang: String) = apply { this.lang = lang }

    /** Read the backing [Property] instance. */
    abstract fun Property.read(): T

    /** Write the backing [Property] instance. */
    abstract fun Property.write(value: T)

    /** Get the backing [Property] instance. */
    abstract fun resolve(target: Configuration, category: String, name: String): Property

    /** Kotlin deleagation implementation. */
    operator fun getValue(thisRef: Any, delegator: KProperty<*>): T {
        if (cached != null) return cached!!
        cached = property!!.read()
        return cached!!
    }

    /** Kotlin deleagation implementation. */
    operator fun setValue(thisRef: Any, delegator: KProperty<*>, value: T) {
        cached = value
        property!!.write(value)
    }

    override fun read() { cached = null }

    override fun attach(target: Configuration, langPrefix: String, categoryName: String, propertyName: String) {
        cached = null
        property = resolve(target, categoryName, propertyName)
        property!!.setLanguageKey("$langPrefix.$lang")
    }
}

/** [Double]-typed property delegate. */
class ConfigPropertyDouble(val min: Double, val max: Double, val default: Double) :
    ConfigPropertyDelegate<Double>() {
    override fun resolve(target: Configuration, category: String, name: String) =
            target.get(category, name, default, null).apply { setMinValue(min); setMaxValue(max) }
    override fun Property.read() = property!!.double
    override fun Property.write(value: Double) = property!!.set(value)
}

/** [Float]-typed property delegate. */
class ConfigPropertyFloat(val min: Double, val max: Double, val default: Double) :
    ConfigPropertyDelegate<Float>() {
    override fun resolve(target: Configuration, category: String, name: String) =
        target.get(category, name, default, null).apply { setMinValue(min); setMaxValue(max) }
    override fun Property.read() = property!!.double.toFloat()
    override fun Property.write(value: Float) = property!!.set(value.toDouble())
}

/** [Int]-typed property delegate. */
class ConfigPropertyInt(val min: Int, val max: Int, val default: Int) :
        ConfigPropertyDelegate<Int>() {
    override fun resolve(target: Configuration, category: String, name: String) =
            target.get(category, name, default, null).apply { setMinValue(min); setMaxValue(max) }
    override fun Property.read() = property!!.int
    override fun Property.write(value: Int) = property!!.set(value)
}

/** [Boolean]-typed property delegate. */
class ConfigPropertyBoolean(val default: Boolean) :
    ConfigPropertyDelegate<Boolean>() {
    override fun resolve(target: Configuration, category: String, name: String) =
            target.get(category, name, default, null)
    override fun Property.read() = property!!.boolean
    override fun Property.write(value: Boolean) = property!!.set(value)
}

/** [Int] array typed property delegate. */
class ConfigPropertyIntList(val defaults: ()->Array<Int>) :
        ConfigPropertyDelegate<Array<Int>>() {
    override fun resolve(target: Configuration, category: String, name: String) =
            target.get(category, name, defaults().toIntArray(), null)
    override fun Property.read() = property!!.intList.toTypedArray()
    override fun Property.write(value: Array<Int>) = property!!.set(value.toIntArray())
}

// ============================
// Delegate factory methods
// ============================
fun double(min: Double = 0.0, max: Double = 1.0, default: Double) = ConfigPropertyDouble(min, max, default)
fun float(min: Double = 0.0, max: Double = 1.0, default: Double) = ConfigPropertyFloat(min, max, default)
fun int(min: Int = 0, max: Int, default: Int) = ConfigPropertyInt(min, max, default)
fun intList(defaults: ()->Array<Int>) = ConfigPropertyIntList(defaults)
fun boolean(default: Boolean) = ConfigPropertyBoolean(default)