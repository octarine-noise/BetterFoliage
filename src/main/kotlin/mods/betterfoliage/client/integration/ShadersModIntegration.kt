package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.loader.Refs
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.Block
import net.minecraft.block.BlockTallGrass
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.init.Blocks
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumBlockRenderType.MODEL
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for ShadersMod.
 */
@SideOnly(Side.CLIENT)
object ShadersModIntegration {

    @JvmStatic val isAvailable = allAvailable(Refs.sVertexBuilder, Refs.pushEntity_state, Refs.pushEntity_num, Refs.popEntity)

    val grassDefaultBlockId = blockIdFor(Blocks.TALLGRASS.defaultState.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS))
    val leavesDefaultBlockId = blockIdFor(Blocks.LEAVES.defaultState)
    fun blockIdFor(blockState: IBlockState) = Block.REGISTRY.getIDForObject(blockState.block).toLong() and 65535

//    fun entityDataFor(blockState: IBlockState) =
//        (Block.REGISTRY.getIDForObject(blockState.block).toLong() and 65535) //or
//        ((blockState.renderType.ordinal.toLong() and 65535) shl 16) or
//        (blockState.block.getMetaFromState(blockState).toLong() shl 32)

    fun logEntityData(name: String, blockState: IBlockState) {
        val blockId = Block.REGISTRY.getIDForObject(blockState.block).toLong() and 65535
        val meta = blockState.renderType.ordinal.toLong() and 65535
        val renderType = blockState.renderType.ordinal.toLong() and 65535
        Client.log(INFO, "ShadersMod integration for $name")
        Client.log(INFO, "     blockState=$blockState")
        Client.log(INFO, "     blockId=$blockId, meta=$meta, type=$renderType")
    }
    /**
     * Called from transformed ShadersMod code.
     * @see mods.betterfoliage.loader.BetterFoliageTransformer
     */
    @JvmStatic fun getBlockIdOverride(original: Long, blockState: IBlockState): Long {
        if (Config.blocks.leavesClasses.matchesClass(blockState.block)) return Config.shaders.leavesId
        if (Config.blocks.crops.matchesClass(blockState.block)) return Config.shaders.grassId
        return original
    }

    init {
        Client.log(INFO, "ShadersMod integration is ${if (isAvailable) "enabled" else "disabled" }")
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(blockId: Long, renderType: EnumBlockRenderType, renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) {
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
    inline fun renderAs(state: IBlockState, renderType: EnumBlockRenderType, renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(blockIdFor(state), renderType, renderer, enabled, func)

    /** Quads rendered inside this block will behave as tallgrass blocks in shader programs. */
    inline fun grass(renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(Config.shaders.grassId, MODEL, renderer, enabled, func)

    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(renderer: BufferBuilder, enabled: Boolean = true, func: ()->Unit) =
        renderAs(Config.shaders.leavesId, MODEL, renderer, enabled, func)
}
