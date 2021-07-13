package mods.betterfoliage.config.match

import mods.betterfoliage.resource.discovery.RuleProcessingContext
import mods.betterfoliage.resource.discovery.getAncestry
import mods.betterfoliage.util.findFirst
import mods.betterfoliage.util.tryDefault
import net.minecraft.block.Block
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

object MatchRuleList {
    fun visitRoot(ctx: RuleProcessingContext, node: Node.MatchAll): MatchResult {
        val results = mutableListOf<MatchResult>()
        for (rule in node.list) {
            val result = when(rule) {
                is Node.MatchValueList -> visitMatchList(ctx, rule)
                is Node.MatchParam -> ParamMatchRules.visitMatch(ctx, rule)
                is Node.SetParam -> ParamMatchRules.visitSet(ctx, rule)
                else -> node.notImplemented()
            }
            results.add(result)
            if (!result.isSuccess) break
        }
        return MatchResult.RootList(node.configSource, results)
    }

    fun visitMatchList(ctx: RuleProcessingContext, node: Node.MatchValueList) = node.values.map { value ->
        try {
            when (node.matchSource) {
                Node.MatchSource.BLOCK_CLASS -> BlockMatchRules.visitClass(ctx, node, value)
                Node.MatchSource.BLOCK_NAME -> BlockMatchRules.visitName(ctx, node, value)
                Node.MatchSource.MODEL_LOCATION -> ModelMatchRules.visitModel(ctx, node, value)
            }
        } catch (e: Exception) {
            MatchResult.Error(node.configSource, e.message ?: "")
        }
    }.let { MatchResult.Any(node.configSource, it) }
}

object BlockMatchRules {
    fun visitClass(ctx: RuleProcessingContext, node: Node.MatchValueList, value: Node.Value): MatchResult {
        val source = ctx.discovery.blockState.block::class.java.let {
            MatchValue.Found("block class \"${it.name}\"", it)
        }
        val target = when(value) {
            is Node.Value.Literal -> tryDefault(null) { Class.forName(value.value) as Class<out Block> }
                ?.let { MatchValue.Found("class \"${value.value}\"", it) }
                ?: MatchValue.Missing("missing class \"${value.value}\"")

            is Node.Value.ClassOf -> ForgeRegistries.BLOCKS.getValue(ResourceLocation(value.value))
                ?.let { MatchValue.Found("class \"${it::class.java}\" of block \"${value.value}\"", it::class.java) }
                ?: MatchValue.Missing("class of missing block \"${value.value}\"")

            else -> MatchValue.Invalid("${value::class.java.name}(${value.value})")
        }
        return when(node.matchMethod) {
            MatchMethod.EXACT_MATCH -> node.compare(source, target, this::isExactClass)
            MatchMethod.EXTENDS -> node.compare(source, target, this::isExtendsClass)
            MatchMethod.CONTAINS -> node.error("invalid match type for block class: \"contains\"")
        }
    }

    fun visitName(ctx: RuleProcessingContext, node: Node.MatchValueList, value: Node.Value): MatchResult {
        val source = ctx.discovery.blockState.block.registryName?.let {
            MatchValue.Found("block name \"$it\"", it)
        } ?: MatchValue.Missing("missing block name")
        if (value !is Node.Value.Literal) return node.invalidValueType("block name", value)
        val (namespace, path) = if (value.value.contains(":")) ResourceLocation(value.value).let { it.namespace to it.path } else null to value.value
        return when(node.matchMethod) {
            MatchMethod.EXACT_MATCH -> node.compare<ResourceLocation>(source, value) { isExactName(it.value, namespace, path) }
            MatchMethod.CONTAINS -> node.compare<ResourceLocation>(source, value) { isContainsName(it.value, namespace, path) }
            MatchMethod.EXTENDS -> node.error("invalid match type for block name: \"extends\"")
        }
    }

    private fun isExactClass(source: MatchValue.Found<Class<out Block>>, target: MatchValue.Found<Class<out Block>>) =
        source.value == target.value
    private fun isExtendsClass(source: MatchValue.Found<Class<out Block>>, target: MatchValue.Found<Class<out Block>>) =
        target.value.isAssignableFrom(source.value)

