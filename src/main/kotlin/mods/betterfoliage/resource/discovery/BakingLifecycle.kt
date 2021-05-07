package mods.betterfoliage.resource.discovery

import mods.betterfoliage.ModelDefinitionsLoadedEvent
import mods.betterfoliage.model.SpecialRenderVariantList
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.Invalidator
import mods.betterfoliage.util.SimpleInvalidator
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IModelTransform
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.Material
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.loading.progress.StartupMessageManager
import org.apache.logging.log4j.Level.INFO
import java.lang.ref.WeakReference
import java.util.function.Function

interface ModelDiscovery {
    fun onModelsLoaded(
        bakery: ModelBakery,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    )
}

interface ModelBakingKey {
    fun bake(
        location: ResourceLocation,
        unbaked: IUnbakedModel,
        transform: IModelTransform,
        bakery: ModelBakery,
        spriteGetter: Function<Material, TextureAtlasSprite>
    ): IBakedModel? = unbaked.bakeModel(bakery, spriteGetter, transform, location)
}

object BakeWrapperManager : Invalidator, HasLogger() {
    val discoverers = mutableListOf<ModelDiscovery>()
    override val callbacks = mutableListOf<WeakReference<()->Unit>>()

    val modelsValid = SimpleInvalidator()
    val spritesValid = SimpleInvalidator()

    private val replacements = mutableMapOf<ResourceLocation, ModelBakingKey>()
    private val sprites = mutableSetOf<ResourceLocation>()

    @SubscribeEvent
    fun handleModelLoad(event: ModelDefinitionsLoadedEvent) {
        modelsValid.invalidate()
        StartupMessageManager.addModMessage("BetterFoliage: discovering models")
        logger.log(INFO, "starting model discovery (${discoverers.size} listeners)")
        discoverers.forEach { listener ->
            val replacementsLocal = mutableMapOf<ResourceLocation, ModelBakingKey>()
            listener.onModelsLoaded(event.bakery, sprites, replacementsLocal)
            replacements.putAll(replacementsLocal)
        }
    }

    @SubscribeEvent
    fun handleStitch(event: TextureStitchEvent.Pre) {
        if (event.map.textureLocation == Atlas.BLOCKS.resourceId) {
            logger.log(INFO, "Adding ${sprites.size} sprites to block atlas")
            spritesValid.invalidate()
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
        spriteGetter: Function<Material, TextureAtlasSprite>,
        transform: IModelTransform,
        location: ResourceLocation
    ): IBakedModel? {
        // bake replacement if available
        replacements[location]?.let { replacement ->
            detailLogger.log(INFO, "Baking replacement for [${unbaked::class.java.simpleName}] $location -> $replacement")
            return replacement.bake(location, unbaked, transform, bakery, spriteGetter)
        }
        // container model support
        if (unbaked is VariantList) SpecialRenderVariantList.bakeIfSpecial(location, unbaked, bakery, spriteGetter)?.let {
            detailLogger.log(INFO, "Wrapping container [${unbaked::class.java.simpleName}] $location")
            return it
        }

        return unbaked.bakeModel(bakery, spriteGetter, transform, location)
    }
}
