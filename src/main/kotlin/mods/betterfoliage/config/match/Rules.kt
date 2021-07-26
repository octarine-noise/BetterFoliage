package mods.betterfoliage.config.match

import mods.betterfoliage.config.match.MatchMethod.CONTAINS
import mods.betterfoliage.config.match.MatchMethod.EXACT_MATCH
import mods.betterfoliage.config.match.MatchMethod.EXTENDS
import mods.betterfoliage.resource.discovery.RuleProcessingContext
import mods.betterfoliage.util.findFirst
import mods.betterfoliage.util.quoted
import mods.betterfoliage.util.tryDefault
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

typealias PartialLocation = Pair<String?, String>

object MatchRules {
    fun visitRoot(ctx: RuleProcessingContext, node: Node.MatchAll): MListAll {
        val results = mutableListOf<MAnything<Boolean>>()
        for (rule in node.list) {
            val result = mNode(ctx, rule)
            results.add(result)
            if (!result.value) break
        }
        return MListAll(results)
    }

    fun mNode(ctx: RuleProcessingContext, node: Node): MAnything<Boolean> = when(node) {
        is Node.MatchValueList -> mMatchList(ctx, node)
        is Node.MatchParam -> mParam(ctx, node)
        is Node.SetParam -> mParamSet(ctx, node)
        is Node.Negate -> mNegate(ctx, node)
        else -> node.error("match type not implemented: ${node::class.java.name.quoted}").left
    }

    fun mNegate(ctx: RuleProcessingContext, node: Node.Negate) = MNegated(mNode(ctx, node.node))

    fun mMatchList(ctx: RuleProcessingContext, node: Node.MatchValueList) = node.values.map { value ->
        when (node.matchSource) {
            Node.MatchSource.BLOCK_CLASS -> mBlockClass(ctx, node, value)
            Node.MatchSource.BLOCK_NAME -> mBlockName(ctx, node, value)
            Node.MatchSource.MODEL_LOCATION -> mModel(ctx, node, value)
        }
    }.let { MListAny(it) }

    fun mBlockClass(ctx: RuleProcessingContext, node: Node.MatchValueList, value: Node.Value): MValue<Boolean> {
        val blockClass = ctx.discovery.blockState.block::class.java.let {
            MValue.right(it, "block class ${it.name.quoted}", node.configSource)
        }
        val target = when(value) {
            is Node.Value.Literal -> value.asEither.mapNotNull(
                func = { tryDefault(null) { Class.forName(it) }},
                dLeft = { "missing class ${it.value}" },
                dRight = { m, _ -> " class ${m.value}" }
            )
            is Node.Value.ClassOf -> value.asEither.mapValue(::ResourceLocation).mapNotNull(
                func = { loc -> ForgeRegistries.BLOCKS.getValue(loc)?.let { it::class.java } },
                dLeft = { "missing block ${it.value.toString().quoted}" },
                dRight = { m, r -> "class ${r.name.quoted} of block ${m.value}" }
            )
            else -> value.invalidTypeFor("block class")
        }
        return when(node.matchMethod) {
            EXACT_MATCH -> MComparison.equals.compare(blockClass, target)
            EXTENDS -> classExtends.compare(blockClass, target)
            CONTAINS -> node.error("invalid match type for block class: contains").left
        }
    }

    fun mBlockName(ctx: RuleProcessingContext, node: Node.MatchValueList, value: Node.Value): MValue<Boolean> {
        val blockName = MValue.right(ctx.discovery.blockState.block, "", node.configSource).mapNotNull(
            func = { it.registryName }, dLeft = { "missing block name" }, dRight = { _, r -> "block name ${r.toString().quoted}" }
        )
        val target = when(value) {
            is Node.Value.Literal -> value.asEither.map(::splitLocation, ::quoteString)
            else -> value.invalidTypeFor("block name")
        }
        return when(node.matchMethod) {
            EXACT_MATCH -> blockNameExact.compare(blockName, target)
            CONTAINS -> blockNameContains.compare(blockName, target)
            EXTENDS -> node.error("invalid match type for block name: extends").left
        }
    }

