package mods.betterfoliage.mixin;

import mods.betterfoliage.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(targets = {"net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask"})
public class MixinOptifineChunkRender {

    private static final String compile = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask;compile(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Ljava/util/Set;";
    private static final String invokeReflector = "Lnet/optifine/reflect/Reflector;callBoolean(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z";
    private static final String forgeBlockCanRender = "Lnet/minecraft/client/renderer/chunk/ChunkRender;FORGE_BLOCK_CAN_RENDER_IN_LAYER:Z";

//    @Redirect(
//            method = compile,
//            at = @At(value = "INVOKE", target = invokeReflector),
//            slice = @Slice(
//                    from = @At(value = "FIELD", target = forgeBlockCanRender)
//            )
//    )
//    @SuppressWarnings("UnresolvedMixinReference")
//    boolean canRenderInLayer(Object state, @Coerce Object reflector, Object[] layer) {
//        return Hooks.canRenderInLayerOverride((BlockState) state, (RenderType) layer[0]);
//    }
}
