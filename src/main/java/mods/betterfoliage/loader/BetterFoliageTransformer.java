package mods.betterfoliage.loader;

import mods.betterfoliage.common.util.DeobfNames;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class BetterFoliageTransformer extends EZTransformerBase {

	@MethodTransform(className="net.minecraft.client.renderer.RenderBlocks",
					 obf=@MethodMatch(name=DeobfNames.RB_RBBRT_NAME_OBF, signature=DeobfNames.RB_RBBRT_SIG_OBF),
					 deobf=@MethodMatch(name=DeobfNames.RB_RBBRT_NAME_MCP, signature=DeobfNames.RB_RBBRT_SIG_MCP),
					 log="Adding RenderBlocks.renderBlockByRenderType() render type ovverride")
	public void handleRenderBlockOverride(MethodNode method, boolean obf) {
		AbstractInsnNode invokeGetRenderType = findNext(method.instructions.getFirst(), matchInvokeAny());
		AbstractInsnNode storeRenderType = findNext(invokeGetRenderType, matchOpcode(Opcodes.ISTORE));
		insertAfter(method.instructions, storeRenderType,
			new VarInsnNode(Opcodes.ILOAD, 5),
			new VarInsnNode(Opcodes.ALOAD, 1),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/BlockRenderTypeOverride", "getRenderTypeOverride", obf ? DeobfNames.BRTO_GRTO_SIG_OBF : DeobfNames.BRTO_GRTO_SIG_MCP),
			new VarInsnNode(Opcodes.ISTORE, 5)
		);
	}
}
