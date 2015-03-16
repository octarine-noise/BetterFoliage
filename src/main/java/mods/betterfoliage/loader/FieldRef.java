package mods.betterfoliage.loader;

import java.lang.reflect.Field;

/** Reference to a field. Contains information to locate the field regardless of environment.
 * @author octarine-noise
 */
public class FieldRef implements IResolvable<Field> {
    
    /** Containing class */
    public ClassRef parent;
    
    /** Field name in MCP namespace */
    public String mcpName;
    
    /** Field name in SRG namespace */
    public String srgName;
    
    /** Field name in OBF namespace */
    public String obfName;
    
    /** Field type */
    public ClassRef type;
    
    /** Cached {@link Field} object to use for reflection */
    public Field fieldObj;
    
    public FieldRef(ClassRef parent, String mcpName, String srgName, String obfName, ClassRef returnType) {
        this.parent = parent;
        this.mcpName = mcpName;
        this.srgName = srgName;
        this.obfName = obfName;
        this.type = returnType;
    }

    public FieldRef(ClassRef parent, String mcpName, ClassRef returnType) {
        this(parent, mcpName, mcpName, mcpName, returnType);
    }
    
    /** Get field name in the given namespace
     * @param ns
     * @return
     */
    public String getName(Namespace ns) {
        if (ns == Namespace.OBF) return obfName;
        if (ns == Namespace.SRG) return srgName;
        return mcpName;
    }
    
    /** Get ASM field descriptor in the given namespace
     * @param ns
     * @return
     */
    public String getAsmDescriptor(Namespace ns) {
        return type.getAsmDescriptor(ns);
    }
    
    public Field resolve() {
        if (fieldObj == null) {
            Class<?> parentClass = parent.resolve();
            if (parentClass == null) return null;
            try {
                fieldObj = parentClass.getDeclaredField(srgName);
                fieldObj.setAccessible(true);
            } catch (Exception e) {}
            if (fieldObj == null) try {
                fieldObj = parentClass.getDeclaredField(mcpName);
                fieldObj.setAccessible(true);
            } catch (Exception e) {}
        }
        return fieldObj;
    }
    
    /** Get field value for a given instance
     * @param instance
     * @return field value
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstanceField(Object instance) {
        if (resolve() == null) return null;
        
        try {
            Object result = resolve().get(instance);
            return (T) result;
        } catch (Exception e) {
            return null;
        }
    }
}