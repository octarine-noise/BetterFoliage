package mods.betterfoliage.loader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformRenderBlockOverride extends MethodTransformerBase {

	@Override
	public String getClassName() {
		return "net.minecraft.client.renderer.RenderBlocks";
	}

	@Override
	public String getMethodName() {
		return "renderBlockByRenderType";
	}

	@Override
	public String getSignature() {
		return "(Lnet/minecraft/block/Block;III)Z";
	}

	@Override
	public String getLogMessage() {
		return "Applying RenderBlocks.renderBlockByRenderType() render type override";
	}

	@Override
	public void transform(MethodNode method, boolean obf) {
		AbstractInsnNode invokeGetRenderType = findNext(method.instructions.getFirst(), matchInvokeAny());
		AbstractInsnNode storeRenderType = findNext(invokeGetRenderType, matchOpcode(Opcodes.ISTORE));
		insertAfter(method.instructions, storeRenderType,
			new VarInsnNode(Opcodes.ALOAD, 0),
			new FieldInsnNode(Opcodes.GETFIELD, className("net/minecraft/client/renderer/RenderBlocks", obf), element("blockAccess", obf), signature("Lnet/minecraft/world/IBlockAccess;", obf)),
			new VarInsnNode(Opcodes.ILOAD, 2),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new VarInsnNode(Opcodes.ILOAD, 4),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new VarInsnNode(Opcodes.ILOAD, 5),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/BetterFoliageClient", "getRenderTypeOverride", signature("(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/block/Block;I)I", obf)),
			new VarInsnNode(Opcodes.ISTORE, 5)
		);
	}

}
