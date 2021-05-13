package mods.betterfoliage.resource

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloadListener
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Catch resource reload extremely early, before any of the reloaders
 * have started working.
 */
interface VeryEarlyReloadListener : ResourceReloadListener, IdentifiableResourceReloadListener {
    override fun reload(
        synchronizer: ResourceReloadListener.Synchronizer,
        resourceManager: ResourceManager,
        preparationsProfiler: Profiler,
        reloadProfiler: Profiler,
        backgroundExecutor: Executor,
        gameExecutor: Executor
    ): CompletableFuture<Void> {
        onReloadStarted(resourceManager)
        return synchronizer.whenPrepared(null)
    }

    fun onReloadStarted(resourceManager: ResourceManager) {}
}