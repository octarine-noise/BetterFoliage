package mods.betterfoliage.util

import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Invalidator {
    fun invalidate() {
        val iterator = callbacks.iterator()
        while(iterator.hasNext()) iterator.next().let { callback ->
            callback.get()?.invoke() ?: iterator.remove()
        }
    }
    val callbacks: MutableList<WeakReference<()->Unit>>
    fun onInvalidate(callback: ()->Unit) {
        callbacks.add(WeakReference(callback))
    }
}

class SimpleInvalidator : Invalidator {
    override val callbacks = mutableListOf<WeakReference<() -> Unit>>()
}

class LazyInvalidatable<V>(invalidator: Invalidator, val valueFactory: ()->V): ReadOnlyProperty<Any, V> {
    init { invalidator.onInvalidate { value = null } }

    var value: V? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        value?.let { return it }
        return synchronized(this) {
            value?.let { return it }
            valueFactory().apply { value = this }
        }
    }
}

open class LazyMapInvalidatable<K, V>(val invalidator: Invalidator, val valueFactory: (K)->V) {
    init { invalidator.onInvalidate { values.clear() } }

    val values = mutableMapOf<K, V>()

    operator fun get(key: K): V {
        values[key]?.let { return it }
        return synchronized(values) {
            values[key]?.let { return it }
            valueFactory(key).apply { values[key] = this }
        }
    }
    operator fun set(key: K, value: V) { values[key] = value }

    fun delegate(key: K) = Delegate(key)

    inner class Delegate(val key: K) : ReadOnlyProperty<Any, V> {
        init { invalidator.onInvalidate { cached = null } }

        private var cached: V? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): V {
            cached?.let { return it }
            get(key).let { cached = it; return it }
        }
    }
}
