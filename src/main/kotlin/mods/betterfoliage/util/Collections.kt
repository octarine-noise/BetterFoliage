package mods.betterfoliage.util

import com.google.common.collect.ImmutableList
import java.util.*

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

inline fun <T, C: Comparable<C>> Pair<T, T>.minBy(func: (T)->C) =
    if (func(first) < func(second)) first else second

inline fun <T, C: Comparable<C>> Pair<T, T>.maxBy(func: (T)->C) =
    if (func(first) > func(second)) first else second

inline fun <T, C: Comparable<C>> Triple<T, T, T>.maxValueBy(func: (T)->C): C {
    var result = func(first)
    func(second).let { if (it > result) result = it }
    func(third).let { if (it > result) result = it }
    return result
}

inline fun <reified T, reified R> Array<T>.mapArray(func: (T)->R) = Array<R>(size) { idx -> func(get(idx)) }

@Suppress("UNCHECKED_CAST")
inline fun <K, V> Map<K, V?>.filterValuesNotNull() = filterValues { it != null } as Map<K, V>

inline fun <reified T, R> Iterable<T>.findFirst(func: (T)->R?): R? {
    forEach { func(it)?.let { return it } }
    return null
}

inline fun <A1, reified A2, B> List<Pair<A1, B>>.filterIsInstanceFirst(cls: Class<A2>) = filter { it.first is A2 } as List<Pair<A2, B>>

/** Cross product of this [Iterable] with the parameter. */
fun <A, B> Iterable<A>.cross(other: Iterable<B>) = flatMap { a -> other.map { b -> a to b } }

inline fun <C, R, T> Iterable<T>.mapAs(transform: (C) -> R) = map { transform(it as C) }

inline fun <T1, T2> forEachNested(list1: Iterable<T1>, list2: Iterable<T2>, func: (T1, T2)-> Unit) =
    list1.forEach { e1 ->
        list2.forEach { e2 ->
            func(e1, e2)
        }
    }

/** Mutating version of _map_. Replace each element of the list with the result of the given transformation. */
inline fun <reified T> MutableList<T>.replace(transform: (T) -> T) = forEachIndexed { idx, value -> this[idx] = transform(value) }

/** Exchange the two elements of the list with the given indices */
inline fun <T> MutableList<T>.exchange(idx1: Int, idx2: Int) {
    val e = this[idx1]
    this[idx1] = this[idx2]
    this[idx2] = e
}

/** Return a random element from the array using the provided random generator */
inline operator fun <T> Array<T>.get(random: Random) = get(random.nextInt(Int.MAX_VALUE) % size)

inline fun Random.idx(array: Array<*>) = nextInt(Int.MAX_VALUE) % array.size
inline fun Random.idxOrNull(array: Array<*>, predicate: ()->Boolean) = if (predicate()) idx(array) else null

fun <T> Iterable<T>.toImmutableList() = ImmutableList.builder<T>().let { builder ->
    forEach { builder.add(it) }
    builder.build()
}