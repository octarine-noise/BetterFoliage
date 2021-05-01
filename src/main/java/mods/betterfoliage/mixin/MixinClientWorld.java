package mods.betterfoliage.mixin;

import mods.betterfoliage.Hooks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    private static final String worldAnimateTick = "Lnet/minecraft/client/world/ClientWorld;animateTick(IIIILjava/util/Random;ZLnet/minecraft/util/math/BlockPos$Mutable;)V";
    private static final String blockAnimateTick = "Lnet/minecraft/block/Block;animateTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V";

    private static final String worldNotify = "Lnet/minecraft/client/world/ClientWorld;notifyBlockUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V";
    private static final String rendererNotify = "Lnet/minecraft/client/renderer/WorldRenderer;notifyBlockUpdate(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V";

    /**
     * Inject a callback to call for every random display tick. Used for adding custom particle effects to blocks.
     */
    @Redirect(method = worldAnimateTick, at = @At(value = "INVOKE", target = blockAnimateTick))
    void onAnimateTick(Block block, BlockState state, World world, BlockPos pos, Random random) {
        Hooks.onRandomDisplayTick(block, state, world, pos, random);
        block.animateTick(state, world, pos, random);
    }

    /**
     * Inject callback to get notified of client-side blockstate changes.
     * Used to invalidate caches in the {@link mods.betterfoliage.chunk.ChunkOverlayManager}
     */
    @Redirect(method = worldNotify, at = @At(value = "INVOKE", target = rendererNotify))
    void onClientBlockChanged(WorldRenderer renderer, IBlockReader world, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        Hooks.onClientBlockChanged((ClientWorld) world, pos, oldState, newState, flags);
        renderer.notifyBlockUpdate(world, pos, oldState, newState, flags);
    }
}
