package core

import oneact.DragCallback
import kotlin.math.sign

@JsName("WeakMap")
external class WeakMap<in K, V>() {
    fun delete(key: K)
    fun has(key: K): Boolean
    fun get(key: K): V?
    fun set(key: K, value: V?)
}

fun <K, V> WeakMap<K, V>.get(key: K, compute: (key: K) -> V): V {
    val cached = get(key)
    return if (cached === null) {
        val computed = compute(key)
        set(key, computed)
        computed
    } else {
        cached
    }
}

private class CacheItem<T : Disposable>(val value: T) : Comparable<CacheItem<T>> {
    private companion object {
        var timer = 0.0
    }

    private var time = 0.0
    private var counter = 0
    val locked
        get () = counter > 0
    inline fun <X> lock(callback: (value: T) -> X): X {
        try {
            counter++
            return callback(value)
        } finally {
            counter--
            if (counter == 0) {
                time = CacheItem.timer++
            }
        }
    }
    override fun compareTo(other: CacheItem<T>): Int = if (counter != 0 || other.counter != 0) {
        other.counter - counter
    } else {
        sign(other.time - time).toInt()
    }
}

class DisposableCache<in Key, Value : Disposable>(val capacity: Int) : Disposable {
    private var storage = HashMap<Key, CacheItem<Value>>()

    fun <X> take(key: Key, create: () -> Value, callback: (value: Value) -> X): X {
        val item = storage.getOrPut(key) {  CacheItem(create()) }
        val result = item.lock(callback)
        clear()
        return result
    }

    private fun clear() {
        if (storage.size > capacity * 2) {
            val retain = storage.values.sorted().withIndex()
                    .takeWhile { (i, v) -> i < capacity || v.locked }
                    .map { it.value }
                    .toSet()
            storage = HashMap(storage.filter { (_, item) -> retain.contains(item) })
        }
    }
    override fun dispose() {
        storage.forEach { (_, value) -> value.value.dispose() }
    }
}