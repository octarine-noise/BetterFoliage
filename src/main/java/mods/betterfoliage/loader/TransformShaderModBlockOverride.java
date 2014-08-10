package mods.betterfoliage.loader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformShaderModBlockOverride extends MethodTransformerBase {

	@Override
	public String getClassName() {
		return "shadersmodcore.client.Shaders";
	}

	@Override
	public String getMethodName() {
		return "pushEntity";
	}

	@Override
	public String getSignature() {
		return "(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;III)V";
	}

	@Override
	public String getLogMessage() {
		return "Applying Shaders.pushEntity() block id override";
	}

	@Override
	public void transform(MethodNode method, boolean obf) {
		AbstractInsnNode arrayStore = findNext(method.instructions.getFirst(), matchOpcode(Opcodes.IASTORE));
		insertAfter(method.instructions, arrayStore.getPrevious(),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/ShadersModIntegration", "getBlockIdOverride", signature("(ILnet/minecraft/block/Block;)I", obf))
		);
	}

}
