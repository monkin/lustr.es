package core


class ListNode<T>(val head: T, val tail: ListNode<T>? = null) : Iterable<T> {
    val length: Int = (tail?.length ?: 0) + 1

    companion object {
        fun <T> fromArray(array: Array<T>) = array.reversed().fold<T, ImmutableList<T>>(null) { r, v ->
            ListNode(v, r)
        }
        fun <T> fromIterable(iterable: Iterable<T>) = iterable.reversed().fold<T, ImmutableList<T>>(null) { r, v ->
            ListNode(v, r)
        }

        fun <T> toJson(list: ImmutableList<T>, itemToJson: (T) -> dynamic = { v -> v}) = js("[]").also { out ->
            list?.forEach { v ->
                out.push(itemToJson(v))
                Unit
            }
        }

        fun <T> fromJson(json: dynamic, itemFromJson: (v: dynamic) -> T = { v -> v.unsafeCast<T>() }) =
            (0 until json.length.unsafeCast<Int>()).reversed().fold<dynamic, ImmutableList<T>>(null, { r, i ->
                ListNode(itemFromJson(json[i]), r)
            })
    }

    override fun iterator() = object : Iterator<T> {
        private var pointer: ListNode<T>? = this@ListNode

        override fun next(): T = pointer.let { p ->
            when {
                p !== null -> {
                    pointer = p.tail
                    p.head
                }
                else -> throw NoSuchElementException()
            }
        }

        override fun hasNext() = pointer !== null
    }
}

typealias ImmutableList<T> = ListNode<T>?

operator fun <T> ImmutableList<T>.plus(list: ImmutableList<T>): ImmutableList<T> = when {
    this === null -> list
    list === null -> this
    else -> ListNode(this.head, this.tail + list)
}

inline fun <T> ImmutableList<T>.eachNode(f: (node: ListNode<T>) -> Unit) {
    var i: ImmutableList<T> = this
    while (i !== null) {
        f(i)
        i = i.tail
    }
}

fun <T> immutableListOf(value: T): ImmutableList<T> = ListNode(value)
fun <T> immutableListOf(vararg value: T) =
    value.foldRight(null as ImmutableList<T>) { v, r -> ListNode(v, r) }

fun <T> ImmutableList<T>.size() = this?.length ?: 0

fun <T> ImmutableList<T>.toList() = ArrayList<T>().also { list ->
    if (this !== null) {
        this.eachNode { n ->
            list.add(n.head)
        }
    }
}

