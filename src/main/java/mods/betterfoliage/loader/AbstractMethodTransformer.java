package mods.betterfoliage.loader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public abstract class AbstractMethodTransformer {
    
    /** Instruction node filter
     * @author octarine-noise
     */
    public static interface IInstructionMatch {
        public boolean matches(AbstractInsnNode node);
    }
    
    protected ClassNode currentClass;
    protected MethodNode currentMethod;
    protected Namespace environment;
    
    public abstract void transform();
    
    protected void insertAfter(IInstructionMatch filter, AbstractInsnNode... added) {
        InsnList listAdd = new InsnList();
        for (AbstractInsnNode inst : added) listAdd.add(inst);
        AbstractInsnNode targetNode = findNext(currentMethod.instructions.getFirst(), filter);
        currentMethod.instructions.insert(targetNode, listAdd);
    }
    
    protected void insertBefore(IInstructionMatch filter, AbstractInsnNode... added) {
        InsnList listAdd = new InsnList();
        for (AbstractInsnNode inst : added) listAdd.add(inst);
        AbstractInsnNode targetNode = findNext(currentMethod.instructions.getFirst(), filter);
        currentMethod.instructions.insertBefore(targetNode, listAdd);
    }
    
    protected void replace(IInstructionMatch filter, AbstractInsnNode... added) {
        insertAfter(filter, added);
        AbstractInsnNode targetNode = findNext(currentMethod.instructions.getFirst(), filter);
        currentMethod.instructions.remove(targetNode);
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
    
    protected IInstructionMatch matchOpcode(final int opcode) {
        return new IInstructionMatch() {
            public boolean matches(AbstractInsnNode node) {
                return node.getOpcode() == opcode;
            }
        };
    }
    
    protected IInstructionMatch matchVarInsn(final int opcode, final int var) {
        return new IInstructionMatch() {
            public boolean matches(AbstractInsnNode node) {
                if (node instanceof VarInsnNode) {
                    return (node.getOpcode() == opcode) && ( ((VarInsnNode) node).var == var);
                }
                return false;
            }
        };
    }
   
    protected IInstructionMatch matchInvokeMethod(final MethodRef method) {
        return new IInstructionMatch() {
            public boolean matches(AbstractInsnNode node) {
                if (!(node instanceof MethodInsnNode)) return false;
                MethodInsnNode methodNode = (MethodInsnNode) node;
                return methodNode.name.equals(method.getName(environment)) && methodNode.owner.equals(method.parent.getName(environment).replace(".", "/"));
            }
        };
    }
    
    /**
     * @return an instruction node filter matching an invoke instruction to a specified method name
     */
    protected IInstructionMatch matchInvokeName(final String methodName) {
        return new IInstructionMatch() {
            public boolean matches(AbstractInsnNode node) {
                return (node instanceof MethodInsnNode) && methodName.equals(((MethodInsnNode) node).name);
            }
        };
    }
    
    protected FieldInsnNode createGetField(FieldRef field) {
        return new FieldInsnNode(Opcodes.GETFIELD, field.parent.getName(environment).replace(".", "/"), field.getName(environment), field.getAsmDescriptor(environment));
    }
   
    protected MethodInsnNode createInvokeStatic(MethodRef method) {
        return new MethodInsnNode(Opcodes.INVOKESTATIC, method.parent.getName(environment).replace(".", "/"), method.getName(environment), method.getAsmDescriptor(environment), false);
    }
}