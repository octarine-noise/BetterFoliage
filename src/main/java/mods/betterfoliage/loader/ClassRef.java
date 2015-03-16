package mods.betterfoliage.loader;


/** Reference to a class. Contains information to locate the class regardless of environment.
 * @author octarine-noise
 */
public class ClassRef implements IResolvable<Class<?>> {
    
    /** Reference to primitive <b>int</b> type */
    public static final ClassRef INT = primitive("I", int.class);
    
    /** Reference to primitive <b>long</b> type */
    public static final ClassRef LONG = primitive("J", long.class);
    
    /** Reference to primitive <b>float</b> type */
    public static final ClassRef FLOAT = primitive("F", float.class);
    
    /** Reference to primitive <b>boolean</b> type */
    public static final ClassRef BOOLEAN = primitive("Z", boolean.class);
    
    /** Reference to primitive <b>void</b> type */
    public static final ClassRef VOID = primitive("V", void.class);
    
    /** True for primitive types */
    public boolean isPrimitive = false;
    
    /** Class name in MCP namespace */
    public String mcpName;
    
    /** Class name in OBF namespace */
    public String obfName;
    
    /** Cached {@link Class} object to use for reflection */
    public Class<?> classObj;
    
    public ClassRef(String mcpName, String obfName) {
        this.mcpName = mcpName;
        this.obfName = obfName;
    }
    
    public ClassRef(String mcpName) {
        this(mcpName, mcpName);
    }
    
    /** Internal factory for primitive types
     * @param name
     * @param special
     * @return
     */
    protected static ClassRef primitive(String name, Class<?> classObj) {
        ClassRef result = new ClassRef(name);
        result.isPrimitive = true;
        result.classObj = classObj;
        return result;
    }
    
    /** Get class name in the given namespace
     * @param ns
     * @return
     */
    public String getName(Namespace ns) {
        return (ns == Namespace.OBF) ? obfName : mcpName;
    }
    
    /** Get ASM class descriptor in the given namespace
     * @param ns
     * @return
     */
    public String getAsmDescriptor(Namespace ns) {
        return isPrimitive ? mcpName : "L" + getName(ns).replace(".", "/") + ";";
    }
    
    public Class<?> resolve() {
        if (classObj == null) {
            try {
                classObj = Class.forName(mcpName);
            } catch (ClassNotFoundException e) {}
            if (classObj == null) try {
                classObj = Class.forName(obfName);
            } catch (ClassNotFoundException e) {}
        }
        return classObj;
    }
}