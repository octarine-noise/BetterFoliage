package mods.betterfoliage.mixin;

import mods.betterfoliage.ClientWorldLoadCallback;
import mods.betterfoliage.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    private static final String ctor = "<init>(Lnet/minecraft/client/network/ClientPlayNetworkHandler;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/dimension/DimensionType;ILnet/minecraft/util/profiler/Profiler;Lnet/minecraft/client/render/WorldRenderer;)V";
    private static final String checkBlockRender = "Lnet/minecraft/client/world/ClientWorld;checkBlockRerender(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V";
    private static final String rendererNotify = "Lnet/minecraft/client/render/WorldRenderer;method_21596(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V";
    private static final String worldDisplayTick = "randomBlockDisplayTick(IIIILjava/util/Random;ZLnet/minecraft/util/math/BlockPos$Mutable;)V";
    private static final String blockDisplayTick = "Lnet/minecraft/block/Block;randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V";

    /**
     * Inject callback to get notified of client-side blockstate changes.
     * Used to invalidate caches in the {@link mods.betterfoliage.chunk.ChunkOverlayManager}
     */
    @Inject(method = checkBlockRender, at = @At(value = "HEAD"))
    void onClientBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        Hooks.onClientBlockChanged((ClientWorld) (Object) this, pos, oldState, newState);
    }

    @Inject(method = ctor, at = @At("RETURN"))
    void onClientWorldCreated(ClientPlayNetworkHandler netHandler, LevelInfo levelInfo, DimensionType dimensionType, int i, Profiler profiler, WorldRenderer worldRenderer, CallbackInfo ci) {
        ClientWorldLoadCallback.EVENT.invoker().loadWorld((ClientWorld) (Object) this);
    }

    /**
     * Inject a callback to call for every random display tick. Used for adding custom particle effects to blocks.
     */
    @Inject(method = worldDisplayTick, at = @At(value = "INVOKE", target = blockDisplayTick))
    void onRandomDisplayTick(int xCenter, int yCenter, int zCenter, int radius, Random random, boolean spawnBarrierParticles, BlockPos.Mutable mutable, CallbackInfo ci) {
        Hooks.onRandomDisplayTick((ClientWorld) (Object) this, mutable);
    }
}
