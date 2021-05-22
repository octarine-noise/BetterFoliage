package mods.betterfoliage.config

import me.shedaniel.clothconfig2.forge.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.forge.api.ConfigBuilder
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.forge.gui.entries.SubCategoryListEntry
import mods.betterfoliage.util.asText
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.ForgeConfigSpec
import java.util.Optional
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

const val MAX_LINE_LEN = 30

fun DelegatingConfigGroup.forgeSpecRoot() =
    ForgeConfigSpec.Builder()
        .also { createForgeNode(it) }
        .build()

fun DelegatingConfigGroup.clothGuiRoot(
    parentScreen: Screen,
    prefix: List<String>,
    background: ResourceLocation,
    saveAction: ()->Unit
) = ConfigBuilder.create()
    .setParentScreen(parentScreen)
    .setTitle(I18n.get((prefix + "title").joinToString(".")).asText())
    .setDefaultBackgroundTexture(background)
    .setSavingRunnable(saveAction)
    .also { builder ->
        createClothNode(prefix).value.forEach { rootCategory ->
            builder.getOrCreateCategory("main".asText()).addEntry(rootCategory)
        }
    }
    .build()

sealed class DelegatingConfigNode {
    abstract fun createClothNode(path: List<String>): AbstractConfigListEntry<*>
}

abstract class DelegatingConfigValue<T> : DelegatingConfigNode(), ReadOnlyProperty<DelegatingConfigGroup, T> {
    lateinit var forgeValue: ForgeConfigSpec.ConfigValue<T>
    abstract fun createForgeNode(builder: ForgeConfigSpec.Builder, name: String)
}

open class DelegatingConfigGroup : DelegatingConfigNode() {
    val children = mutableMapOf<String, DelegatingConfigNode>()

    fun createForgeNode(builder: ForgeConfigSpec.Builder) {
        children.forEach { (name, node) ->
            when(node) {
                is DelegatingConfigGroup -> {
                    builder.push(name)
                    node.createForgeNode(builder)
                    builder.pop()
                }
                is DelegatingConfigValue<*> -> node.createForgeNode(builder, name)
            }
        }
    }

    override fun createClothNode(path: List<String>): SubCategoryListEntry {
        val builder = ConfigEntryBuilder.create()
            .startSubCategory(path.joinToString(".").translate())
            .setTooltip(*path.joinToString(".").translateTooltip())
            .setExpanded(false)
        children.forEach { (name, node) -> builder.add(node.createClothNode(path + name)) }
        return builder.build()
    }
}

interface DelegatingConfigGroupFactory<T> {
    operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T>
}

fun <T: DelegatingConfigGroup> subNode(factory: ()->T) = object : DelegatingConfigGroupFactory<T> {
    override operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T> {
        val child = factory()
        parent.children[property.name] = child
        return ReadOnlyProperty { _, _ -> child }
    }
}

interface DelegatingConfigValueFactory<T> {
    fun createForgeNode(builder: ForgeConfigSpec.Builder, name: String): ForgeConfigSpec.ConfigValue<T>
    fun createClothNode(prop: CachingConfigProperty<T>, path: List<String>): AbstractConfigListEntry<T>

    operator fun provideDelegate(parent: DelegatingConfigGroup, property: KProperty<*>): ReadOnlyProperty<DelegatingConfigGroup, T> {
        return object : CachingConfigProperty<T>(parent, property) {
            override fun createForgeNode(builder: ForgeConfigSpec.Builder, name: String) {
                forgeValue = this@DelegatingConfigValueFactory.createForgeNode(builder, name)
            }

            override fun createClothNode(path: List<String>): AbstractConfigListEntry<*> = createClothNode(this, path)

        }.apply { parent.children[property.name] = this }
    }
}

abstract class CachingConfigProperty<T>(parent: DelegatingConfigGroup, property: KProperty<*>) : DelegatingConfigValue<T>() {
    var value: T? = null
    override fun getValue(thisRef: DelegatingConfigGroup, property: KProperty<*>) =
        value ?: forgeValue.get().apply { value = this }
}

fun String.translate() = I18n.get(this).asText()
fun String.translateTooltip(lineLength: Int = MAX_LINE_LEN) =
    I18n.get("$this.tooltip").splitToSequence(" ").fold(mutableListOf("")) { tooltips, word ->
        if (tooltips.last().length + word.length < lineLength) {
            tooltips[tooltips.lastIndex] += "$word "
        } else {
            tooltips.add("$word ")
        }
        tooltips
    }.map { it.trim().asText() }.toTypedArray()

fun boolean(
    default: Boolean,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Boolean)->Boolean = { it }
) = object : DelegatingConfigValueFactory<Boolean> {
    override fun createForgeNode(builder: ForgeConfigSpec.Builder, name: String) =
        builder.define(name, default)

    override fun createClothNode(prop: CachingConfigProperty<Boolean>, path: List<String>) = ConfigEntryBuilder.create()
        .startBooleanToggle(langKey(path).translate(), prop.forgeValue.get())
        .setTooltip(langKey(path).let { if (I18n.exists("$it.tooltip")) Optional.of(it.translateTooltip()) else Optional.empty() })
        .setSaveConsumer { prop.forgeValue.set(valueOverride(it)); prop.value = null }
        .build()
}

fun integer(
    default: Int = 0, min: Int = 0, max: Int,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Int)->Int = { it }
) = object : DelegatingConfigValueFactory<Int> {
    override fun createForgeNode(builder: ForgeConfigSpec.Builder, name: String) =
        builder.defineInRange(name, default, min, max)

    override fun createClothNode(prop: CachingConfigProperty<Int>, path: List<String>) = ConfigEntryBuilder.create()
        .startIntField(langKey(path).translate(), prop.forgeValue.get())
        .setTooltip(langKey(path).let { if (I18n.exists("$it.tooltip")) Optional.of(it.translateTooltip()) else Optional.empty() })
        .setMin(min).setMax(max)
        .setSaveConsumer { prop.forgeValue.set(valueOverride(it)); prop.value = null }
        .build()
}

fun double(
    default: Double = 0.0, min: Double = 0.0, max: Double = 1.0,
    langKey: (List<String>)->String = { it.joinToString(".") },
    valueOverride: (Double)->Double = { it }
) = object : DelegatingConfigValueFactory<Double> {
    override fun createForgeNode(builder: ForgeConfigSpec.Builder, name: String) =
        builder.defineInRange(name, default, min, max)

    override fun createClothNode(prop: CachingConfigProperty<Double>, path: List<String>) = ConfigEntryBuilder.create()
        .startDoubleField(langKey(path).translate(), prop.forgeValue.get())
        .setTooltip(langKey(path).let { if (I18n.exists("$it.tooltip")) Optional.of(it.translateTooltip()) else Optional.empty() })
        .setMin(min).setMax(max)
        .setSaveConsumer { prop.forgeValue.set(valueOverride(it)); prop.value = null }
        .build()
}

val recurring = { path: List<String> -> "${path.first()}.${path.last()}" }
fun fakeCategory(name: String) = { names: List<String> ->
    (listOf(names.first(), name) + names.drop(1)).joinToString(".")
}