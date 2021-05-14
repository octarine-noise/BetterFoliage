package mods.betterfoliage.config

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry
import me.zeroeightsix.fiber.builder.ConfigValueBuilder
import me.zeroeightsix.fiber.tree.ConfigLeaf
import me.zeroeightsix.fiber.tree.ConfigNode
import me.zeroeightsix.fiber.tree.ConfigValue
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.LiteralText
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

const val MAX_LINE_LEN = 30

fun textify(string: String) = LiteralText(string)
fun textify(strings: Array<String>) = strings.map(::LiteralText).toTypedArray()

sealed class DelegatingConfigNode<N: ConfigLeaf>(val fiberNode: N) {
    abstract fun createClothNode(names: List<String>): AbstractConfigListEntry<*>
}

abstract class DelegatingConfigValue<T>(fiberNode: ConfigValue<T>) : DelegatingConfigNode<ConfigValue<T>>(fiberNode), ReadOnlyProperty<DelegatingConfigGroup, T>

open class DelegatingConfigGroup(fiberNode: ConfigNode) : DelegatingConfigNode<ConfigNode>(fiberNode) {
    val children = mutableListOf<DelegatingConfigNode<*>>()
    override fun createClothNode(names: List<String>): SubCategoryListEntry {
        val builder = ConfigEntryBuilder.create()
            .startSubCategory(textify(names.joinToString(".").translate()))
            .setTooltip(*textify(names.joinToString(".").translateTooltip()))
            .setExpanded(false)
        children.forEach { builder.add(it.createClothNode(names + it.fiberNode.name!!)) }
        return builder.build()
    }
    operator fun get(name: String) = children.find { it.fiberNode.name == name }
}

interface DelegatingConfigGroupFactory<T> {
    operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T>
}

fun <T: DelegatingConfigGroup> subNode(factory: (ConfigNode)->T) = object : DelegatingConfigGroupFactory<T> {
    override operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T> {
        val childNode = ConfigNode(property.name, null)
        val configGroup = factory(childNode)
        parent.fiberNode.items.add(childNode)
        parent.children.add(configGroup)
        return object : ReadOnlyProperty<DelegatingConfigGroup, T> {
            override fun getValue(thisRef: DelegatingConfigGroup, property: KProperty<*>) = configGroup
        }
    }
}

interface DelegatingConfigValueFactory<T> {
    fun createFiberNode(parent: ConfigNode, name: String): ConfigValue<T>
    fun createClothNode(node: ConfigValue<T>, names: List<String>): AbstractConfigListEntry<T>

    operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T> {
        return object : DelegatingConfigValue<T>(createFiberNode(parent.fiberNode, property.name)) {
            override fun createClothNode(names: List<String>) = createClothNode(fiberNode, names)
            override fun getValue(thisRef: DelegatingConfigGroup, property: KProperty<*>) = fiberNode.value!!
        }.apply { parent.children.add(this) }
    }
}

fun String.translate() = I18n.translate(this)
fun String.translateTooltip(lineLength: Int = MAX_LINE_LEN) = ("$this.tooltip").translate().let { tooltip ->
    tooltip.splitToSequence(" ").fold(mutableListOf("")) { tooltips, word ->
        if (tooltips.last().length + word.length < lineLength) {
            tooltips[tooltips.lastIndex] += "$word "
        } else {
            tooltips.add("$word ")
        }
        tooltips
    }.map { it.trim() }.toTypedArray()
}

fun boolean(
    default: Boolean,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Boolean)->Boolean = { it }
) = object : DelegatingConfigValueFactory<Boolean> {
    override fun createFiberNode(parent: ConfigNode, name: String) = ConfigValueBuilder(Boolean::class.java)
        .withName(name)
        .withParent(parent)
        .withDefaultValue(default)
        .build()

    override fun createClothNode(node: ConfigValue<Boolean>, names: List<String>) = ConfigEntryBuilder.create()
        .startBooleanToggle(textify(langKey(names).translate()), node.value!!)
        .setTooltip(langKey(names).let { if (I18n.hasTranslation("$it.tooltip")) Optional.of(textify(it.translateTooltip())) else Optional.empty() })
        .setSaveConsumer { node.value = valueOverride(it) }
        .build()
}

fun integer(
    default: Int, min: Int, max: Int,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Int)->Int = { it }
) = object : DelegatingConfigValueFactory<Int> {
    override fun createFiberNode(parent: ConfigNode, name: String) = ConfigValueBuilder(Int::class.java)
        .withName(name)
        .withParent(parent)
        .withDefaultValue(default)
        .constraints().minNumerical(min).maxNumerical(max).finish()
        .build()

    override fun createClothNode(node: ConfigValue<Int>, names: List<String>) = ConfigEntryBuilder.create()
        .startIntField(textify(langKey(names).translate()), node.value!!)
        .setTooltip(langKey(names).let { if (I18n.hasTranslation("$it.tooltip")) Optional.of(textify(it.translateTooltip())) else Optional.empty() })
        .setMin(min).setMax(max)
        .setSaveConsumer { node.value = valueOverride(it) }
        .build()
}

fun double(
    default: Double, min: Double, max: Double,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Double)->Double = { it }
) = object : DelegatingConfigValueFactory<Double> {
    override fun createFiberNode(parent: ConfigNode, name: String) = ConfigValueBuilder(Double::class.java)
        .withName(name)
        .withParent(parent)
        .withDefaultValue(default)
        .constraints().minNumerical(min).maxNumerical(max).finish()
        .build()

    override fun createClothNode(node: ConfigValue<Double>, names: List<String>) = ConfigEntryBuilder.create()
        .startDoubleField(textify(langKey(names).translate()), node.value!!)
        .setTooltip(langKey(names).let { if (I18n.hasTranslation("$it.tooltip")) Optional.of(textify(it.translateTooltip())) else Optional.empty() })
        .setMin(min).setMax(max)
        .setSaveConsumer { node.value = valueOverride(it) }
        .build()
}

val recurring = { names: List<String> -> "${names.first()}.${names.last()}" }
fun fakeCategory(name: String) = { names: List<String> ->
    (listOf(names.first(), name) + names.drop(1)).joinToString(".")
}