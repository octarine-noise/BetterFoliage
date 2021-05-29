package mods.betterfoliage.model

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.resourceManager
import net.minecraft.client.renderer.texture.MissingTextureSprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface SpriteSet {
    val num: Int
    operator fun get(idx: Int): TextureAtlasSprite
}

class FixedSpriteSet(val sprites: List<TextureAtlasSprite>) : SpriteSet {
    override val num = sprites.size
    override fun get(idx: Int) = sprites[idx % num]
}

class SpriteDelegate(val atlas: Atlas, val idFunc: () -> ResourceLocation) : ReadOnlyProperty<Any, TextureAtlasSprite> {
    private lateinit var id: ResourceLocation
    private var value: TextureAtlasSprite? = null

    init {
        BetterFoliageMod.bus.register(this)
    }

    @SubscribeEvent
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        id = idFunc(); value = null
        event.addSprite(id)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): TextureAtlasSprite {
        value?.let { return it }
        synchronized(this) {
            value?.let { return it }
            atlas[id].let { value = it; return it }
        }
    }

}

class SpriteSetDelegate(
    val atlas: Atlas,
    val idRegister: (ResourceLocation) -> ResourceLocation = { it },
    val idFunc: (Int) -> ResourceLocation
) : ReadOnlyProperty<Any, SpriteSet> {
    private var idList: List<ResourceLocation> = emptyList()
    private var spriteSet: SpriteSet? = null

    init {
        BetterFoliageMod.bus.register(this)
    }

    @SubscribeEvent
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        if (event.map.location() != atlas.resourceId) return
        spriteSet = null
        idList = (0 until 16)
            .map(idFunc)
            .filter { resourceManager.hasResource(atlas.file(it)) }
            .map(idRegister)
        idList.forEach { event.addSprite(it) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): SpriteSet {
        spriteSet?.let { return it }
        synchronized(this) {
            spriteSet?.let { return it }
            spriteSet = FixedSpriteSet(
                idList
                    .ifEmpty { listOf(MissingTextureSprite.getLocation()) }
                    .map { atlas[it] }
            )
            return spriteSet!!
        }
    }
}