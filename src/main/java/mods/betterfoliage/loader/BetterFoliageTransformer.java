package mods.betterfoliage.loader;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class BetterFoliageTransformer implements IClassTransformer {

	protected Iterable<MethodTransformerBase> transformers = ImmutableList.<MethodTransformerBase>of(
		new TransformRenderBlockOverride(),
		new TransformShaderModBlockOverride(),
		new TransformRandomDisplayTick()
	);
	
	protected Logger logger = LogManager.getLogger(getClass().getSimpleName());
	
	public BetterFoliageTransformer() {
		String mcVersion = FMLInjectionData.data()[4].toString();
		if (!ImmutableList.<String>of("1.7.2", "1.7.10").contains(mcVersion))
		logger.warn(String.format("Unsupported Minecraft version %s", mcVersion));
		
		DeobfHelper.init();
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		// ???
		if (basicClass == null) return null;
		
		// read class
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        boolean hasTransformed = false;
        
        for (MethodTransformerBase transformer : transformers) {
        	// try to find specified method in class
			if (!transformedName.equals(transformer.getClassName())) continue;
			
			logger.debug(String.format("Found class: %s -> %s", name, transformedName));
			for (MethodNode methodNode : classNode.methods) {
				logger.trace(String.format("Checking method: %s, sig: %s", methodNode.name, methodNode.desc));
				Boolean isObfuscated = null;
				if (methodNode.name.equals(DeobfHelper.transformElementName(transformer.getMethodName())) && methodNode.desc.equals(DeobfHelper.transformSignature(transformer.getSignature()))) {
					isObfuscated = true;
				} else if (methodNode.name.equals(transformer.getMethodName()) && methodNode.desc.equals(transformer.getSignature())) {
					isObfuscated = false;
				}
				
				if (isObfuscated != null) {
					// transform
					hasTransformed = true;
					try {
						transformer.transform(methodNode, isObfuscated);
						logger.info(String.format("%s: SUCCESS", transformer.getLogMessage()));
					} catch (Exception e) {
						logger.info(String.format("%s: FAILURE", transformer.getLogMessage()));
					}
					break;
				}
			}
        }
        
        // return result
 		ClassWriter writer = new ClassWriter(0);
 		if (hasTransformed) classNode.accept(writer);
 		return !hasTransformed ? basicClass : writer.toByteArray();
	}

}
