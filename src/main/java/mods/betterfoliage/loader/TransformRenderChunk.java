package mods.betterfoliage.loader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformRenderChunk extends MethodTransformerBase {

	@Override
	public String getClassName() {
		return "net.minecraft.client.renderer.chunk.RenderChunk";
	}

	@Override
	public String getMethodName() {
		return "rebuildChunk";
	}

	@Override
	public String getSignature() {
		return "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V";
	}

	@Override
	public String getLogMessage() {
		return "Applying RenderChunk block layer override";
	}

	@Override
	public void transform(MethodNode method, boolean obf) {
	    AbstractInsnNode invokeCanRender = findNext(method.instructions.getFirst(), matchInvokeMethod(className("net/minecraft/block/Block", obf), element("canRenderInLayer", obf)));
	    AbstractInsnNode invokeRenderBlock = findNext(invokeCanRender, matchInvokeMethod(className("net/minecraft/client/renderer/BlockRendererDispatcher", obf), element("renderBlock", obf)));
	    
	    insertAfter(method.instructions, invokeCanRender,
	        new MethodInsnNode(
	            Opcodes.INVOKESTATIC,
	            "mods/betterfoliage/client/BetterFoliageClient",
	            "canRenderBlockInLayer",
	            "(Lnet/minecraft/block/Block;Lnet/minecraft/util/EnumWorldBlockLayer;)Z",
	            false
	        )
	    );
	    method.instructions.remove(invokeCanRender);

	    insertBefore(method.instructions, invokeRenderBlock,
	        new VarInsnNode(Opcodes.ALOAD, 18),
	        new MethodInsnNode(
	            Opcodes.INVOKESTATIC,
	            "mods/betterfoliage/client/BetterFoliageClient",
	            "renderWorldBlock",
	            signature("(Lnet/minecraft/client/renderer/BlockRendererDispatcher;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/util/EnumWorldBlockLayer;)Z", obf),
	            false)
	    );
	    method.instructions.remove(invokeRenderBlock);
	}

}
