package mods.betterfoliage.resource.discovery

import com.google.common.base.Joiner
import mods.betterfoliage.util.YarnHelper
import mods.betterfoliage.util.get
import mods.betterfoliage.util.stripStart
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.DEBUG
import org.apache.logging.log4j.Level.INFO
import java.util.function.Consumer

// net.minecraft.client.render.model.json.JsonUnbakedModel.parent
val JsonUnbakedModel_parent = YarnHelper.requiredField<JsonUnbakedModel>("net.minecraft.class_793", "field_4253", "Lnet/minecraft/class_793;")
// net.minecraft.client.render.model.json.JsonUnbakedModel.parentId
val JsonUnbakedModel_parentId = YarnHelper.requiredField<Identifier>("net.minecraft.class_793", "field_4247", "Lnet/minecraft/class_2960;")

fun Pair<JsonUnbakedModel, Identifier>.derivesFrom(targetLocation: Identifier): Boolean {
    if (second.stripStart("models/") == targetLocation) return true
    if (first[JsonUnbakedModel_parent] != null && first[JsonUnbakedModel_parentId] != null)
        return Pair(first[JsonUnbakedModel_parent]!!, first[JsonUnbakedModel_parentId]!!).derivesFrom(targetLocation)
    return false
}

abstract class ConfigurableModelDiscovery : ModelDiscoveryBase() {

    abstract val matchClasses: IBlockMatcher
    abstract val modelTextures: List<ModelTextureList>

    override fun discover(loader: ModelLoader, atlas: Consumer<Identifier>): Map<BlockState, BlockRenderKey> {
        log(INFO, "Starting model discovery: ${this::class.java.canonicalName}")
        matchClasses.describe(this)
        modelTextures.forEach { modelTex ->
            log(DEBUG, "    model: ${modelTex.modelLocation} textures: ${modelTex.textureNames.joinToString(", ")}")
        }
        return super.discover(loader, atlas)
    }

    abstract fun processModel(state: BlockState, textures: List<String>, atlas: Consumer<Identifier>): BlockRenderKey?

    override fun processModel(ctx: ModelDiscoveryContext, atlas: Consumer<Identifier>): BlockRenderKey? {
        val matchClass = matchClasses.matchingClass(ctx.state.block) ?: return null
        log(DEBUG, "block state ${ctx.state.toString()}")
        log(DEBUG, "      class ${ctx.state.block.javaClass.name} matches ${matchClass.name}")

        (ctx.models.filter { it.first is JsonUnbakedModel } as List<Pair<JsonUnbakedModel, Identifier>>).forEach { (model, location) ->
            val modelMatch = modelTextures.firstOrNull { (model to location).derivesFrom(it.modelLocation) }
            if (modelMatch != null) {
                log(DEBUG, "      model ${model} matches ${modelMatch.modelLocation}")

                val textures = modelMatch.textureNames.map { it to model.resolveTexture(it) }
                val texMapString = Joiner.on(", ").join(textures.map { "${it.first}=${it.second}" })
                log(DEBUG, "    sprites [$texMapString]")

                if (textures.all { it.second != "missingno" }) {
                    // found a valid model (all required textures exist)
                    return processModel(ctx.state, textures.map { it.second }, atlas).also {
                        log(DEBUG, "    valid model discovered: $it")
                    }
                }
            }
        }
        return null
    }
}