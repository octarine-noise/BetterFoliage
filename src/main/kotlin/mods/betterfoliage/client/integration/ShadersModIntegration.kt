package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.loader.Refs
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.Block
import net.minecraft.block.BlockTallGrass
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.init.Blocks
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for ShadersMod.
 */
@SideOnly(Side.CLIENT)
object ShadersModIntegration {

    @JvmStatic var isPresent = false
    @JvmStatic val tallGrassEntityData = entityDataFor(Blocks.tallgrass.defaultState.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS))
    @JvmStatic val leavesEntityData = entityDataFor(Blocks.leaves.defaultState)

    fun entityDataFor(blockState: IBlockState) =
        (Block.blockRegistry.getIDForObject(blockState.block).toLong() and 65535) or
        ((blockState.renderType.ordinal.toLong() and 65535) shl 16) or
        (blockState.block.getMetaFromState(blockState).toLong() shl 32)


    /**
     * Called from transformed ShadersMod code.
     * @see mods.betterfoliage.loader.BetterFoliageTransformer
     */
        @JvmStatic fun getBlockIdOverride(original: Long, blockState: IBlockState): Long {
        if (Config.blocks.leaves.matchesID(blockState.block)) return leavesEntityData
        if (Config.blocks.crops.matchesID(blockState.block)) return tallGrassEntityData
        return original
    }

    init {
        if (allAvailable(Refs.sVertexBuilder, Refs.pushEntity_state, Refs.pushEntity_num, Refs.popEntity)) {
            Client.log(INFO, "ShadersMod integration enabled")
            isPresent = true
        }
    }

    /** Quads rendered inside this block will behave as tallgrass blocks in shader programs. */
    inline fun grass(renderer: VertexBuffer, enabled: Boolean = true, func: ()->Unit) {
        if ((isPresent && enabled)) {
            val vertexBuilder = Refs.sVertexBuilder.get(renderer)!!
            Refs.pushEntity_num.invoke(vertexBuilder, tallGrassEntityData)
            func()
            Refs.popEntity.invoke(vertexBuilder)
        } else {
            func()
        }
    }

    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(renderer: VertexBuffer, enabled: Boolean = true, func: ()->Unit) {
        if ((isPresent && enabled)) {
            val vertexBuilder = Refs.sVertexBuilder.get(renderer)!!
            Refs.pushEntity_num.invoke(vertexBuilder, leavesEntityData.toLong())
            func()
            Refs.popEntity.invoke(vertexBuilder)
        } else {
            func()
        }
    }
}
