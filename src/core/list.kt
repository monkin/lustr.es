package core

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = ImmutableListSerializer::class)
class ListNode<T>(val head: T, val tail: ListNode<T>? = null) : Iterable<T> {
    val length: Int = (tail?.length ?: 0) + 1

    companion object {
        fun <T> fromArray(array: Array<T>) = array.reversed().fold<T, ImmutableList<T>>(null) { r, v ->
            ListNode(v, r)
        }
        fun <T> fromIterable(iterable: Iterable<T>) = iterable.reversed().fold<T, ImmutableList<T>>(null) { r, v ->
            ListNode(v, r)
        }
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

fun <T> ImmutableList<T>.toList(): List<T> = ArrayList<T>().also { list ->
    if (this !== null) {
        this.eachNode { n ->
            list.add(n.head)
        }
    }
}

@Serializer(forClass = ListNode::class)
class ImmutableListSerializer<T>(element: KSerializer<T>) : KSerializer<ImmutableList<T>> {
    private val serializer = ListSerializer(element)

    override fun deserialize(decoder: Decoder): ImmutableList<T> = ImmutableList.fromIterable(serializer.deserialize(decoder))

    override val descriptor: SerialDescriptor = SerialDescriptor("ImmutableList", StructureKind.LIST)

    override fun serialize(encoder: Encoder, value: ImmutableList<T>) {
        serializer.serialize(encoder, value.toList())
    }
}

