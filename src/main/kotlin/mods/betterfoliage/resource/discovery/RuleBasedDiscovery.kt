package mods.betterfoliage.resource.discovery

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.match.MatchResult
import mods.betterfoliage.config.match.MatchRuleList
import mods.betterfoliage.config.match.Node
import mods.betterfoliage.util.HasLogger
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level

abstract class ParametrizedModelDiscovery : HasLogger() {
    abstract fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>)

    fun Map<String, String>.location(key: String): ResourceLocation? {
        val result = get(key)?.let { ResourceLocation(it) }
        if (result == null) detailLogger.log(Level.WARN, "Cannot find texture parameter \"$key\"")
        return result
    }

    fun Map<String, String>.int(key: String): Int? {
        val result = get(key)?.toInt()
        if (result == null) detailLogger.log(Level.WARN, "Cannot find integer parameter \"$key\"")
        return result
    }
}

class RuleProcessingContext(
    val discovery: ModelDiscoveryContext
) {
    val params = mutableMapOf("type" to "none")
}

class RuleBasedDiscovery : AbstractModelDiscovery() {
    val discoverers = mutableMapOf<String, ParametrizedModelDiscovery>()

    override fun processModel(ctx: ModelDiscoveryContext) = when(ctx.getUnbaked()) {
        is VariantList -> processContainerModel(ctx)
        is BlockModel -> processBlockModel(ctx)
        else -> Unit
    }

    fun processBlockModel(ctx: ModelDiscoveryContext) {
        val ruleCtx = RuleProcessingContext(ctx)
        val rulesToCheck = BetterFoliage.blockConfig.rules.toMutableList()
        val ruleResults = mutableListOf<MatchResult>()
        var previousSize = 0

        // stop processing if nothing changes anymore
        while (rulesToCheck.size != previousSize) {
            previousSize = rulesToCheck.size
            val iterator = rulesToCheck.listIterator()
            while (iterator.hasNext()) iterator.next().let { rule ->
                // process single rule
                MatchRuleList.visitRoot(ruleCtx, rule).let { result ->
                    ruleResults.add(result)
                    // remove rule from active list if:
                    //  - rule succeeded (all directives returned success)
                    //  - rule is invariant (result will always be the same)
                    if (result.isSuccess || result.isInvariant) iterator.remove()
                }
            }
        }

        // log result of rule processing
        if (ruleResults.any { it.isSuccess }) {
            detailLogger.log(Level.INFO, "================================")
            detailLogger.log(Level.INFO, "block state: ${ctx.blockState}")
            detailLogger.log(Level.INFO, "block class: ${ctx.blockState.block::class.java.name}")
            detailLogger.log(Level.INFO, "model      : ${ctx.modelLocation}")
            detailLogger.log(Level.INFO, "--------------------------------")
            ruleResults.forEach { result ->
                if (result !is MatchResult.RootList || result.results.shouldLog())
                    result.log { source, message -> detailLogger.log(Level.INFO, "[$source] $message") }
            }
        }

        discoverers[ruleCtx.params["type"]]?.processModel(ctx, ruleCtx.params)
    }

    fun List<MatchResult>.shouldLog() = all { it.isSuccess } || fold(false) { seenInvariantSuccess, result ->
        seenInvariantSuccess || (result.isSuccess && result.isInvariant)
    }
}