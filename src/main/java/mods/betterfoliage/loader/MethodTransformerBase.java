package mods.betterfoliage.loader;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Base class for class transformers operating on a single method.
 * @author octarine-noise
 */
public abstract class MethodTransformerBase {

	/** Instruction node filter
	 * @author octarine-noise
	 */
	public static interface IInstructionMatch {
		public boolean matches(AbstractInsnNode node);
	}
	
	/**
	 * @return MCP name of the class to transform
	 */
	public abstract String getClassName();
	
	/**
	 * @return MCP name of the method to transform
	 */
	public abstract String getMethodName();
	
	/**
	 * @return ASM signature of the method to transform using MCP names
	 */
	public abstract String getSignature();
	
	/**
	 * @return Log message to write when method is found
	 */
	public abstract String getLogMessage();
	
	/** Transform method node
	 * @param method method node
	 * @param isObfuscated true for obfuscated environment
	 */
	public abstract void transform(MethodNode method, boolean isObfuscated);
	
	/** Transform a class name from MCP to obfuscated names if necessary.
	 * @param className MCP name
	 * @param isObfuscated true for obfuscated environment
	 * @return transformed name
	 */
	protected static String className(String className, boolean isObfuscated) {
		return isObfuscated ? DeobfHelper.transformClassName(className) : className;
	}
	
	/** Transform a method or field name from MCP to obfuscated names if necessary.
	 * @param fieldName MCP name
	 * @param isObfuscated true for obfuscated environment
	 * @return transformed name
	 */
	protected static String element(String fieldName, boolean isObfuscated) {
		return isObfuscated ? DeobfHelper.transformElementName(fieldName) : fieldName;
	}
	
	/** Transform an ASM signature from MCP to obfuscated names if necessary.
	 * @param signature MCP signature
	 * @param isObfuscated true for obfuscated environment
	 * @return transformed signature
	 */
	protected static String signature(String signature, boolean isObfuscated) {
		return isObfuscated ? DeobfHelper.transformSignature(signature) : signature;
	}
	
	/** Find the next instruction node in an instruction list starting from a given node, matching a given filter
	 * @param start start node
	 * @param match filter
	 * @return instruction node if found, null otherwise
	 */
	protected AbstractInsnNode findNext(AbstractInsnNode start, IInstructionMatch match) {
		AbstractInsnNode current = start;
		while(current != null) {
			if (match.matches(current)) break;
			current = current.getNext();
		}
		return current;
	}
	
	/** Find the previous instruction node in a list starting from a given node, matching a given filter
	 * @param start start node
	 * @param match filter
	 * @return instruction node if found, null otherwise
	 */
	protected AbstractInsnNode findPrevious(AbstractInsnNode start, IInstructionMatch match) {
		AbstractInsnNode current = start;
		while(current != null) {
			if (match.matches(current)) break;
			current = current.getPrevious();
		}
		return current;
	}
	
	/**
	 * @return an instruction node filter matching any invoke instruction
	 */
	protected IInstructionMatch matchInvokeAny() {
		return new IInstructionMatch() {
			public boolean matches(AbstractInsnNode node) {
				return node instanceof MethodInsnNode;
			}
		};
	}
	
	/**
	 * @return an instruction node filter matching the given opcode
	 */
	protected IInstructionMatch matchOpcode(final int opcode) {
		return new IInstructionMatch() {
			public boolean matches(AbstractInsnNode node) {
				return node.getOpcode() == opcode;
			}
		};
	}
	
	/** Insert a list of instruction nodes in a list after a given node
	 * @param insnList instruction list
	 * @param node start node
	 * @param added instructions to add
	 */
	protected void insertAfter(InsnList insnList, AbstractInsnNode node, AbstractInsnNode... added) {
		InsnList listAdd = new InsnList();
		for (AbstractInsnNode inst : added) listAdd.add(inst);
		insnList.insert(node, listAdd);
	}
	
	/** Insert a list of instruction nodes in a list before a given node
	 * @param insnList instruction list
	 * @param node start node
	 * @param added instructions to add
	 */
	protected void insertBefore(InsnList insnList, AbstractInsnNode node, AbstractInsnNode... added) {
		InsnList listAdd = new InsnList();
		for (AbstractInsnNode inst : added) listAdd.add(inst);
		insnList.insertBefore(node, listAdd);
	}
}