    fun isExactName(source: ResourceLocation, namespace: String?, path: String) =
        (namespace == null || namespace == source.namespace) && path == source.path
    fun isContainsName(source: ResourceLocation, namespace: String?, path: String) =
        (namespace == null || source.namespace.contains(namespace)) && source.path.contains(path)
}

object ModelMatchRules {
    fun visitModel(ctx: RuleProcessingContext, node: Node.MatchValueList, value: Node.Value): MatchResult {
        val source = ctx.discovery.modelLocation.let { MatchValue.Found("model \"$it\"", it) }
        if (value !is Node.Value.Literal) return node.invalidValueType("model", value)
        val (namespace, path) = value.value.splitLocation()

        val check = when (node.matchMethod) {
            MatchMethod.EXACT_MATCH, MatchMethod.CONTAINS -> listOf(ctx.discovery.modelLocation)
            MatchMethod.EXTENDS -> ctx.discovery.bakery.getAncestry(ctx.discovery.modelLocation)
        }

        return when (node.matchMethod) {
            MatchMethod.EXACT_MATCH, MatchMethod.EXTENDS -> node.compare<ResourceLocation>(source, value) { isExactModel(check, namespace, path) }
            MatchMethod.CONTAINS -> node.compare<ResourceLocation>(source, value) { isContainsModel(check, namespace, path) }
        }
    }

    private fun String.splitLocation() = when(contains(":")) {
        true -> ResourceLocation(this).let { it.namespace to it.path }
        false -> null to this
    }

    private fun isExactModel(models: List<ResourceLocation>, namespace: String?, path: String) = models.any { model ->
        (namespace == null || namespace == model.namespace) && path == model.path
    }

    private fun isContainsModel(models: List<ResourceLocation>, namespace: String?, path: String) = models.any { model ->
        (namespace == null || model.namespace.contains(namespace)) && model.path.contains(path)
    }
}

object ParamMatchRules {
    fun visitMatch(ctx: RuleProcessingContext, node: Node.MatchParam) = node.values.map { value ->
        if (value !is Node.Value.Literal) return@map node.invalidValueType("parameter", value)
        val currentParamValue = ctx.params[node.name] ?: return@map MatchResult.UniComparison(
            isSuccess = false, isInvariant = false,
            node.configSource,
            MatchValue.Missing("missing parameter \"${node.name}\""), value.value,
            MatchMethod.EXACT_MATCH
        )
        val isSuccess = currentParamValue == value.value
        MatchResult.UniComparison(
            isSuccess, false,
            node.configSource,
            MatchValue.Found("parameter \"${node.name}\"", currentParamValue), value.value,
            MatchMethod.EXACT_MATCH
        )
    }.let { MatchResult.Any(node.configSource, it) }

    fun visitSet(ctx: RuleProcessingContext, node: Node.SetParam): MatchResult {
        val target = when(node.value) {
            is Node.Value.Literal -> node.value.value.let { MatchValue.Found("\"$it\"", it) }
            is Node.Value.Texture -> when(val model = ctx.discovery.getUnbaked()) {
                is BlockModel -> getModelTexture(model, node.value.value)
                else -> return node.error("cannot get texture from ${model::class.java.name}")
            }
            is Node.Value.Tint -> when(val model = ctx.discovery.getUnbaked()) {
                is BlockModel -> getModelTint(ctx.discovery.loadHierarchy(model), node.value.value)
                else -> return node.error("cannot get tint index from ${model::class.java.name}")
            }
            else -> return node.invalidValueType("parameter", node.value)
        }
        ctx.params[node.name] = target.value
        return node.action("parameter \"${node.name}\" set to ${target.description}")
    }

    fun getModelTexture(model: BlockModel, spriteName: String) =
        model.getMaterial(spriteName).texture().toString().let {
            MatchValue.Found("texture \"${spriteName}\" = \"$it\"", it)
        }

    fun getModelTint(model: BlockModel, spriteName: String) =
        model.elements.findFirst { element ->
            element.faces.entries.firstOrNull { (_, face) ->
                face.texture == "#$spriteName"
            }?.value?.tintIndex ?: -1
        }?.let { MatchValue.Found("tint index \"$it\" for sprite \"${spriteName}\"", it.toString()) }
        ?: MatchValue.Found("tint index \"-1\" for unused sprite \"${spriteName}\"", "-1")
}