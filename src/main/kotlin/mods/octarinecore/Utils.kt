@file:JvmName("Utils")
@file:Suppress("NOTHING_TO_INLINE")
package mods.octarinecore

import net.minecraft.util.ResourceLocation
import kotlin.reflect.KProperty
import java.lang.Math.*

const val PI2 = 2.0 * PI

inline fun String.stripStart(str: String) = if (startsWith(str)) substring(str.length) else this
inline fun ResourceLocation.stripStart(str: String) = ResourceLocation(resourceDomain, resourcePath.stripStart(str))

/** Mutating version of _map_. Replace each element of the list with the result of the given transformation. */
inline fun <reified T> MutableList<T>.replace(transform: (T) -> T) = forEachIndexed { idx, value -> this[idx] = transform(value) }

/** Exchange the two elements of the list with the given indices */
inline fun <T> MutableList<T>.exchange(idx1: Int, idx2: Int) {
    val e = this[idx1]
    this[idx1] = this[idx2]
    this[idx2] = e
}

/** Cross product of this [Iterable] with the parameter. */
fun <A, B> Iterable<A>.cross(other: Iterable<B>) = flatMap { a -> other.map { b -> a to b } }

inline fun <C, R, T> Iterable<T>.mapAs(transform: (C) -> R) = map { transform(it as C) }

inline fun <T1, T2> forEachNested(list1: Iterable<T1>, list2: Iterable<T2>, func: (T1, T2)-> Unit) =
    list1.forEach { e1 ->
        list2.forEach { e2 ->
            func(e1, e2)
        }
    }

/**
 * Property-level delegate backed by a [ThreadLocal].
 *
 * @param[init] Lambda to get initial value
 */
class ThreadLocalDelegate<T>(init: () -> T) {
    var tlVal = ThreadLocal.withInitial(init)
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = tlVal.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { tlVal.set(value) }
}

/**
 * Starting with the second element of this [Iterable] until the last, call the supplied lambda with
 * the parameters (index, element, previous element).
 */
inline fun <reified T> Iterable<T>.forEachPairIndexed(func: (Int, T, T)->Unit) {
    var previous: T? = null
    forEachIndexed { idx, current ->
        if (previous != null) func(idx, current, previous!!)
        previous = current
    }
}

/** Call the supplied lambda and return its result, or the given default value if an exception is thrown. */
fun <T> tryDefault(default: T, work: ()->T) = try { work() } catch (e: Throwable) { default }

/** Return a random [Double] value between the given two limits (inclusive min, exclusive max). */
fun random(min: Double, max: Double) = Math.random().let { min + (max - min) * it }

/**
 * Return this [Double] value if it lies between the two limits. If outside, return the
 * minimum/maximum value correspondingly.
 */
fun Double.minmax(minVal: Double, maxVal: Double) = min(max(this, minVal), maxVal)

/**
 * Return this [Int] value if it lies between the two limits. If outside, return the
 * minimum/maximum value correspondingly.
 */
fun Int.minmax(minVal: Int, maxVal: Int) = min(max(this, minVal), maxVal)

fun nextPowerOf2(x: Int): Int {
    return 1 shl (if (x == 0) 0 else 32 - Integer.numberOfLeadingZeros(x - 1))
}