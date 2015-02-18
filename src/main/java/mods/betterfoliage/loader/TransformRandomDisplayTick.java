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
        boolean optifinePresent = false;
        try {
            @SuppressWarnings("unused") Class<?> optifine = Class.forName("optifine.OptiFineClassTransformer");
            optifinePresent = true;
        } catch (ClassNotFoundException e) { }
        
		AbstractInsnNode endLoop = findNext(method.instructions.getFirst(), matchOpcode(Opcodes.IINC));
		insertBefore(method.instructions, endLoop,
			new VarInsnNode(Opcodes.ALOAD, 0),
			new VarInsnNode(Opcodes.ALOAD, 13),
			optifinePresent ? new VarInsnNode(Opcodes.ALOAD, 8) : new VarInsnNode(Opcodes.ALOAD, 12),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/BetterFoliageClient", "onRandomDisplayTick", signature("(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;)V", obf), false)
		);
	}
}
