@file:JvmName("Reflection")
package mods.octarinecore.metaprog

import java.lang.reflect.Field
import java.lang.reflect.Method
import mods.octarinecore.metaprog.Namespace.*
import mods.octarinecore.tryDefault

/** Get a Java class with the given name. */
fun getJavaClass(name: String) = tryDefault(null) { Class.forName(name) }

/** Get the field with the given name and type using reflection. */
inline fun <reified T> Any.reflectField(field: String): T? =
    tryDefault(null) { this.javaClass.getDeclaredField(field) }?.let {
        it.isAccessible = true
        it.get(this) as T
    }

/** Get the static field with the given name and type using reflection. */
inline fun <reified T> Class<*>.reflectStaticField(field: String): T? =
    tryDefault(null) { this.getDeclaredField(field) }?.let {
        it.isAccessible = true
        it.get(null) as T
    }

/**
 * Get all nested _object_s of this _object_ with reflection.
 *
 * @return [Pair]s of (name, instance)
 */
val Any.reflectNestedObjects: List<Pair<String, Any>> get() = this.javaClass.declaredClasses.map {
    tryDefault(null) { it.name.split("$")[1] to it.getField("INSTANCE").get(null) }
}.filterNotNull()

/**
 * Get all fields of this instance that match (or subclass) any of the given classes.
 *
 * @param[types] classes to look for
 * @return [Pair]s of (field name, instance)
 */
fun Any.reflectFieldsOfType(vararg types: Class<*>) = this.javaClass.declaredFields
    .filter { field -> types.any { it.isAssignableFrom(field.type) } }
    .map { field -> field.name to field.let { it.isAccessible = true; it.get(this) } }
    .filterNotNull()

enum class Namespace { OBF, SRG, MCP }

abstract class Resolvable<T> {
    abstract fun resolve(): T?
    val element: T? by lazy { resolve() }
}

/** Return true if all given elements are found. */
fun allAvailable(vararg codeElement: Resolvable<*>) = codeElement.all { it.element != null }

/**
 * Reference to a class.
 *
 * @param[mcpName] MCP name of the class
 * @param[obfName] obfuscated name of the class
 */
open class ClassRef(val mcpName: String, val obfName: String) : Resolvable<Class<*>>() {
    constructor(mcpName: String) : this(mcpName, mcpName)

    companion object {
        val int = ClassRefPrimitive("I", Int::class.java)
        val long = ClassRefPrimitive("J", Long::class.java)
        val float = ClassRefPrimitive("F", Float::class.java)
        val boolean = ClassRefPrimitive("Z", Boolean::class.java)
        val void = ClassRefPrimitive("V", null)
    }

    fun name(namespace: Namespace) = if (namespace == Namespace.OBF) obfName else mcpName
    open fun asmDescriptor(namespace: Namespace) = "L${name(namespace).replace(".", "/")};"

    override fun resolve() = listOf(mcpName, obfName).map { getJavaClass(it) }.filterNotNull().firstOrNull()

    fun isInstance(obj: Any) = element?.isInstance(obj) ?: false
}

/**
 * Reference to a primitive type.
 *
 * @param[name] ASM descriptor of this primitive type
 * @param[clazz] class of this primitive type
 */
class ClassRefPrimitive(name: String, val clazz: Class<*>?) : ClassRef(name) {
    override fun asmDescriptor(namespace: Namespace) = mcpName
    override fun resolve() = clazz
}

/**
 * Reference to a method.
 *
 * @param[parentClass] reference to the class containing the method
 * @param[mcpName] MCP name of the method
 * @param[srgName] SRG name of the method
 * @param[obfName] obfuscated name of the method
 * @param[returnType] reference to the return type
 * @param[returnType] references to the argument types
 */
class MethodRef(val parentClass: ClassRef,
                val mcpName: String,
                val srgName: String?,
                val obfName: String?,
                val returnType: ClassRef,
                vararg argTypes: ClassRef
) : Resolvable<Method>() {
    constructor(parentClass: ClassRef, mcpName: String, returnType: ClassRef, vararg argTypes: ClassRef) :
    this(parentClass, mcpName, mcpName, mcpName, returnType, *argTypes)

    val argTypes = argTypes

    fun name(namespace: Namespace) = when(namespace) { OBF -> obfName!!; SRG -> srgName!!; MCP -> mcpName }
    fun asmDescriptor(namespace: Namespace) = "(${argTypes.map { it.asmDescriptor(namespace) }.fold(""){ s1, s2 -> s1 + s2 } })${returnType.asmDescriptor(namespace)}"

    override fun resolve(): Method? =
        if (parentClass.element == null || argTypes.any { it.element == null }) null
        else {
            val args = argTypes.map { it.element!! }.toTypedArray()
            listOf(srgName!!, mcpName).map { tryDefault(null) {
                parentClass.element!!.getDeclaredMethod(it, *args)
            }}.filterNotNull().firstOrNull()
            ?.apply { isAccessible = true }
        }

    /** Invoke this method using reflection. */
    fun invoke(receiver: Any, vararg args: Any) = element?.invoke(receiver, *args)

    /** Invoke this static method using reflection. */
    fun invokeStatic(vararg args: Any) = element?.invoke(null, *args)

}

/**
 * Reference to a field.
 *
 * @param[parentClass] reference to the class containing the field
 * @param[mcpName] MCP name of the field
 * @param[srgName] SRG name of the field
 * @param[obfName] obfuscated name of the field
 * @param[type] reference to the field type
 */
class FieldRef(val parentClass: ClassRef,
               val mcpName: String,
               val srgName: String?,
               val obfName: String?,
               val type: ClassRef?
) : Resolvable<Field>() {
    constructor(parentClass: ClassRef, mcpName: String, type: ClassRef?) : this(parentClass, mcpName, mcpName, mcpName, type)

    fun name(namespace: Namespace) = when(namespace) { OBF -> obfName!!; SRG -> srgName!!; MCP -> mcpName }
    fun asmDescriptor(namespace: Namespace) = type!!.asmDescriptor(namespace)

    override fun resolve(): Field? =
        if (parentClass.element == null) null
        else {
            listOf(srgName!!, mcpName).map { tryDefault(null) {
                parentClass.element!!.getDeclaredField(it)
            }}.filterNotNull().firstOrNull()
            ?.apply{ isAccessible = true }
        }

    /** Get this field using reflection. */
    fun get(receiver: Any?) = element?.get(receiver)

    /** Get this static field using reflection. */
    fun getStatic() = get(null)
}