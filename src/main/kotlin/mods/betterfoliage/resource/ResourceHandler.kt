package mods.betterfoliage.resource

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.render.old.Model
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.completedVoid
import mods.betterfoliage.util.sink
import mods.octarinecore.client.resource.AsyncSpriteProvider
import mods.octarinecore.client.resource.AtlasFuture
import mods.octarinecore.client.resource.StitchPhases
import net.minecraft.resources.IResourceManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.IWorld
import net.minecraft.world.gen.SimplexNoiseGenerator
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.config.ModConfig
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// ============================
// Resource types
// ============================
interface IConfigChangeListener { fun onConfigChange() }
interface IWorldLoadListener { fun onWorldLoad(world: IWorld) }

/**
 * Base class for declarative resource handling.
 *
 * Resources are automatically reloaded/recalculated when the appropriate events are fired.
 *
 * @param[modId] mod ID associated with this handler (used to filter config change events)
 */
open class ResourceHandler(
    val modId: String,
    val modBus: IEventBus,
    val targetAtlas: Atlas = Atlas.BLOCKS
) {

    val resources = mutableListOf<Any>()
    // ============================
    // Self-registration
    // ============================
    init { modBus.register(this) }

    // ============================
    // Resource declarations
    // ============================
    fun sprite(id: Identifier) = sprite { id }
    fun sprite(idFunc: ()->Identifier) = AsyncSpriteDelegate(idFunc).apply { BetterFoliage.getSpriteManager(targetAtlas).providers.add(this) }
    fun spriteSet(idFunc: (Int)->Identifier) = AsyncSpriteSet(targetAtlas, idFunc).apply { BetterFoliage.getSpriteManager(targetAtlas).providers.add(this) }
    fun spriteSetTransformed(check: (Int)->Identifier, register: (Identifier)->Identifier) =
        AsyncSpriteSet(targetAtlas, check, register).apply { BetterFoliage.getSpriteManager(targetAtlas).providers.add(this) }
    fun model(init: Model.()->Unit) = ModelHolder(init).apply { resources.add(this) }
    fun modelSet(num: Int, init: Model.(Int)->Unit) = ModelSet(num, init).apply { resources.add(this) }
    fun vectorSet(num: Int, init: (Int)-> Double3) = VectorSet(num, init).apply { resources.add(this) }
    fun simplexNoise() = SimplexNoise().apply { resources.add(this) }

    // ============================
    // Event registration
    // ============================
    @SubscribeEvent
    fun handleModConfigChange(event: ModConfig.ModConfigEvent) {
        resources.forEach { (it as? IConfigChangeListener)?.onConfigChange() }
    }

    @SubscribeEvent
    fun handleWorldLoad(event: WorldEvent.Load) =
        resources.forEach { (it as? IWorldLoadListener)?.onWorldLoad(event.world) }
}

// ============================
// Resource container classes
// ============================
class AsyncSpriteDelegate(val idFunc: ()->Identifier) : ReadOnlyProperty<Any, Sprite>, AsyncSpriteProvider<Any> {
    protected lateinit var value: Sprite
    override fun getValue(thisRef: Any, property: KProperty<*>) = value

    override fun setup(manager: IResourceManager, sourceF: CompletableFuture<Any>, atlas: AtlasFuture): StitchPhases {
        sourceF.thenRun {
            val sprite = atlas.sprite(idFunc())
            atlas.runAfter {
                sprite.handle { sprite, error -> value = sprite ?: atlas.missing.get()!! }
            }
        }
        return StitchPhases(completedVoid(), completedVoid())
    }
}

interface SpriteSet {
    val num: Int
    operator fun get(idx: Int): Sprite
}

class AsyncSpriteSet(val targetAtlas: Atlas = Atlas.BLOCKS, val idFunc: (Int)->Identifier, val transform: (Identifier)->Identifier = { it }) :
    AsyncSpriteProvider<Any> {
    var num = 0
        protected set
    protected var sprites: List<Sprite> = emptyList()

    override fun setup(manager: IResourceManager, sourceF: CompletableFuture<Any>, atlas: AtlasFuture): StitchPhases {
        var list: List<CompletableFuture<Sprite>> = emptyList()

        return StitchPhases(
            discovery = sourceF.sink {
                list = (0 until 16).map { idFunc(it) }
                    .filter { manager.hasResource( targetAtlas.wrap(it)) }
                    .map { transform(it) }
                    .map { atlas.sprite(it) }
            },
            cleanup = atlas.runAfter {
                sprites = list.filter { !it.isCompletedExceptionally }.map { it.get() }
                if (sprites.isEmpty()) sprites = listOf(atlas.missing.get()!!)
                num = sprites.size
            }
        )
    }
    operator fun get(idx: Int) = sprites[idx % num]
}

class ModelHolder(val init: Model.()->Unit): IConfigChangeListener {
    var model: Model = Model()
    override fun onConfigChange() { model = Model().apply(init) }
}

class ModelSet(val num: Int, val init: Model.(Int)->Unit): IConfigChangeListener {
    val models = Array(num) { Model() }
    override fun onConfigChange() { (0 until num).forEach { models[it] = Model().apply{ init(it) } } }
    operator fun get(idx: Int) = models[idx % num]
}

class VectorSet(val num: Int, val init: (Int)-> Double3): IConfigChangeListener {
    val models = Array(num) { Double3.zero }
    override fun onConfigChange() { (0 until num).forEach { models[it] = init(it) } }
    operator fun get(idx: Int) = models[idx % num]
}

class SimplexNoise : IWorldLoadListener {
    var noise = SimplexNoiseGenerator(Random())
    override fun onWorldLoad(world: IWorld) { noise = SimplexNoiseGenerator(Random(world.worldInfo.seed))
    }
    operator fun get(x: Int, z: Int) = MathHelper.floor((noise.getValue(x.toDouble(), z.toDouble()) + 1.0) * 32.0)
    operator fun get(pos: Int3) = get(pos.x, pos.z)
    operator fun get(pos: BlockPos) = get(pos.x, pos.z)
}
