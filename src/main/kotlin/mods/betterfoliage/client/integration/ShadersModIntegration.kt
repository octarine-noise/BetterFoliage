package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.loader.Refs
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockRenderType.MODEL
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.TallGrassBlock
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for ShadersMod.
 */
object ShadersModIntegration {

    @JvmStatic val isAvailable = allAvailable(Refs.sVertexBuilder, Refs.pushEntity_state, Refs.pushEntity_num, Refs.popEntity)

    val grassDefaultBlockId = 31L
    val leavesDefaultBlockId = 18L

    /**
     * Called from transformed ShadersMod code.
     * @see mods.betterfoliage.loader.BetterFoliageTransformer
     */
    @JvmStatic fun getBlockIdOverride(original: Long, blockState: BlockState): Long {
        if (BlockConfig.leafBlocks.matchesClass(blockState.block)) return Config.shaders.leavesId
        if (BlockConfig.crops.matchesClass(blockState.block)) return Config.shaders.grassId
        return original
    }

    init {
        Client.log(INFO, "ShadersMod integration is ${if (isAvailable) "enabled" else "disabled" }")
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(blockId: Long, renderType: BlockRenderType, renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) {
        val blockData = blockId or (renderType.ordinal shl 16).toLong()
        if ((isAvailable && enabled)) {
            val vertexBuilder = Refs.sVertexBuilder.get(renderer)!!
            Refs.pushEntity_num.invoke(vertexBuilder, blockId)
            func()
            Refs.popEntity.invoke(vertexBuilder)
        } else {
            func()
        }
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    // temporarily NO-OP
    inline fun renderAs(state: BlockState, renderType: BlockRenderType, renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) = func()

    /** Quads rendered inside this block will behave as tallgrass blocks in shader programs. */
    inline fun grass(renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(Config.shaders.grassId, MODEL, renderer, enabled, func)

    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(Config.shaders.leavesId, MODEL, renderer, enabled, func)
}
