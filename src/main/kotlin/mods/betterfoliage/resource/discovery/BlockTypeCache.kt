package mods.betterfoliage.resource.discovery

import mods.betterfoliage.Client
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.ResourceLocation

class BlockTypeCache {
    val leaf = mutableSetOf<BlockState>()
    val grass = mutableSetOf<BlockState>()
    val dirt = mutableSetOf<BlockState>()

    companion object : ModelDiscovery {
        override fun onModelsLoaded(bakery: ModelBakery, sprites: MutableSet<ResourceLocation>, replacements: MutableMap<ResourceLocation, ModelBakeKey>
        ) {
            Client.blockTypes = BlockTypeCache()
        }
    }
}