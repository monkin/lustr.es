package oneact

import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import kotlin.browser.document

inline fun Node.toHtmlElement(): HTMLElement {
    @Suppress("UnsafeCastFromDynamic")
    return this.asDynamic()
}

sealed class Nodes: Sequence<Node> {

    class Single(private val node: Node): Nodes() {
        override val first: Node
            get() = node
        override val last: Node
            get() = node
        override val only: Node
            get() = node

        override fun iterator(): Iterator<Node> {
            return generateSequence { node }.take(1).iterator()
        }
    }

    class Range(override val first: Node, override val last: Node): Nodes() {
        override val only: Node
            get() = throw Error("Can't convert nodes range to single node")

        override fun iterator(): Iterator<Node> {
            var i: Node? = this.first
            return object : Iterator<Node> {
                override fun hasNext(): Boolean {
                    return i !== null && i !== last.nextSibling
                }
                override fun next(): Node {
                    if (i !== null && i !== last.nextSibling) {
                        val result = i!!
                        i = result.nextSibling
                        return result
                    } else {
                        throw NoSuchElementException()
                    }
                }
            }
        }
    }

    abstract val first: Node
    abstract val last: Node
    abstract val only: Node
    val parent: Node
        get() = first.parentNode!!

    fun appendTo(node: Node): Nodes {
        forEach { node.appendChild(it) }
        return this
    }
    fun prependTo(node: Node): Nodes {
        val ref = node.firstChild
        forEach { node.insertBefore(it, ref) }
        return this
    }

    fun insertBefore(node: Node): Nodes {
        val p = node.parentNode!!
        if (node.previousSibling != last) {
            forEach { p.insertBefore(it, node) }
        }
        return this
    }

    fun insertAfter(node: Node): Nodes {
        val p = node.parentNode!!
        val next = node.nextSibling
        if (next != first) {
            forEach { p.insertBefore(it, next) }
        }
        return this
    }

    fun remove(): Nodes {
        val fragment = document.createDocumentFragment()
        forEach { fragment.appendChild(it) }
        return this
    }
}

fun combine(nodes: Collection<Nodes>): Nodes {
    return if (nodes.isNotEmpty()) {
        val fragment = document.createDocumentFragment()
        nodes.forEach { it.appendTo(fragment) }
        Nodes.Range(fragment.firstChild!!, fragment.lastChild!!)
    } else {
        Nodes.Single(document.createComment("empty combination"))
    }
}
