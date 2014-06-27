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
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MethodMatch {
		public String name();
		public String signature();
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MethodTransform {
		public String className();
		public MethodMatch deobf();
		public MethodMatch obf();
		public String log();
	}
	
	protected Logger logger = LogManager.getLogger(getClass().getSimpleName());
	
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		// read class
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        boolean hasTransformed = false;
        
		for (Method classMethod : getClass().getMethods()) {
			// check for annotated method with correct signature
			MethodTransform annot = classMethod.getAnnotation(MethodTransform.class);
			if (annot == null) continue;
			if (classMethod.getParameterTypes().length != 2) continue;
			if (!classMethod.getParameterTypes()[0].equals(MethodNode.class)) continue;
			if (!classMethod.getParameterTypes()[1].equals(boolean.class)) continue;
			
			// try to find specified method in class
			if (!transformedName.equals(annot.className())) continue;
			for (MethodNode methodNode : classNode.methods) {
				Boolean obf = null;
				if (methodNode.name.equals(annot.obf().name()) && methodNode.desc.equals(annot.obf().signature())) {
					obf = true;
				} else if (methodNode.name.equals(annot.deobf().name()) && methodNode.desc.equals(annot.deobf().signature())) {
					obf = false;
				}
				
				if (obf != null) {
					// transform
					hasTransformed = true;
					try {
						classMethod.invoke(this, new Object[] {methodNode, obf});
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
