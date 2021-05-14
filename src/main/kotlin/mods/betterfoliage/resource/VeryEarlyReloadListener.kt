package mods.betterfoliage.resource

import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IFutureReloadListener
import net.minecraft.resources.IResourceManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Catch resource reload extremely early, before any of the reloaders
 * have started working.
 */
interface VeryEarlyReloadListener : IFutureReloadListener {
    override fun reload(
        stage: IFutureReloadListener.IStage,
        resourceManager: IResourceManager,
        preparationsProfiler: IProfiler,
        reloadProfiler: IProfiler,
        backgroundExecutor: Executor,
        gameExecutor: Executor
    ): CompletableFuture<Void> {
        onReloadStarted()
        return stage.wait(null)
    }

    fun onReloadStarted() {}
}