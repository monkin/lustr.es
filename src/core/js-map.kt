@file:Suppress("NOTHING_TO_INLINE")

package core

import kotlinext.js.Object

class JsMap<V> {
    val storage = js("{}")
    inline operator fun get(key: String) = (storage[key] ?: null).unsafeCast<V?>()
    inline operator fun set(key: String, value: V) {
        storage[key] = value
    }
    inline fun has(key: String) = storage.hasOwnProperty(key).unsafeCast<Boolean>()
    inline fun remove(key: String) {
        storage[key] = null
    }

    fun getOrPut(key: String, create: () -> V) = get(key) ?: create().also { v ->
        set(key, v)
    }

    val keys
            get() = Object.keys(storage.unsafeCast<Any>())
    val values
            get() = Object.keys(storage.unsafeCast<Any>()).map { key -> storage[key].unsafeCast<V>() }
}