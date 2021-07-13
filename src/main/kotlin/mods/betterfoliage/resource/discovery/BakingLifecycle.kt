package mods.betterfoliage.resource.discovery

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.Invalidator
import mods.betterfoliage.util.resourceManager
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IModelTransform
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.RenderMaterial
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.loading.progress.StartupMessageManager
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Level.WARN
import org.apache.logging.log4j.Logger
import java.lang.ref.WeakReference
import java.util.function.Function

data class ModelDefinitionsLoadedEvent(
    val bakery: ModelBakery
) : Event()

interface ModelBakingKey {
    fun bake(ctx: ModelBakingContext): IBakedModel? =
        ctx.getUnbaked().bake(ctx.bakery, ctx.spriteGetter, ctx.transform, ctx.location)
}

interface ModelDiscovery {
    fun onModelsLoaded(
        bakery: ModelBakery,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    )
}

data class ModelDiscoveryContext(
    val bakery: ModelBakery,
    val blockState: BlockState,
    val modelLocation: ResourceLocation,
    val sprites: MutableSet<ResourceLocation>,
    val replacements: MutableMap<ResourceLocation, ModelBakingKey>,
    val logger: Logger
) {
    fun getUnbaked(location: ResourceLocation = modelLocation) = bakery.getModel(location)
    fun addReplacement(key: ModelBakingKey, addToStateKeys: Boolean = true) {
        replacements[modelLocation] = key
        if (addToStateKeys) BetterFoliage.blockTypes.stateKeys[blockState] = key
        logger.log(INFO, "Adding model replacement $modelLocation -> $key")
    }
    fun <T: IUnbakedModel> loadHierarchy(model: T) = model.apply {
        getMaterials(this@ModelDiscoveryContext::getUnbaked, mutableSetOf())
    }
}

data class ModelBakingContext(
    val bakery: ModelBakery,
    val spriteGetter: Function<RenderMaterial, TextureAtlasSprite>,
    val location: ResourceLocation,
    val transform: IModelTransform,
    val logger: Logger
) {
    fun getUnbaked() = bakery.getModel(location)
    fun getBaked() = bakery.getBakedModel(location, transform, spriteGetter)
}

class BakeWrapperManager : Invalidator, HasLogger() {
    val discoverers = mutableListOf<ModelDiscovery>()
    override val callbacks = mutableListOf<WeakReference<()->Unit>>()

    private val replacements = mutableMapOf<ResourceLocation, ModelBakingKey>()
    private val sprites = mutableSetOf<ResourceLocation>()

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun handleModelLoad(event: ModelDefinitionsLoadedEvent) {
        val startTime = System.currentTimeMillis()
        invalidate()
        BetterFoliage.blockConfig.readConfig(resourceManager)
        BetterFoliage.blockTypes = BlockTypeCache()

        StartupMessageManager.addModMessage("BetterFoliage: discovering models")
        logger.log(INFO, "starting model discovery (${discoverers.size} listeners)")
        discoverers.forEach { listener ->
            val replacementsLocal = mutableMapOf<ResourceLocation, ModelBakingKey>()
            listener.onModelsLoaded(event.bakery, sprites, replacements)
        }

        val elapsed = System.currentTimeMillis() - startTime
        logger.log(INFO, "finished model discovery in $elapsed ms, ${replacements.size} top-level replacements")
    }

    @SubscribeEvent
    fun handleStitch(event: TextureStitchEvent.Pre) {
        if (event.map.location() == Atlas.BLOCKS.resourceId) {
            logger.log(INFO, "Adding ${sprites.size} sprites to block atlas")
            sprites.forEach { event.addSprite(it) }
            sprites.clear()
        }
    }

    @SubscribeEvent
    fun handleModelBake(event: ModelBakeEvent) {
        replacements.clear()
    }

    fun onBake(
        unbaked: IUnbakedModel,
        bakery: ModelBakery,
        spriteGetter: Function<RenderMaterial, TextureAtlasSprite>,
        transform: IModelTransform,
        location: ResourceLocation
    ): IBakedModel? {
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
