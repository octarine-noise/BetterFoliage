package mods.octarinecore.client.resource

import mods.octarinecore.client.render.Model
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.stripEnd
import mods.octarinecore.stripStart
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.IWorld
import net.minecraft.world.gen.SimplexNoiseGenerator
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.config.ModConfig
import java.util.*

enum class Atlas(val basePath: String) {
    BLOCKS("textures"),
    PARTICLES("textures/particle");

    fun wrap(resource: ResourceLocation) = ResourceLocation(resource.namespace, "$basePath/${resource.path}.png")
    fun unwrap(resource: ResourceLocation) = resource.stripStart("$basePath/").stripEnd(".png")
    fun matches(event: TextureStitchEvent) = event.map.basePath == basePath
}

// ============================
// Resource types
// ============================
interface IStitchListener {
    fun onPreStitch(event: TextureStitchEvent.Pre)
    fun onPostStitch(atlas: AtlasTexture)
}
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
    open fun afterPreStitch() {}
    open fun afterPostStitch() {}

    // ============================
    // Self-registration
    // ============================
    init { modBus.register(this) }

    // ============================
    // Resource declarations
    // ============================
    fun iconStatic(location: ()->ResourceLocation) = IconHolder(location).apply { resources.add(this) }
    fun iconStatic(location: ResourceLocation) = iconStatic { location }
    fun iconStatic(domain: String, path: String) = iconStatic(ResourceLocation(domain, path))
    fun iconSet(targetAtlas: Atlas = Atlas.BLOCKS, location: (Int)->ResourceLocation) = IconSet(targetAtlas, location).apply { this@ResourceHandler.resources.add(this) }
    fun model(init: Model.()->Unit) = ModelHolder(init).apply { resources.add(this) }
    fun modelSet(num: Int, init: Model.(Int)->Unit) = ModelSet(num, init).apply { resources.add(this) }
    fun vectorSet(num: Int, init: (Int)-> Double3) = VectorSet(num, init).apply { resources.add(this) }
    fun simplexNoise() = SimplexNoise().apply { resources.add(this) }

    // ============================
    // Event registration
    // ============================
    @SubscribeEvent
    fun onPreStitch(event: TextureStitchEvent.Pre) {
        if (!targetAtlas.matches(event)) return
        resources.forEach { (it as? IStitchListener)?.onPreStitch(event) }
        afterPreStitch()
    }

    @SubscribeEvent
    fun onPostStitch(event: TextureStitchEvent.Post) {
        if (!targetAtlas.matches(event)) return
        resources.forEach { (it as? IStitchListener)?.onPostStitch(event.map) }
        afterPostStitch()
    }

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
class IconHolder(val location: ()->ResourceLocation) : IStitchListener {
    var iconRes: ResourceLocation? = null
    var icon: TextureAtlasSprite? = null
    override fun onPreStitch(event: TextureStitchEvent.Pre) {
        iconRes = location()
        event.addSprite(iconRes)
    }
    override fun onPostStitch(atlas: AtlasTexture) {
        icon = atlas[iconRes!!]
    }
}

class ModelHolder(val init: Model.()->Unit): IConfigChangeListener {
    var model: Model = Model()
    override fun onConfigChange() { model = Model().apply(init) }
}

class IconSet(val targetAtlas: Atlas, val location: (Int)->ResourceLocation) : IStitchListener {
    val resources = arrayOfNulls<ResourceLocation>(16)
    val icons = arrayOfNulls<TextureAtlasSprite>(16)
    var num = 0

    override fun onPreStitch(event: TextureStitchEvent.Pre) {
        num = 0
        (0..15).forEach { idx ->
            icons[idx] = null
            val loc = location(idx)
            if (resourceManager[targetAtlas.wrap(loc)] != null) resources[num++] = loc.apply { event.addSprite(this) }
        }
    }

    override fun onPostStitch(atlas: AtlasTexture) {
        (0 until num).forEach { idx -> icons[idx] = atlas[resources[idx]!!] }
    }

    operator fun get(idx: Int) = if (num == 0) null else icons[idx % num]
}

class ModelSet(val num: Int, val init: Model.(Int)->Unit): IConfigChangeListener {
    val models = Array(num) { Model() }
    override fun onConfigChange() { (0 until num).forEach { models[it] = Model().apply{ init(it) } } }
    operator fun get(idx: Int) = models[idx % num]
}

class VectorSet(val num: Int, val init: (Int)->Double3): IConfigChangeListener {
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