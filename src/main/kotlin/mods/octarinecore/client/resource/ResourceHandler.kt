package mods.octarinecore.client.resource

import mods.octarinecore.client.render.Model
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraft.world.gen.NoiseGeneratorSimplex
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

// ============================
// Resource types
// ============================
interface IStitchListener {
    fun onPreStitch(atlas: TextureMap)
    fun onPostStitch(atlas: TextureMap)
}
interface IConfigChangeListener { fun onConfigChange() }
interface IWorldLoadListener { fun onWorldLoad(world: World) }

/**
 * Base class for declarative resource handling.
 *
 * Resources are automatically reloaded/recalculated when the appropriate events are fired.
 *
 * @param[modId] mod ID associated with this handler (used to filter config change events)
 */
open class ResourceHandler(val modId: String) {

    val resources = mutableListOf<Any>()
    open fun afterPreStitch() {}
    open fun afterPostStitch() {}

    // ============================
    // Self-registration
    // ============================
    init { MinecraftForge.EVENT_BUS.register(this) }

    // ============================
    // Resource declarations
    // ============================
    fun iconStatic(domain: String, path: String) = IconHolder(domain, path).apply { resources.add(this) }
    fun iconStatic(location: ResourceLocation) = iconStatic(location.namespace, location.path)
    fun iconSet(domain: String, pathPattern: String) = IconSet(domain, pathPattern).apply { this@ResourceHandler.resources.add(this) }
    fun iconSet(location: ResourceLocation) = iconSet(location.namespace, location.path)
    fun model(init: Model.()->Unit) = ModelHolder(init).apply { resources.add(this) }
    fun modelSet(num: Int, init: Model.(Int)->Unit) = ModelSet(num, init).apply { resources.add(this) }
    fun vectorSet(num: Int, init: (Int)-> Double3) = VectorSet(num, init).apply { resources.add(this) }
    fun simplexNoise() = SimplexNoise().apply { resources.add(this) }

    // ============================
    // Event registration
    // ============================
    @SubscribeEvent
    fun onPreStitch(event: TextureStitchEvent.Pre) {
        resources.forEach { (it as? IStitchListener)?.onPreStitch(event.map) }
        afterPreStitch()
    }

    @SubscribeEvent
    fun onPostStitch(event: TextureStitchEvent.Post) {
        resources.forEach { (it as? IStitchListener)?.onPostStitch(event.map) }
        afterPostStitch()
    }

    @SubscribeEvent
    fun handleConfigChange(event: ConfigChangedEvent.OnConfigChangedEvent) {
        if (event.modID == modId) resources.forEach { (it as? IConfigChangeListener)?.onConfigChange() }
    }

    @SubscribeEvent
    fun handleWorldLoad(event: WorldEvent.Load) =
        resources.forEach { (it as? IWorldLoadListener)?.onWorldLoad(event.world) }
}

// ============================
// Resource container classes
// ============================
class IconHolder(val domain: String, val name: String) : IStitchListener {
    val iconRes = ResourceLocation(domain, name)
    var icon: TextureAtlasSprite? = null
    override fun onPreStitch(atlas: TextureMap) { atlas.registerSprite(iconRes) }
    override fun onPostStitch(atlas: TextureMap) { icon = atlas[iconRes] }
}

class ModelHolder(val init: Model.()->Unit): IConfigChangeListener {
    var model: Model = Model().apply(init)
    override fun onConfigChange() { model = Model().apply(init) }
}

class IconSet(val domain: String, val namePattern: String) : IStitchListener {
    val resources = arrayOfNulls<ResourceLocation>(16)
    val icons = arrayOfNulls<TextureAtlasSprite>(16)
    var num = 0

    override fun onPreStitch(atlas: TextureMap) {
        num = 0
        (0..15).forEach { idx ->
            icons[idx] = null
            val locReal = ResourceLocation(domain, "textures/${namePattern.format(idx)}.png")
            if (resourceManager[locReal] != null) resources[num++] = ResourceLocation(domain, namePattern.format(idx)).apply { atlas.registerSprite(this) }
        }
    }

    override fun onPostStitch(atlas: TextureMap) {
        (0 until num).forEach { idx -> icons[idx] = atlas[resources[idx]!!] }
    }

    operator fun get(idx: Int) = if (num == 0) null else icons[idx % num]
}

class ModelSet(val num: Int, val init: Model.(Int)->Unit): IConfigChangeListener {
    val models = Array(num) { Model().apply{ init(it) } }
    override fun onConfigChange() { (0 until num).forEach { models[it] = Model().apply{ init(it) } } }
    operator fun get(idx: Int) = models[idx % num]
}

class VectorSet(val num: Int, val init: (Int)->Double3): IConfigChangeListener {
    val models = Array(num) { init(it) }
    override fun onConfigChange() { (0 until num).forEach { models[it] = init(it) } }
    operator fun get(idx: Int) = models[idx % num]
}

class SimplexNoise() : IWorldLoadListener {
    var noise = NoiseGeneratorSimplex()
    override fun onWorldLoad(world: World) { noise = NoiseGeneratorSimplex(Random(world.worldInfo.seed))
    }
    operator fun get(x: Int, z: Int) = MathHelper.floor((noise.getValue(x.toDouble(), z.toDouble()) + 1.0) * 32.0)
    operator fun get(pos: Int3) = get(pos.x, pos.z)
    operator fun get(pos: BlockPos) = get(pos.x, pos.z)
}