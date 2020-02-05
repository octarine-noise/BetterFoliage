package mods.betterfoliage.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.lang.Exception

/** Get a Java class with the given name. */
fun getJavaClass(name: String) = tryDefault(null) { Class.forName(name) }

/** Get the field with the given name and type using reflection. */
fun <T> Any.reflectField(name: String) = getFieldRecursive(this::class.java, name).let {
    it.isAccessible = true
    it.get(this) as T
}

fun getFieldRecursive(cls: Class<*>, name: String): Field = try {
    cls.getDeclaredField(name)
} catch (e: NoSuchFieldException) {
    cls.superclass?.let { getFieldRecursive(it, name) } ?: throw e
}

fun getMethodRecursive(cls: Class<*>, name: String): Method = try {
    cls.declaredMethods.find { it.name == name } ?: throw NoSuchMethodException()
} catch (e: NoSuchMethodException) {
    cls.superclass?.let { getMethodRecursive(it, name) } ?: throw e
}


interface FieldRef<T> {
    val field: Field?

    /** Get this field using reflection. */
    operator fun get(receiver: Any?) = field?.get(receiver) as T?

    /** Get this static field using reflection. */
    fun getStatic() = get(null)

    /** Get this field using reflection. */
    fun set(receiver: Any?, obj: Any?) { field?.set(receiver, obj) }
}

interface MethodRef<T> {
    val method: Method?

    /** Invoke this method using reflection. */
    operator fun invoke(receiver: Any, vararg args: Any?) = method?.invoke(receiver, *args) as T

    /** Invoke this static method using reflection. */
    fun invokeStatic(vararg args: Any) = method?.invoke(null, *args)
}

const val INTERMEDIARY = "intermediary"

object YarnHelper {
    val logger = LogManager.getLogger()
    val resolver = FabricLoader.getInstance().mappingResolver

    fun <T> requiredField(className: String, fieldName: String, descriptor: String) = Field<T>(false, className, fieldName, descriptor)
    fun <T> requiredMethod(className: String, methodName: String, descriptor: String, vararg params: String) = Method<T>(false, className, methodName, descriptor)

    class Field<T>(val optional: Boolean, val className: String, val fieldName: String, descriptor: String) : FieldRef<T> {
        override val field = FabricLoader.getInstance().mappingResolver.let { resolver ->
            try {
                val classMapped = resolver.mapClassName(INTERMEDIARY, className)
                val fieldMapped = resolver.mapFieldName(INTERMEDIARY, className, fieldName, descriptor)
                Class.forName(classMapped)?.let { cls -> getFieldRecursive(cls, fieldMapped).apply { isAccessible = true } }
            } catch (e: Exception) {
                logger.log(
                    if (optional) Level.DEBUG else Level.ERROR,
                    "Could not resolve field $className.$fieldName ( $descriptor ): ${e.message}"
                )
                if (optional) null else throw e
            }
        }
    }

    class Method<T>(val optional: Boolean, val className: String, val methodName: String, descriptor: String) : MethodRef<T> {
        override val method = FabricLoader.getInstance().mappingResolver.let { resolver ->
            try {
                val classMapped = resolver.mapClassName(INTERMEDIARY, className)
                val methodMapped = resolver.mapMethodName(INTERMEDIARY, className, methodName, descriptor)
                Class.forName(classMapped)?.let { cls -> getMethodRecursive(cls, methodMapped).apply { isAccessible = true } }
            } catch (e: Exception) {
                logger.log(
                    if (optional) Level.DEBUG else Level.ERROR,
                    "Could not resolve field $className.$methodName ( $descriptor ): ${e.message}"
                )
                if (optional) null else throw e
            }
        }
    }
}

//fun Any.isInstance(cls: ClassRefOld<*>) = cls.isInstance(this)

interface ReflectionCallable<T> {
    operator fun invoke(vararg args: Any): T
}
inline operator fun <reified T> Any.get(field: FieldRef<T>) = field.get(this)
inline operator fun <reified T> Any.set(field: FieldRef<T>, value: T) = field.set(this, value)
inline operator fun <T> Any.get(methodRef: MethodRef<T>) = object : ReflectionCallable<T> {
    override fun invoke(vararg args: Any) = methodRef.invoke(this@get, *args)
}
