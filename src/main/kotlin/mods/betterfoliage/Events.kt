package mods.betterfoliage

import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.eventbus.api.Event

data class ModelDefinitionsLoadedEvent(
    val bakery: ModelBakery
) : Event()