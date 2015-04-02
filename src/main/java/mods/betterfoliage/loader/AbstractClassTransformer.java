package mods.betterfoliage.loader;

import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.FMLLaunchHandler;


public abstract class AbstractClassTransformer implements IClassTransformer {

	protected final Logger log = LogManager.getLogger(getClass().getSimpleName());
	
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
            	log.debug(String.format("Found class: %s -> %s", name, transformedName));
            	log.debug(String.format("Searching for method: %s %s -> %s %s", 
            			entry.getKey().getName(Namespace.OBF), entry.getKey().getAsmDescriptor(Namespace.OBF),
            			entry.getKey().getName(Namespace.MCP), entry.getKey().getAsmDescriptor(Namespace.MCP)));
                for (MethodNode methodNode : classNode.methods) {
                	log.debug(String.format("    %s, %s", methodNode.name, methodNode.desc));
                	// try to match against both namespaces - mods sometimes have deobfed class names in signatures
                    if (entry.getKey().getName(Namespace.MCP).equals(methodNode.name) && entry.getKey().getAsmDescriptor(Namespace.MCP).equals(methodNode.desc) ||
                    	entry.getKey().getName(Namespace.OBF).equals(methodNode.name) && entry.getKey().getAsmDescriptor(Namespace.OBF).equals(methodNode.desc)) {
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
        ClassWriter writer = new ClassWriter(0);
        if (hasTransformed) classNode.accept(writer);
        return !hasTransformed ? basicClass : writer.toByteArray();
    }
    
    protected boolean isServerSide() {
    	String side = FMLLaunchHandler.side().name();
    	return "SERVER".equals(side);
    }
}
