package mods.betterfoliage.resource.discovery

import net.minecraft.block.BlockState

class BlockTypeCache {
    val leaf = mutableSetOf<BlockState>()
    val grass = mutableSetOf<BlockState>()
    val dirt = mutableSetOf<BlockState>()

    val stateKeys = mutableMapOf<BlockState, ModelBakingKey>()

    inline fun <reified T> getTypedOrNull(state: BlockState) = stateKeys[state] as? T
    inline fun <reified T> hasTyped(state: BlockState) = stateKeys[state] is T
}