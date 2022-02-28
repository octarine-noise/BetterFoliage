package mods.betterfoliage.config

import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.*
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.LiteralText
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

const val MAX_LINE_LEN = 30

fun textify(string: String) = LiteralText(string)
fun textify(strings: Array<String>) = strings.map(::LiteralText).toTypedArray()

sealed class DelegatingConfigNode<N: ConfigNode> {
    abstract val name: String
    abstract fun createClothNode(names: List<String>): AbstractConfigListEntry<*>
}

abstract class DelegatingConfigValue<T>(override val name: String, val fiberNode: Property<T>) : DelegatingConfigNode<ConfigLeaf<T>>(), ReadOnlyProperty<DelegatingConfigGroup, T>

open class DelegatingConfigGroup(internal val builder: ConfigTreeBuilder) : DelegatingConfigNode<ConfigBranch>() {
    val fiberNode: ConfigBranch by lazy { builder.build() }
    override val name: String
        get() = fiberNode.name!!
    val children = mutableListOf<DelegatingConfigNode<*>>()
    override fun createClothNode(names: List<String>): SubCategoryListEntry {
        val builder = ConfigEntryBuilder.create()
            .startSubCategory(textify(names.joinToString(".").translate()))
            .setTooltip(*textify(names.joinToString(".").translateTooltip()))
            .setExpanded(false)
        children.forEach { builder.add(it.createClothNode(names + it.name)) }
        return builder.build()
    }
    operator fun get(name: String) = fiberNode.lookup(name)
}

interface DelegatingConfigGroupFactory<T> {
    operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T>
}

fun <T: DelegatingConfigGroup> subNode(factory: (ConfigTreeBuilder)->T) = object : DelegatingConfigGroupFactory<T> {
    override operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T> {
        val configGroup = factory(parent.builder.fork(property.name))
        parent.children.add(configGroup)
        return ReadOnlyProperty { _, _ -> configGroup }
    }
}

interface DelegatingConfigValueFactory<T> {
    fun createFiberNode(builder: ConfigTreeBuilder, name: String): Property<T>
    fun createClothNode(node: Property<T>, names: List<String>): AbstractConfigListEntry<T>

    operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T> {
        val fiberNode = createFiberNode(parent.builder, property.name)
        return object : DelegatingConfigValue<T>(property.name, fiberNode) {
            override fun createClothNode(names: List<String>) = createClothNode(fiberNode, names)
            override fun getValue(thisRef: DelegatingConfigGroup, property: KProperty<*>) = fiberNode.value!!
        }.apply { parent.children.add(this) }
    }
}

fun String.translate(): String = I18n.translate(this)
fun String.translateTooltip(lineLength: Int = MAX_LINE_LEN) =
    ("$this.tooltip").translate().splitToSequence(" ").fold(mutableListOf("")) { tooltips, word ->
        if (tooltips.last().length + word.length < lineLength) {
            tooltips[tooltips.lastIndex] += "$word "
        } else {
            tooltips.add("$word ")
        }
        tooltips
    }.map { it.trim() }.toTypedArray()

fun boolean(
    default: Boolean,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Boolean)->Boolean = { it }
) = object : DelegatingConfigValueFactory<Boolean> {
    override fun createFiberNode(builder: ConfigTreeBuilder, name: String) = builder.beginValue(name, ConfigTypes.BOOLEAN, default).build()

    override fun createClothNode(node: Property<Boolean>, names: List<String>) = ConfigEntryBuilder.create()
        .startBooleanToggle(textify(langKey(names).translate()), node.value)
        .setTooltip(langKey(names).let { if (I18n.hasTranslation("$it.tooltip")) Optional.of(textify(it.translateTooltip())) else Optional.empty() })
        .setSaveConsumer { node.value = valueOverride(it) }
        .build()
}

fun integer(
    default: Int, min: Int, max: Int,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Int)->Int = { it }
) = object : DelegatingConfigValueFactory<Int> {
    override fun createFiberNode(builder: ConfigTreeBuilder, name: String): Property<Int> {
        val type = ConfigTypes.INTEGER.withMinimum(min).withMaximum(max)
        return PropertyMirror.create(type).also {
            builder.beginValue(name, type, default).finishValue(it::mirror)
        }
    }

    override fun createClothNode(node: Property<Int>, names: List<String>) = ConfigEntryBuilder.create()
        .startIntField(textify(langKey(names).translate()), node.value)
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
    override fun createFiberNode(builder: ConfigTreeBuilder, name: String): Property<Double> {
        val type = ConfigTypes.DOUBLE.withMinimum(min).withMaximum(max)
        return PropertyMirror.create(type).also {
            builder.beginValue(name, type, default).finishValue(it::mirror)
        }
    }

    override fun createClothNode(node: Property<Double>, names: List<String>) = ConfigEntryBuilder.create()
        .startDoubleField(textify(langKey(names).translate()), node.value)
        .setTooltip(langKey(names).let { if (I18n.hasTranslation("$it.tooltip")) Optional.of(textify(it.translateTooltip())) else Optional.empty() })
        .setMin(min).setMax(max)
        .setSaveConsumer { node.value = valueOverride(it) }
        .build()
}

val recurring = { names: List<String> -> "${names.first()}.${names.last()}" }
fun fakeCategory(name: String) = { names: List<String> ->
    (listOf(names.first(), name) + names.drop(1)).joinToString(".")
}