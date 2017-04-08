package mods.betterfoliage.client.integration

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.loader.Refs
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import org.apache.logging.log4j.Level.*

/**
 * Integration for ShadersMod.
 */
@SideOnly(Side.CLIENT)
object ShadersModIntegration {

    @JvmStatic var isPresent = false
    @JvmStatic val tallGrassEntityData = entityDataFor(Blocks.tallgrass)
    @JvmStatic val leavesEntityData = entityDataFor(Blocks.leaves)

    fun entityDataFor(block: Block) =
        (Block.blockRegistry.getIDForObject(block) and 65535) or
        ((block.renderType and 65535) shl 16)

    /**
     * Called from transformed ShadersMod code.
     * @see mods.betterfoliage.loader.BetterFoliageTransformer
     */
    @JvmStatic fun getBlockIdOverride(original: Int, block: Block): Int {
        if (Config.blocks.leaves.matchesID(original and 65535)) return leavesEntityData
        if (Config.blocks.crops.matchesID(original and 65535)) return tallGrassEntityData
        return original
    }

    init {
        if (allAvailable(Refs.pushEntity_I, Refs.popEntity)) {
            Client.log(INFO, "ShadersMod integration enabled")
            isPresent = true
        }
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(blockEntityData: Int, enabled: Boolean = true, func: ()->Unit) {
        if (isPresent && enabled) Refs.pushEntity_I.invokeStatic(blockEntityData)
        func()
        if (isPresent && enabled) Refs.popEntity.invokeStatic()
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(block: Block, enabled: Boolean = true, func: ()->Unit) =
        renderAs(entityDataFor(block), enabled, func)

    /** Quads rendered inside this block will behave as tallgrass blocks in shader programs. */
    inline fun grass(enabled: Boolean = true, func: ()->Unit) = renderAs(tallGrassEntityData, enabled, func)

    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(enabled: Boolean = true, func: ()->Unit) = renderAs(leavesEntityData, enabled, func)
}
