package mods.betterfoliage.loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EZTransformerBase implements IClassTransformer {

	public static interface IInstructionMatch {
		public boolean matches(AbstractInsnNode node);
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MethodTransform {
		public String className();
		public String methodName();
		public String signature();
		public String log();
	}
	
	protected Logger logger = LogManager.getLogger(getClass().getSimpleName());
	
	protected Boolean isObfuscated;
	
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		// ???
		if (basicClass == null) return null;
		
		// read class
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        boolean hasTransformed = false;
        
		for (Method classMethod : getClass().getMethods()) {
			// check for annotated method with correct signature
			MethodTransform annot = classMethod.getAnnotation(MethodTransform.class);
			if (annot == null) continue;
			if (classMethod.getParameterTypes().length != 1) continue;
			if (!classMethod.getParameterTypes()[0].equals(MethodNode.class)) continue;
			
			// try to find specified method in class
			if (!transformedName.equals(annot.className())) continue;
			logger.debug(String.format("Found class: %s -> %s", name, transformedName));
			for (MethodNode methodNode : classNode.methods) {
				logger.trace(String.format("Checking method: %s, sig: %s", methodNode.name, methodNode.desc));
				isObfuscated = null;
				if (methodNode.name.equals(DeobfHelper.transformElementName(annot.methodName())) && methodNode.desc.equals(DeobfHelper.transformSignature(annot.signature()))) {
					isObfuscated = true;
				} else if (methodNode.name.equals(annot.methodName()) && methodNode.desc.equals(annot.signature())) {
					isObfuscated = false;
				}
				
				if (isObfuscated != null) {
					// transform
					hasTransformed = true;
					try {
						classMethod.invoke(this, new Object[] {methodNode});
						logger.info(String.format("%s: SUCCESS", annot.log()));
					} catch (Exception e) {
						logger.info(String.format("%s: FAILURE", annot.log()));
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

	protected String className(String className) {
		return isObfuscated ? DeobfHelper.transformClassName(className) : className;
	}
	
	protected String element(String fieldName) {
		return isObfuscated ? DeobfHelper.transformElementName(fieldName) : fieldName;
	}
	
	protected String signature(String signature) {
		return isObfuscated ? DeobfHelper.transformSignature(signature) : signature;
	}
	
	protected AbstractInsnNode findNext(AbstractInsnNode start, IInstructionMatch match) {
		AbstractInsnNode current = start;
		while(current != null) {
			if (match.matches(current)) break;
			current = current.getNext();
		}
		return current;
	}
	
	protected AbstractInsnNode findPrevious(AbstractInsnNode start, IInstructionMatch match) {
		AbstractInsnNode current = start;
		while(current != null) {
			if (match.matches(current)) break;
			current = current.getPrevious();
		}
		return current;
	}
	
	protected static IInstructionMatch matchInvokeAny() {
		return new IInstructionMatch() {
			public boolean matches(AbstractInsnNode node) {
				return node instanceof MethodInsnNode;
			}
		};
	}
	
	protected static IInstructionMatch matchOpcode(final int opcode) {
		return new IInstructionMatch() {
			public boolean matches(AbstractInsnNode node) {
				return node.getOpcode() == opcode;
			}
		};
	}
	
	protected static void insertAfter(InsnList insnList, AbstractInsnNode node, AbstractInsnNode... added) {
		InsnList listAdd = new InsnList();
		for (AbstractInsnNode inst : added) listAdd.add(inst);
		insnList.insert(node, listAdd);
	}
}
