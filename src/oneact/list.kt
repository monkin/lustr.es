package oneact

import core.*
import kotlin.browser.document

private data class MappingItem<V, R>(var i: Int, var v: V, var el: R?)

@Suppress("NOTHING_TO_INLINE")
private class JsMap<K, V>() {
    private val storage: dynamic = js("new Map()")

    inline fun containsKey(key: K) = storage.has(key).unsafeCast<Boolean>()
    inline fun getValue(key: K) = storage.get(key).unsafeCast<V>()
    inline operator fun get(key: K) = getValue(key)
    inline operator fun set(key: K, value: V) {
        storage.set(key, value)
    }
    fun forEach(callback: (value: V, key: K) -> Unit) {
        storage.forEach(callback)
    }
}

private class ListMapping<V, R>(
        private val key: (index: Int, value: V) -> String,
        private val create: (index: Param<Int>, value: Param<V>) -> R,
        private val update: (item: R) -> Unit,
        private val insert: (previous: R?, item: R) -> Unit,
        private val remove: (item: R) -> Unit
) {
    private var items = JsMap<String, MappingItem<V, R>>()
    private var list = JsArray<V>()
    private val listParam = param { list }
    fun update(list: JsArray<V>) {
        this.list = list
        val newItems = JsMap<String, MappingItem<V, R>>()
        list.forEach { i, v ->
            val k = key(i, v)
            if (this.items.containsKey(k)) {
                val item = this.items.getValue(k)
                item.i = i
                item.v = v
                val el = item.el
                if (el !== null) {
                    update(el)
                }
                newItems[k] = item
            } else {
                val item = MappingItem<V, R>(i, v, null)
                item.el = create(param { item.i }, param { item.v })
                newItems[k] = item
            }
        }

        items.forEach { value, key ->
            if (!newItems.containsKey(key)) {
                val el = value.el
                if (el !== null) {
                    remove(el)
                }
            }
        }

        items = newItems
        var previous: R? = null
        items.forEach { value, _ ->
            val el = value.el
            if (el != null) {
                insert(previous, el)
                previous = value.el
            }
        }
    }
    fun forEach(callback: (value: R) -> Unit) {
        items.forEach { value, _ ->
            val el = value.el
            if (el != null) {
                callback(el)
            }
        }
    }
}

fun <T> list(
        data: Param<Collection<T>>,
        key: (i: Int, v: T) -> String,
        render: (index: Param<Int>, item: Param<T>) -> El
): El {
    val context = Context.context()
    if (data is Param.Value) {
        val list = ArrayList(data())
        return children(*list.mapIndexed { i, v -> render(param(i), param(v)) }.toTypedArray())
    } else {
        val fragment = document.createDocumentFragment()
        val node = Nodes.Range(fragment.appendChild(document.createComment("list")),
                fragment.appendChild(document.createComment("/list")))

        val mapping = ListMapping<T, El>(
                key = key,
                create = context.bind(render),
                update = { el -> el.update() },
                insert = { previous: El?, item: El ->
                    if (previous === null) {
                        item.node.insertAfter(node.first)
                    } else {
                        item.node.insertAfter(previous.node.last)
                    }
                },
                remove = { el -> el.remove() }
        )

        mapping.update(JsArray(data()))

        return object : El() {
            override val node = node
            override val dispose = action {
                mapping.forEach { it.dispose() }
            }
            override val update = action {
                mapping.update(JsArray(data()))
            }
        }
    }
}

fun <T> list(data: Param<Collection<T>>,
             render: (index: Param<Int>, item: Param<T>) -> El): El {
    return list(data, { _, i -> i.toString() }, { i, v -> render(i, v) })
}

fun <T> list(data: Param<Collection<T>>,
             key: (v: T) -> String,
             render: (index: Param<Int>, item: Param<T>) -> El): El {
    return list(data, { _, v -> key(v) }, { i, v -> render(i, v) })
}

