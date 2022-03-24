package mods.betterfoliage.mixin;

import mods.betterfoliage.ClientChunkLoadCallback;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    private static final String onLoadChunkFromPacket = "Lnet/minecraft/client/world/ClientChunkManager;loadChunkFromPacket(IILnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/network/PacketByteBuf;Lnet/minecraft/nbt/CompoundTag;IZ)Lnet/minecraft/world/chunk/WorldChunk;";

    @Inject(method = "loadChunkFromPacket", at = @At(value = "RETURN", ordinal = 1))
    void onLoadChunkFromPacket(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
        ClientChunkLoadCallback.EVENT.invoker().loadChunk(cir.getReturnValue());
    }
}
