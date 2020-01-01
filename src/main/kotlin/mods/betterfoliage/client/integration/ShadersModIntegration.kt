package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.loader.Refs
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.BufferBuilder
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for ShadersMod.
 */
object ShadersModIntegration {

    @JvmStatic var isAvailable = allAvailable(Refs.sVertexBuilder, Refs.pushEntity_state, Refs.pushEntity_num, Refs.popEntity)
    @JvmStatic val tallGrassEntityData = entityDataFor(Blocks.TALL_GRASS.defaultState)
    @JvmStatic val leavesEntityData = entityDataFor(Blocks.OAK_LEAVES.defaultState)

    fun entityDataFor(blockState: BlockState) = 0L
//        (ForgeRegistries.BLOCKS.getIDForObject(blockState.block).toLong() and 65535) or
//        ((blockState.renderType.ordinal.toLong() and 65535) shl 16) or
//        (blockState.block.getMetaFromState(blockState).toLong() shl 32)


    /**
     * Called from transformed ShadersMod code.
     * @see mods.betterfoliage.loader.BetterFoliageTransformer
     */
    @JvmStatic fun getBlockIdOverride(original: Long, blockState: BlockState): Long {
        if (BlockConfig.leafBlocks.matchesClass(blockState.block)) return leavesEntityData
        if (BlockConfig.crops.matchesClass(blockState.block)) return tallGrassEntityData
        return original
    }

    init {
        Client.log(INFO, "ShadersMod integration is ${if (isAvailable) "enabled" else "disabled" }")
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(blockEntityData: Long, renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) {
        if ((isAvailable && enabled)) {
            val vertexBuilder = Refs.sVertexBuilder.get(renderer)!!
            Refs.pushEntity_num.invoke(vertexBuilder, blockEntityData)
            func()
            Refs.popEntity.invoke(vertexBuilder)
        } else {
            func()
        }
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(state: BlockState, renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(entityDataFor(state), renderer, enabled, func)

    /** Quads rendered inside this block will behave as tallgrass blocks in shader programs. */
    inline fun grass(renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(tallGrassEntityData, renderer, enabled, func)

    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(leavesEntityData, renderer, enabled, func)
}
