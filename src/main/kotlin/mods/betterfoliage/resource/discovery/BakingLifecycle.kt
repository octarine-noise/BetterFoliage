package mods.betterfoliage.resource.discovery

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BlockModelsReloadCallback
import mods.betterfoliage.ModelLoadingCallback
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.Invalidator
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.block.BlockState
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Level.WARN
import org.apache.logging.log4j.Logger
import java.lang.ref.WeakReference
import java.util.function.Function

data class ModelDiscoveryContext(
    val bakery: ModelLoader,
    val blockState: BlockState,
    val modelLocation: Identifier,
    val sprites: MutableSet<Identifier>,
    val replacements: MutableMap<Identifier, ModelBakingKey>,
    val logger: Logger
) {
    fun getUnbaked(location: Identifier = modelLocation) = bakery.getOrLoadModel(location)
    fun addReplacement(key: ModelBakingKey, addToStateKeys: Boolean = true) {
        replacements[modelLocation] = key
        if (addToStateKeys) BetterFoliage.blockTypes.stateKeys[blockState] = key
        logger.log(INFO, "Adding model replacement $modelLocation -> $key")
    }
}

interface ModelDiscovery {
    fun onModelsLoaded(
        bakery: ModelLoader,
        sprites: MutableSet<Identifier>,
        replacements: MutableMap<Identifier, ModelBakingKey>
    )
}

data class ModelBakingContext(
    val bakery: ModelLoader,
    val spriteGetter: Function<SpriteIdentifier, Sprite>,
    val location: Identifier,
    val transform: ModelBakeSettings,
    val logger: Logger
) {
    fun getUnbaked() = bakery.getOrLoadModel(location)
    fun getBaked() = bakery.bake(location, transform)
}

interface ModelBakingKey {
    fun bake(ctx: ModelBakingContext): BakedModel? =
        ctx.getUnbaked().bake(ctx.bakery, ctx.spriteGetter, ctx.transform, ctx.location)
}

object BakeWrapperManager : HasLogger(), Invalidator, ModelLoadingCallback, ClientSpriteRegistryCallback, BlockModelsReloadCallback {
    init {
        ModelLoadingCallback.EVENT.register(this)
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register(this)
    }
    val discoverers = mutableListOf<ModelDiscovery>()
    override val callbacks = mutableListOf<WeakReference<()->Unit>>()

    private val replacements = mutableMapOf<Identifier, ModelBakingKey>()
    private val sprites = mutableSetOf<Identifier>()

    override fun beginLoadModels(loader: ModelLoader, manager: ResourceManager) {
        val startTime = System.currentTimeMillis()
        replacements.clear()
        sprites.clear()
        invalidate()
        BetterFoliage.blockTypes = BlockTypeCache()

        logger.log(INFO, "starting model discovery (${discoverers.size} listeners)")
        discoverers.forEach { listener ->
            val replacementsLocal = mutableMapOf<Identifier, ModelBakingKey>()
            listener.onModelsLoaded(loader, sprites, replacements)
        }

        val elapsed = System.currentTimeMillis() - startTime
        logger.log(INFO, "finished model discovery in $elapsed ms, ${replacements.size} top-level replacements")
    }

    override fun registerSprites(atlas: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry) {
        logger.log(INFO, "Adding ${sprites.size} sprites to block atlas")
        sprites.forEach { registry.register(it) }
        sprites.clear()
    }

    override fun reloadBlockModels(blockModels: BlockModels) {
        replacements.clear()
    }

    fun onBake(
        unbaked: UnbakedModel,
        bakery: ModelLoader,
        spriteGetter: Function<SpriteIdentifier, Sprite>,
        transform: ModelBakeSettings,
        location: Identifier
    ): BakedModel? {
        val ctx = ModelBakingContext(bakery, spriteGetter, location, transform, detailLogger)
        // bake replacement if available
        replacements[location]?.let { replacement ->
            detailLogger.log(INFO, "Baking replacement for [${unbaked::class.java.simpleName}] $location -> $replacement")
            try {
                return replacement.bake(ctx)
            } catch (e: Exception) {
                detailLogger.log(WARN, "Error while baking $replacement", e)
                logger.log(WARN, "Error while baking $replacement", e)
            }
        }
        return unbaked.bake(bakery, spriteGetter, transform, location)
    }
}
