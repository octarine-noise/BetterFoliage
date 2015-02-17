package mods.betterfoliage.loader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformShaderModBlockIdOverride extends MethodTransformerBase {

	@Override
	public String getClassName() {
		return "shadersmod.client.SVertexBuilder";
	}

	@Override
	public String getMethodName() {
		return "pushEntity";
	}

	@Override
	public String getSignature() {
		return "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/WorldRenderer;)V";
	}

	@Override
	public String getLogMessage() {
		return "Applying SVertexBuilder.pushEntity() block id override";
	}

	@Override
	public void transform(MethodNode method, boolean obf) {
		AbstractInsnNode valueStore = findNext(method.instructions.getFirst(), matchStore(Opcodes.ISTORE, 8));
		insertBefore(method.instructions, valueStore.getPrevious(),
			new VarInsnNode(Opcodes.ALOAD, 0),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/ShadersModIntegration", "getBlockIdOverride", signature("(ILnet/minecraft/block/state/IBlockState;)I", obf), false)
		);
	}

}
