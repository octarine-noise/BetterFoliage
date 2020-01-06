package mods.octarinecore.client.resource

import mods.betterfoliage.client.resource.Identifier
import mods.betterfoliage.client.resource.Sprite
import mods.octarinecore.common.map
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.MissingTextureSprite
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Main entry point to atlas manipulation. Called from mixins that wrap [AtlasTexture.stitch] calls.
 *
 * 1. All registered providers receive an [AsyncSpriteProvider.setup] call. Providers can set up their
 *   processing chain at this point, but should not do anything yet except configuration and housekeeping.
 * 2. The [CompletableFuture] of the stitch source finishes, starting the "discovery" phase. Providers
 *   may register sprites in the [AtlasFuture].
 * 3. After all providers finish their discovery, the atlas is stitched.
 * 4. The [AtlasFuture] finishes, starting the "cleanup" phase. All [AtlasFuture.runAfter] and
 *   [AtlasFuture.mapAfter] tasks are processed.
 * 5. After all providers finish their cleanup, we return to the original code path.
 */
class AsnycSpriteProviderManager<SOURCE: Any>(val profilerSection: String) {

    val providers = mutableListOf<AsyncSpriteProvider<SOURCE>>()

    /**
     * Needed in order to keep the actual [AtlasTexture.stitch] call in the original method, in case
     * other modders want to modify it too.
     */
    class StitchWrapper(val idList: Iterable<Identifier>, val onComplete: (AtlasTexture.SheetData)->Unit) {
        fun complete(sheet: AtlasTexture.SheetData) = onComplete(sheet)
    }

    @Suppress("UNCHECKED_CAST")
    fun prepare(sourceObj: Any, atlas: AtlasTexture, manager: IResourceManager, idList: Iterable<Identifier>, profiler: IProfiler): StitchWrapper {
        profiler.startSection(profilerSection)

        val source = CompletableFuture<SOURCE>()
        val atlasFuture = AtlasFuture(idList)

        val phases = providers.map { it.setup(manager, source, atlasFuture) }
        source.complete(sourceObj as SOURCE)
        phases.forEach { it.discovery.get() }

        return StitchWrapper(atlasFuture.idSet) { sheet ->
            atlasFuture.complete(sheet)
            phases.forEach { it.cleanup.get() }
            profiler.endSection()
        }
    }
}

/**
 * Provides a way for [AsyncSpriteProvider]s to register sprites to receive [CompletableFuture]s.
 * Tracks sprite ids that need to be stitched.
 */
class AtlasFuture(initial: Iterable<Identifier>) {
    val idSet = Collections.synchronizedSet(mutableSetOf<Identifier>().apply { addAll(initial) })
    protected val sheet = CompletableFuture<AtlasTexture.SheetData>()
    protected val finished = CompletableFuture<Void>()

    fun complete(sheetData: AtlasTexture.SheetData) {
        sheet.complete(sheetData)
        finished.complete(null)
    }

    fun sprite(id: String) = sprite(Identifier(id))
    fun sprite(id: Identifier): CompletableFuture<Sprite> {
        idSet.add(id)
        return sheet.map { sheetData -> sheetData.sprites.find { it.name == id } ?: throw IllegalStateException("Atlas does not contain $id") }
    }
    val missing = sheet.map { sheetData -> sheetData.sprites.find { it.name == MissingTextureSprite.getLocation() } }
    fun <T> mapAfter(supplier: ()->T): CompletableFuture<T> = finished.map{ supplier() }
    fun runAfter(action: ()->Unit): CompletableFuture<Void> = finished.thenRun(action)
}

class StitchPhases(
    val discovery: CompletableFuture<Void>,
    val cleanup: CompletableFuture<Void>
)

interface AsyncSpriteProvider<SOURCE: Any> {
    fun setup(manager: IResourceManager, source: CompletableFuture<SOURCE>, atlas: AtlasFuture): StitchPhases
}
