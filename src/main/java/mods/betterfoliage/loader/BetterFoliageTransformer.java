package mods.betterfoliage.loader;

import mods.betterfoliage.common.util.DeobfNames;
import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Transformer overriding the first line of RenderBlocks.renderBlockByRenderType()
 * with the following instruction:<br/><br/>
 * int l = mods.betterfoliage.BlockRenderTypeOverride.getRenderType(block);<br/><br/>
 * 
 * @author octarine-noise
 */
public class BetterFoliageTransformer implements IClassTransformer {

	Logger log = LogManager.getLogger("BetterFoliageCore");
	
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
    	if (basicClass == null) return null;
		
		if (transformedName.equals("net.minecraft.client.renderer.RenderBlocks")) {
			log.info(String.format("Found class %s", transformedName));
			ClassNode classNode = new ClassNode();
	        ClassReader classReader = new ClassReader(basicClass);
	        classReader.accept(classNode, 0);
	        
			for (MethodNode mn : classNode.methods) {
				boolean found = false;
				boolean obf = false;
				if (mn.desc.equals(DeobfNames.RB_RBBRT_SIG_MCP) && (mn.name.equals(DeobfNames.RB_RBBRT_NAME_MCP))) {
					found = true;
				} else if (mn.desc.equals(DeobfNames.RB_RBBRT_SIG_OBF) && (mn.name.equals(DeobfNames.RB_RBBRT_NAME_OBF))) {
					found = true;
					obf = true;
				}
				if (found) {
					log.info("Overriding RenderBlocks.renderBlockByRenderType()");
					int invokeNodeIdx = 0;
					for (int idx = 0; idx < mn.instructions.size(); idx++) if (mn.instructions.get(idx) instanceof MethodInsnNode) {
						invokeNodeIdx = idx;
						break;
					}
					mn.instructions.remove(mn.instructions.get(invokeNodeIdx));
					MethodInsnNode replacement = new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/BlockRenderTypeOverride", "getRenderType", obf ? DeobfNames.BRTO_GRT_SIG_OBF : DeobfNames.BRTO_GRT_SIG_MCP);
					mn.instructions.insertBefore(mn.instructions.get(invokeNodeIdx), replacement);
					break;
				}
			}
			
	        ClassWriter writer = new ClassWriter(0);
	        classNode.accept(writer);
	        return writer.toByteArray();
		}
		return basicClass;
	}
}
