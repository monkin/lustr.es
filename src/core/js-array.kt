package core

@Suppress("NOTHING_TO_INLINE")
class JsArray<T> {
    var data: dynamic

    companion object {
        private fun <T> withData(data: dynamic) = JsArray<T>().also {
            it.data = data
        }
    }

    constructor() {
        data = js("[]")
    }

    constructor(items: Array<T>) {
        data = js("[]")
        items.forEach { v ->
            data.push(v)
            Unit
        }
    }

    constructor(items: Iterable<T>) {
        data = js("[]")
        items.forEach { v ->
            data.push(v)
            Unit
        }
    }

    inline operator fun get(i: Int)  = data[i].unsafeCast<T>()
    inline operator fun set(i: Int, value: T) {
        data[i] = value
    }
    inline fun forEach(noinline callback: (T) -> Unit) {
        data.forEach(callback)
    }
    inline fun forEach(crossinline callback: (Int, T) -> Unit) {
        data.forEach { v: T, i: Int -> callback(i, v) }
    }
    fun <R> map(callback: (T) -> R) = withData<R>(data.map(callback))
    fun <R> map(callback: (Int, T) -> R) = withData<R>(data.map { v: T, i: Int -> callback(i, v)})
    fun filter(callback: (T) -> Boolean) = withData<T>(data.filter(callback))
    fun filter(callback: (Int, T) -> Boolean) = withData<T>(data.filter { v: T, i: Int -> callback(i, v) })
    fun slice(i1: Int, i2: Int) = withData<T>(data.slice(i1, i2))
    operator fun plus(value: T) = withData<T>(data.slice().let { copy ->
        copy.push(value)
    })
    inline operator fun plusAssign(value: T) {
        data.push(value)
    }
    operator fun plus(value: JsArray<T>) = withData<T>(data.concat(value.data))
    inline operator fun plusAssign(value: JsArray<T>) {
        data.push["apply"](data, value.data)
    }
    inline val size
        get() = data.length.unsafeCast<Int>()
    inline val lastIndex
        get() = size - 1

    inline fun first() = data[0].unsafeCast<T>()
    inline fun last() = data[lastIndex].unsafeCast<T>()
    fun <A> fold(initial: A, f: (A, T) -> A) = data.reduce(f, initial).unsafeCast<A>()
    fun some(condition: (T) -> Boolean) = data.some(condition).unsafeCast<Boolean>()
    fun every(condition: (T) -> Boolean) = data.every(condition).unsafeCast<Boolean>()
    fun isEmpty() = size == 0
    fun isNotEmpty() = !isEmpty()
    fun reversed() = withData<T>(data.slice().reverse())

    inline fun add(value: T): JsArray<T> {
        data.push(value)
        return this
    }

    fun clear() {
        data = js("[]")
    }

    fun clone() = withData<T>(data.slice())

    fun toArray() = data.slice().unsafeCast<Array<T>>()

    inline fun join(delimeter: String = "") = data.join(delimeter).unsafeCast<String>()
}