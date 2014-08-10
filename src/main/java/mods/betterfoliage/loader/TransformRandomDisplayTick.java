package mods.betterfoliage.loader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformRandomDisplayTick extends MethodTransformerBase {

	@Override
	public String getClassName() {
		return "net.minecraft.client.multiplayer.WorldClient";
	}

	@Override
	public String getMethodName() {
		return "doVoidFogParticles";
	}

	@Override
	public String getSignature() {
		return "(III)V";
	}

	@Override
	public String getLogMessage() {
		return "Applying random display tick call hook";
	}

	@Override
	public void transform(MethodNode method, boolean obf) {
		AbstractInsnNode endLoop = findNext(method.instructions.getFirst(), matchOpcode(Opcodes.IINC));
		insertBefore(method.instructions, endLoop,
			new VarInsnNode(Opcodes.ALOAD, 10),
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ILOAD, 7),
			new VarInsnNode(Opcodes.ILOAD, 8),
			new VarInsnNode(Opcodes.ILOAD, 9),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/BetterFoliageClient", "onRandomDisplayTick", signature("(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;III)V", obf))
		);
	}
}
