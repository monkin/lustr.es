@file:Suppress("NOTHING_TO_INLINE")

package oneact

import core.Action
import core.Param
import core.param
import org.w3c.dom.events.Event
import kotlin.browser.document

sealed class Child {
    data class Text(val value: Param<String>) : Child()
    data class Attribute(val name: String, val value: Param<String?>) : Child()
    data class Handler<in T : Event>(val name: String, val handler: (event: T) -> Unit) : Child() {
        inline operator fun invoke(e: Event) {
            @Suppress("UNCHECKED_CAST")
            handler(e as T)
        }
    }
    abstract class Element : Child() {
        abstract val node: Nodes
        abstract val dispose: Action
        abstract val update: Action
        fun remove() {
            dispose()
            node.remove()
        }
    }
}

typealias El = Child.Element

private class ElementImplementation(val name: String, val namespace: String?, vararg children: Child) : Child.Element() {
    override val node = Nodes.Single(if (namespace != null) document.createElementNS(namespace, name) else document.createElement(name))
    override val dispose = children.fold<Child, Action>(Action.Noop) { r, v -> when (v) {
        is Child.Element -> r.then(v.dispose)
        else -> r
    }}
    override val update = children.fold<Child, Action>(Action.Noop) { r, v -> when (v) {
        is Child.Element -> {
            v.node.appendTo(node.only)
            r.then(v.update)
        }
        is Child.Text -> if (v.value is Param.Function) {
            val compute = v.value.compute
            var value = compute()
            val text = document.createTextNode(value)
            node.only.appendChild(text)
            r.then {
                val newValue = compute()
                if (newValue != value) {
                    value = newValue
                    text.nodeValue = value
                }
            }
        } else {
            node.only.appendChild(document.createTextNode(v.value()))
            r
        }
        is Child.Attribute -> if (v.value is Param.Function) {
            val compute = v.value.compute
            val name = v.name
            var value = compute()
            setAttribute(name, value)
            r.then {
                val newValue = compute()
                if (newValue != value) {
                    value = newValue
                    setAttribute(name, value)
                }
            }
        } else {
            setAttribute(v.name, v.value())
            r
        }
        is Child.Handler<*> -> {
            val lowName = v.name.toLowerCase()
            val trimName = if (lowName.startsWith("on")) lowName.substring(2) else lowName
            (node.only as org.w3c.dom.Element).addEventListener(trimName, { e -> v(e) })
            r
        }
    }}

    private fun setAttribute(name: String, value: String?) {
        val el = node.only.toHtmlElement()
        if (value === null) {
            el.removeAttribute(name)
        } else if (name != "value" || el.getAttribute(name) != value) {
            el.setAttribute(name, value)
        }
    }
}

inline fun attr(name: String, value: String?) = Child.Attribute(name, param(value))
inline fun attr(name: String, noinline value: () -> String?) = Child.Attribute(name, param(value))
inline fun attr(name: String, value: Param<String?>) = Child.Attribute(name, value)
inline fun className(c: String) = attr("class", c);
inline fun className(noinline c: ClassesBuilder.() -> Unit) = attr("class", classes(c))

inline fun text(value: String) = Child.Text(param(value))
inline fun text(noinline value: () -> String) = Child.Text(param(value))
inline fun text(value: Param<String>) = Child.Text(value)

inline fun <T : Event> handler(name: String, noinline handler: (event: T) -> Unit) = Child.Handler<T>(name, handler)

/**
 * Create HTML element
 */
fun el(name: String, vararg children: Child): El = ElementImplementation(name, null, *children)

/**
 * Create 'div' element
 */
fun el(vararg children: Child): El = ElementImplementation("div", null, *children)

/**
 * Create SVG element
 */
fun svg(name: String, vararg children: Child): El = ElementImplementation(name, "http://www.w3.org/2000/svg", *children)

fun children(vararg children: El, wrap: (children: El) -> El = { el -> el }): El {
    val dispose = children.fold<El, Action>(Action.Noop, { r, e -> r.then(e.dispose) })
    val update = children.fold<El, Action>(Action.Noop, { r, e -> r.then(e.update) })
    val node = combine(children.map<El, Nodes> { it.node })
    val wrapped = wrap(object : El() {
        override val node = node
        override val update = Action.Noop
        override val dispose = Action.Noop
    })
    return object : El() {
        override val node = wrapped.node
        override val update = update.then(wrapped.update)
        override val dispose = dispose.then(wrapped.dispose)
    }
}