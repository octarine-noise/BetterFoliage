package mods.betterfoliage.loader;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class MethodTransformerBase {

	public static interface IInstructionMatch {
		public boolean matches(AbstractInsnNode node);
	}
	
	public abstract String getClassName();
	public abstract String getMethodName();
	public abstract String getSignature();
	public abstract String getLogMessage();
	
	public abstract void transform(MethodNode method, boolean obf);
	
	protected static String className(String className, boolean isObfuscated) {
		return isObfuscated ? DeobfHelper.transformClassName(className) : className;
	}
	
	protected static String element(String fieldName, boolean isObfuscated) {
		return isObfuscated ? DeobfHelper.transformElementName(fieldName) : fieldName;
	}
	
	protected static String signature(String signature, boolean isObfuscated) {
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
	
	protected IInstructionMatch matchInvokeAny() {
		return new IInstructionMatch() {
			public boolean matches(AbstractInsnNode node) {
				return node instanceof MethodInsnNode;
			}
		};
	}
	
	protected IInstructionMatch matchOpcode(final int opcode) {
		return new IInstructionMatch() {
			public boolean matches(AbstractInsnNode node) {
				return node.getOpcode() == opcode;
			}
		};
	}
	
	protected void insertAfter(InsnList insnList, AbstractInsnNode node, AbstractInsnNode... added) {
		InsnList listAdd = new InsnList();
		for (AbstractInsnNode inst : added) listAdd.add(inst);
		insnList.insert(node, listAdd);
	}
	
	protected void insertBefore(InsnList insnList, AbstractInsnNode node, AbstractInsnNode... added) {
		InsnList listAdd = new InsnList();
		for (AbstractInsnNode inst : added) listAdd.add(inst);
		insnList.insertBefore(node, listAdd);
	}
}
