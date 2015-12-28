@file:JvmName("Hooks")
@file:SideOnly(Side.CLIENT)
package mods.betterfoliage.client

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.EntityFallingLeavesFX
import mods.betterfoliage.client.render.EntityRisingSoulFX
import mods.octarinecore.client.render.blockContext
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

fun getRenderTypeOverride(blockAccess: IBlockAccess, x: Int, y: Int, z: Int, block: Block, original: Int): Int {
    if (!Config.enabled) return original;

    // universal sign for DON'T RENDER ME!
    if (original == -1) return original;

    return blockContext.let { ctx ->
        ctx.set(blockAccess, x, y, z)
        Client.renderers.find { it.isEligible(ctx) }?.renderId ?: original
    }
}

fun shouldRenderBlockSideOverride(original: Boolean, blockAccess: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean {
    return original || (Config.enabled && Config.roundLogs.enabled && Config.blocks.logs.matchesID(blockAccess.getBlock(x, y, z)));
}

fun getAmbientOcclusionLightValueOverride(original: Float, block: Block): Float {
    if (Config.enabled && Config.roundLogs.enabled && Config.blocks.logs.matchesID(block)) return Config.roundLogs.dimming;
    return original;
}

fun getUseNeighborBrightnessOverride(original: Boolean, block: Block): Boolean {
    return original || (Config.enabled && Config.roundLogs.enabled && Config.blocks.logs.matchesID(block));
}

fun onRandomDisplayTick(block: Block, world: World, x: Int, y: Int, z: Int) {
    if (Config.enabled &&
        Config.risingSoul.enabled &&
        block == Blocks.soul_sand &&
        world.isAirBlock(x, y + 1, z) &&
        Math.random() < Config.risingSoul.chance) {
            EntityRisingSoulFX(world, x, y, z).addIfValid()
    }

    if (Config.enabled &&
        Config.fallingLeaves.enabled &&
        Config.blocks.leaves.matchesID(block) &&
        world.isAirBlock(x, y - 1, z) &&
        Math.random() < Config.fallingLeaves.chance) {
            EntityFallingLeavesFX(world, x, y, z).addIfValid()
    }
}