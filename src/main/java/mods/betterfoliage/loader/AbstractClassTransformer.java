package mods.betterfoliage.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Maps;


public abstract class AbstractClassTransformer implements IClassTransformer {

    /** The kind of environment we are in. Assume MCP until proven otherwise */
    protected Namespace environment = Namespace.MCP;
    
    protected Map<MethodRef, AbstractMethodTransformer> methodTransformers = Maps.newHashMap();
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // ???
        if (basicClass == null) return null;
        
        // test the environment - a name mismatch indicates the presence of obfuscated code
        if (!transformedName.equals(name)) {
            environment = Namespace.OBF;
        }
        
        // read class data
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        boolean hasTransformed = false;
        
        for (Map.Entry<MethodRef, AbstractMethodTransformer> entry : methodTransformers.entrySet()) {
            if (transformedName.equals(entry.getKey().parent.getName(Namespace.MCP))) {
                for (MethodNode methodNode : classNode.methods) {
                    if (entry.getKey().getName(environment).equals(methodNode.name) && entry.getKey().getAsmDescriptor(environment).equals(methodNode.desc)) {
                        AbstractMethodTransformer transformer = entry.getValue();
                        hasTransformed = true;
                        
                        // transformers are not thread-safe because of laziness reasons
                        synchronized(transformer) {
                            transformer.currentClass = classNode;
                            transformer.currentMethod = methodNode;
                            transformer.environment = environment;
                            try {
                                transformer.transform();
                            } catch (Exception e) {
                                // oops
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        
        // return result
        if (hasTransformed) {
            ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
//            dumpClass(writer.toByteArray(), transformedName, BetterFoliage.configDir);
            return writer.toByteArray();
        } else {
            return basicClass;
        }
    }
    
    protected boolean isServerSide() {
    	String side = FMLLaunchHandler.side().name();
    	return "SERVER".equals(side);
    }
    
    protected void dumpClass(byte[] classData, String className, File dir) {
        File output = new File(dir, className + ".class");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(output);
            fos.write(classData);
            fos.close();
        } catch (Exception e) {}
    }
}
