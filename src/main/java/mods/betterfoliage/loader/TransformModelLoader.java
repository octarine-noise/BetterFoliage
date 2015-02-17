package mods.betterfoliage.loader;

import mods.betterfoliage.client.BetterFoliageClient;
import net.minecraftforge.client.model.ModelLoader;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;


/** Class transformer.<br/>
 * {@link ModelLoader#setupModelRegistry()} will call the method {@link BetterFoliageClient#onAfterLoadModelDefinitions(ModelLoader)} after model definitions are loaded, but before the texture atlas is loaded and models are baked. 
 * @author octarine-noise
 */
public class TransformModelLoader extends MethodTransformerBase {

    @Override
    public String getClassName() {
        return "net.minecraftforge.client.model.ModelLoader";
    }

    @Override
    public String getMethodName() {
        return "setupModelRegistry";
    }

    @Override
    public String getSignature() {
        return "()Lnet/minecraft/util/IRegistry;";
    }

    @Override
    public String getLogMessage() {
        return "Applying ModelLoader lifecycle callback";
    }

    @Override
    public void transform(MethodNode method, boolean obf) {
        AbstractInsnNode invokeSetupModels = findNext(method.instructions.getFirst(), matchInvokeMethod("addAll"));
        insertAfter(method.instructions, invokeSetupModels,
            new VarInsnNode(Opcodes.ALOAD, 0),
            new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/betterfoliage/client/BetterFoliageClient", "onAfterLoadModelDefinitions", "(Lnet/minecraftforge/client/model/ModelLoader;)V", false));
    }

}