    fun mModel(ctx: RuleProcessingContext, node: Node.MatchValueList, value: Node.Value): MValue<Boolean> {
        val model = (ctx.discovery.getUnbaked() as? BlockModel)?.let {
            MValue.right(it, "model ${it.name.quoted}", node.configSource)
        } ?: node.error("unsupported model type: ${ctx.discovery.getUnbaked()::class.java.name.quoted}")

        val target = when(value) {
            is Node.Value.Literal -> value.asEither.map(::splitLocation, ::quoteString)
            else -> value.invalidTypeFor("model")
        }
        val models = when(node.matchMethod) {
            EXTENDS -> model.mapValue { ctx.discovery.loadHierarchy(it).ancestors() }
            else -> model.mapValue { listOf(it) }
        }
        return when(node.matchMethod) {
            EXACT_MATCH, EXTENDS -> anyModel(node.matchMethod, ::locationMatches)
            CONTAINS -> anyModel(CONTAINS, ::locationContains)
        }.compare(models, target)
    }

    fun mParam(ctx: RuleProcessingContext, node: Node.MatchParam) = node.values.map { value ->
        val paramValue = ctx.params[node.name] ?.let {
            MValue.right(it, "parameter ${node.name.quoted}", node.configSource, immutable = false)
        } ?: node.error("missing parameter ${node.name.quoted}")

        val target = when(value) {
            is Node.Value.Literal -> value.asEither.mapDescription { it.description.quoted }
            else -> value.invalidTypeFor("parameter")
        }
        MComparison.equals.compare(paramValue, target)
    }.let { MListAny(it) }

    fun mParamSet(ctx: RuleProcessingContext, node: Node.SetParam): MValue<Boolean> {
        val target = when(node.value) {
            is Node.Value.Literal -> node.value.asEither
            is Node.Value.Texture -> when(val model = ctx.discovery.getUnbaked()) {
                is BlockModel -> node.value.asEither.map(
                    func = { model.getMaterial(it).texture().toString() },
                    description = { m, r -> "texture \"${m.value}\" = \"$r\" of model ${model.name}"}
                )
                else -> node.error("unsupported model type: ${model::class.java.name.quoted}")
            }
            is Node.Value.Tint -> when(val model = ctx.discovery.getUnbaked()) {
                is BlockModel -> node.value.asEither.mapNotNull(
                    func = { model.tintOf(it)?.toString() },
                    dRight = { m, r -> "tint index $r for sprite ${m.value}" },
                    dLeft = { m -> "tint index -1 for unused sprite ${m.value}"}
                ).toRight("-1")

                else -> node.error("unsupported model type: ${model::class.java.name.quoted}")
            }
            else -> node.value.invalidTypeFor("prameter")
        }
        target.ifRight { ctx.params[node.name] = it.value }
        return target.mapDescription { m -> "parameter ${node.name} set to ${m.value}" }.mapValue { true }.flatten()
    }

    private val classExtends = MComparison.of<Class<*>, Class<*>>(EXTENDS) { c1, c2 -> c2.isAssignableFrom(c1) }

    private val blockNameExact = MComparison.of<ResourceLocation, PartialLocation>(EXACT_MATCH) { block, partial ->
        locationMatches(block, partial)
    }
    private val blockNameContains = MComparison.of<ResourceLocation, Pair<String?, String>>(CONTAINS) { block, partial ->
            locationContains(block, partial)
    }
    private fun anyModel(matchMethod: MatchMethod, func: (ResourceLocation, PartialLocation)->Boolean) =
        MComparison.of<List<BlockModel>, PartialLocation>(matchMethod) { models, partial ->
            models.any { func(ResourceLocation(it.name), partial) }
        }


    fun locationMatches(loc: ResourceLocation, partial: PartialLocation) =
        (partial.first == null || loc.namespace == partial.first) && loc.path == partial.second
    fun locationContains(loc: ResourceLocation, partial: PartialLocation) =
        (partial.first == null || loc.namespace.contains(partial.first!!)) && loc.path.contains(partial.second)

    fun splitLocation(str: String): PartialLocation =
        if (str.contains(":")) ResourceLocation(str).let { it.namespace to it.path } else null to str

    fun <T, R> quoteString(mValue: MValue<T>, newValue: R) = mValue.description.quoted

    fun BlockModel.ancestors(): List<BlockModel> = if (parent == null) listOf(this) else parent!!.ancestors() + this

    fun BlockModel.tintOf(spriteName: String) =
        elements.findFirst { element ->
            element.faces.entries.findFirst { (_, face) ->
                if (face.texture == "#$spriteName") face.tintIndex else null
            }
        }
}
