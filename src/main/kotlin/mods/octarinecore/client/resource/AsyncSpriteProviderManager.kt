package mods.octarinecore.client.resource

import mods.betterfoliage.client.resource.Identifier
import mods.betterfoliage.client.resource.Sprite
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

class StitchPhases(
    val discovery: CompletableFuture<Void>,
    val cleanup: CompletableFuture<Void>
)

interface AsyncSpriteProvider {
    fun setup(bakeryFuture: CompletableFuture<ModelBakery>, atlasFuture: AtlasFuture): StitchPhases
}

object AsyncSpriteProviderManager {

    val providers = mutableListOf<ModelDiscovery<*>>()

    fun onStitchBlockAtlas(bakeryObj: Any, atlas: AtlasTexture, manager: IResourceManager, idList: Iterable<Identifier>, profiler: IProfiler): AtlasTexture.SheetData {
        profiler.startSection("additional-sprites")

        val bakery = CompletableFuture<ModelBakery>()
        val atlasFuture = AtlasFuture(idList)

        val phases = providers.map { it.setup(bakery, atlasFuture) }
        bakery.complete(bakeryObj as ModelBakery)
        phases.forEach { it.discovery.get() }
        val sheetData = atlas.stitch(manager, idList, profiler)
        atlasFuture.sheet.complete(sheetData)
        phases.forEach { it.cleanup.get() }

        profiler.endSection()
        return sheetData
    }
}

class AtlasFuture(initial: Iterable<Identifier>) {
    val idSet = Collections.synchronizedSet(mutableSetOf<Identifier>().apply { addAll(initial) })
    val sheet = CompletableFuture<AtlasTexture.SheetData>()
    fun sprite(id: String) = sprite(Identifier(id))
    fun sprite(id: Identifier): CompletableFuture<Sprite> {
        idSet.add(id)
        return sheet.thenApply { sheetData -> sheetData.sprites.find { it.name == id } ?: throw IllegalStateException("Atlas does not contain $id") }.toCompletableFuture()
    }
    fun <T> afterStitch(supplier: ()->T): CompletableFuture<T> = sheet.thenApplyAsync(Function { supplier() }, Minecraft.getInstance())
}

fun completedVoid() = CompletableFuture.completedFuture<Void>(null)
fun <T> CompletableFuture<T>.thenRunAsync(run: (T)->Unit) = thenAcceptAsync(Consumer(run), Minecraft.getInstance()).toCompletableFuture()!!
fun Collection<CompletableFuture<*>>.allComplete() = CompletableFuture.allOf(*toTypedArray())