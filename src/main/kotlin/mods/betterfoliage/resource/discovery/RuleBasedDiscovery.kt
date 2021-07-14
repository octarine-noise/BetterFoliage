package mods.betterfoliage.resource.discovery

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.match.MAnything
import mods.betterfoliage.config.match.MListAll
import mods.betterfoliage.config.match.MListAny
import mods.betterfoliage.config.match.MValue
import mods.betterfoliage.config.match.MatchRules
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
        val ruleResults = mutableListOf<MListAll>()
        var previousSize = 0

        // stop processing if nothing changes anymore
        while (rulesToCheck.size != previousSize) {
            previousSize = rulesToCheck.size
            val iterator = rulesToCheck.listIterator()
            while (iterator.hasNext()) iterator.next().let { rule ->
                // process single rule
                MatchRules.visitRoot(ruleCtx, rule).let { result ->
                    ruleResults.add(result)
                    // remove rule from active list if:
                    //  - rule succeeded (all directives returned success)
                    //  - rule is immutable (result will always be the same)
                    if (result.value || result.immutable) iterator.remove()
                }
            }
        }

        // log result of rule processing
        if (ruleResults.any { it.value }) {
            detailLogger.log(Level.INFO, "================================")
            detailLogger.log(Level.INFO, "block state: ${ctx.blockState}")
            detailLogger.log(Level.INFO, "block class: ${ctx.blockState.block::class.java.name}")
            detailLogger.log(Level.INFO, "model      : ${ctx.modelLocation}")
            detailLogger.log(Level.INFO, "--------------------------------")
            ruleResults.forEach { logResult(it) }
        }

        discoverers[ruleCtx.params["type"]]?.processModel(ctx, ruleCtx.params)
    }

    fun logResult(match: MAnything<Boolean>) {
        when(match) {
            is MListAll -> if (match.list.any { it.value }) {
                var seenInvariantSuccess = false
                match.list.forEach { item ->
                    if (item.immutable && item.value) seenInvariantSuccess = true
                    if (seenInvariantSuccess) logResult(item)
                }
            }
            is MListAny -> if (match.value) match.list.first { it.value }.let { logResult(it) }
                else match.list.forEach { logResult(it) }
            is MValue<Boolean> -> detailLogger.log(Level.INFO, "[${match.configSource}] ${match.description}")
        }
    }
}