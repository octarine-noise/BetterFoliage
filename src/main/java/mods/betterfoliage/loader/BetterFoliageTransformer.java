package mods.betterfoliage.loader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class BetterFoliageTransformer extends EZTransformerBase {

	public BetterFoliageTransformer() {
		String mcVersion = FMLInjectionData.data()[4].toString();
		if (!ImmutableList.<String>of("1.7.2", "1.7.10").contains(mcVersion))
			logger.warn(String.format("Unsupported Minecraft version %s", mcVersion));
		
		DeobfHelper.init();
	}
	
	@MethodTransform(className="net.minecraft.client.renderer.RenderBlocks",
					 methodName="renderBlockByRenderType",
					 signature="(Lnet/minecraft/block/Block;III)Z",
					 log="Applying RenderBlocks.renderBlockByRenderType() render type ovverride")
	public void handleRenderBlockOverride(MethodNode method) {
		AbstractInsnNode invokeGetRenderType = findNext(method.instructions.getFirst(), matchInvokeAny());
		AbstractInsnNode storeRenderType = findNext(invokeGetRenderType, matchOpcode(Opcodes.ISTORE));
		insertAfter(method.instructions, storeRenderType,
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldInsnNode(Opcodes.GETFIELD, className("net/minecraft/client/renderer/RenderBlocks"), element("blockAccess"), signature("Lnet/minecraft/world/IBlockAccess;")),
			new VarInsnNode(Opcodes.ILOAD, 2),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new VarInsnNode(Opcodes.ILOAD, 4),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ILOAD, 5),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/BetterFoliageClient", "getRenderTypeOverride", signature("(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/block/Block;I)I")),
			new VarInsnNode(Opcodes.ISTORE, 5)
		);
	}
	
	@MethodTransform(className="shadersmodcore.client.Shaders",
					 methodName="pushEntity",
					 signature="(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;III)V",
					 log="Applying Shaders.pushEntity() block id ovverride")
	public void handleGLSLBlockIDOverride(MethodNode method) {
		AbstractInsnNode arrayStore = findNext(method.instructions.getFirst(), matchOpcode(Opcodes.IASTORE));
		insertAfter(method.instructions, arrayStore.getPrevious(),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/BetterFoliageClient", "getGLSLBlockIdOverride", signature("(ILnet/minecraft/block/Block;)I"))
		);
	}
}
