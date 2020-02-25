package core

import kotlin.math.sign

private data class PoolItem<Value : Disposable>(val value: Value, val time: Double) : Comparable<PoolItem<Value>> {
    override fun compareTo(other: PoolItem<Value>) = sign(other.time - time).toInt()
}

class Pool<in Key, Value : Disposable>(private val capacity: Int) {
    private var storage = JsMap<ArrayList<PoolItem<Value>>>()
    private var size = 0
    private var counter = 0.0

    fun <X> take(key: Key, create: () -> Value, callback: (value: Value) -> X): X {
        val list = storage.getOrPut(key.toString()) { ArrayList() }
        val item = if (list.isEmpty()) {
            create()
        } else {
            val i = list.size - 1
            val v = list[i]
            list.removeAt(i)
            size--
            v.value
        }

        try {
            return callback(item)
        } finally {
            list.add(PoolItem(item, counter++))
            size++

            if (size > capacity * 2) {
                val retain = storage.values.flatten().sorted().take(capacity).toList()

                val newStorage = JsMap<ArrayList<PoolItem<Value>>>()
                storage.keys.forEach { k ->
                    val filtered = storage[k]!!.filter { item -> retain.contains(item) }
                    if (filtered.isNotEmpty()) {
                        newStorage[k] = ArrayList(filtered)
                    }
                }
                storage = newStorage
            }
        }
    }
}