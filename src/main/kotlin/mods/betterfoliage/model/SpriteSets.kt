package mods.betterfoliage.model

import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.get
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.Identifier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface SpriteSet {
    val num: Int
    operator fun get(idx: Int): Sprite
}

class FixedSpriteSet(val sprites: List<Sprite>) : SpriteSet {
    override val num = sprites.size
    override fun get(idx: Int) = sprites[idx % num]

    constructor(atlas: Atlas, ids: List<Identifier>) : this(
        ids.mapNotNull { atlas[it] }.let { sprites ->
            if (sprites.isNotEmpty()) sprites else listOf(atlas[MissingSprite.getMissingSpriteId()]!!)
        }
    )
}

class SpriteDelegate(val atlas: Atlas, val idFunc: ()->Identifier) : ReadOnlyProperty<Any, Sprite>, ClientSpriteRegistryCallback {
    private var id: Identifier? = null
    private var value: Sprite? = null

    init { ClientSpriteRegistryCallback.event(atlas.resourceId).register(this) }

    override fun registerSprites(atlasTexture: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry) {
        id = idFunc(); value = null
        registry.register(id)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): Sprite {
        value?.let { return it }
        synchronized(this) {
            value?.let { return it }
            atlas[id!!]!!.let { value = it; return it }
        }
    }
}


class SpriteSetDelegate(
    val atlas: Atlas,
    val idRegister: (Identifier)->Identifier = { it },
    val idFunc: (Int)->Identifier
) : ReadOnlyProperty<Any, SpriteSet>, ClientSpriteRegistryCallback {
    private var idList: List<Identifier> = emptyList()
    private var spriteSet: SpriteSet? = null
    init { ClientSpriteRegistryCallback.event(atlas.resourceId).register(this) }

    override fun registerSprites(atlasTexture: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry) {
        spriteSet = null
        val manager = MinecraftClient.getInstance().resourceManager
        idList = (0 until 16).map(idFunc).filter { manager.containsResource(atlas.file(it)) }.map(idRegister)
        idList.forEach { registry.register(it) }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): SpriteSet {
        spriteSet?.let { return it }
        synchronized(this) {
            spriteSet?.let { return it }
            spriteSet = FixedSpriteSet(atlas, idList)
            return spriteSet!!
        }
    }
}