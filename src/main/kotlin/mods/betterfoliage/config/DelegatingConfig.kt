@file:JvmName("DelegatingConfigKt")

package mods.betterfoliage.config

import mods.betterfoliage.util.reflectDelegates
import mods.betterfoliage.util.reflectNestedObjects
import net.minecraftforge.common.ForgeConfigSpec
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class DelegatingConfig(val modId: String, val langPrefix: String) {
    fun build() = ForgeConfigSpec.Builder().apply { ConfigBuildContext(langPrefix, emptyList(), this).addCategory(this@DelegatingConfig) }.build()
}

class ConfigBuildContext(val langPrefix: String, val path: List<String>, val builder: ForgeConfigSpec.Builder) {

    fun addCategory(configObj: Any) {
        configObj.reflectNestedObjects.forEach { (name, category) ->
            builder.push(name)
            descend(name).addCategory(category)
            builder.pop()
        }
        configObj.reflectDelegates(ConfigDelegate::class.java).forEach { (name, delegate) ->
            descend(name).apply { delegate.addToBuilder(this) }
        }
    }

    fun descend(pathName: String) = ConfigBuildContext(langPrefix, path + pathName, builder)
}

open class ConfigCategory(val comment: String? = null) {
}

abstract class ConfigDelegate<T> : ReadOnlyProperty<Any, T> {
    lateinit var configValue: ForgeConfigSpec.ConfigValue<T>
    var cachedValue: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (cachedValue == null) cachedValue = configValue.get()
        return cachedValue!!
    }

    abstract fun getConfigValue(name: String, builder: ForgeConfigSpec.Builder): ForgeConfigSpec.ConfigValue<T>
    fun addToBuilder(ctx: ConfigBuildContext) {
        val langKey = ctx.langPrefix + "." + (langPrefixOverride ?: ctx.path.joinToString("."))
        ctx.builder.translation(langKey)
        configValue = getConfigValue(ctx.path.last(), ctx.builder)
    }

    var langPrefixOverride: String? = null
    fun lang(prefix: String) = apply { langPrefixOverride = prefix }

}

class DelegatingBooleanValue(val defaultValue: Boolean) : ConfigDelegate<Boolean>() {
    override fun getConfigValue(name: String, builder: ForgeConfigSpec.Builder) = builder.define(name, defaultValue)
}

class DelegatingIntValue(
    val minValue: Int = 0,
    val maxValue: Int = 1,
    val defaultValue: Int = 0
) : ConfigDelegate<Int>() {
    override fun getConfigValue(name: String, builder: ForgeConfigSpec.Builder) = builder.defineInRange(name, defaultValue, minValue, maxValue)
}

class DelegatingLongValue(
    val minValue: Long = 0,
    val maxValue: Long = 1,
    val defaultValue: Long = 0
) : ConfigDelegate<Long>() {
    override fun getConfigValue(name: String, builder: ForgeConfigSpec.Builder) = builder.defineInRange(name, defaultValue, minValue, maxValue)
}

class DelegatingDoubleValue(
    val minValue: Double = 0.0,
    val maxValue: Double = 1.0,
    val defaultValue: Double = 0.0
) : ConfigDelegate<Double>() {
    override fun getConfigValue(name: String, builder: ForgeConfigSpec.Builder) = builder.defineInRange(name, defaultValue, minValue, maxValue)
}

// ============================
// Delegate factory methods
// ============================
fun double(min: Double = 0.0, max: Double = 1.0, default: Double) = DelegatingDoubleValue(min, max, default)
fun int(min: Int = 0, max: Int, default: Int) = DelegatingIntValue(min, max, default)
fun long(min: Long = 0, max: Long, default: Long) = DelegatingLongValue(min, max, default)
fun boolean(default: Boolean) = DelegatingBooleanValue(default)
