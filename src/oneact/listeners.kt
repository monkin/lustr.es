package oneact

import core.Param
import core.TouchEvent
import core.map
import core.param
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.UIEvent
import kotlin.browser.window
import kotlin.js.Promise

enum class EventPhase {
    CAPTURING_PHASE,
    BUBBLING_PHASE
}

private val trueByDefault = param(true)

private interface Listener {
    val target: EventTarget
    fun attach()
    fun detach()
    fun then(l: Listener) = CombinationListener(this, l)
}

private class CombinationListener(val l1: Listener, val l2: Listener) : Listener {
    override val target = l2.target
    override fun attach() {
        l1.attach()
        l2.attach()
    }
    override fun detach() {
        l1.detach()
        l2.detach()
    }
}

private class DomListener<T : Event>(override val target: EventTarget, val name: String, val phase: EventPhase = EventPhase.BUBBLING_PHASE, handler: (e: T) -> Unit) : Listener {
    @Suppress("UNCHECKED_CAST")
    val handler = handler as (e: Event) -> Unit
    var attached = false

    override fun attach() {
        if (!attached) {
            attached = true
            target.addEventListener(name, handler, phase === EventPhase.CAPTURING_PHASE)
        }
    }
    override fun detach() {
        if (attached) {
            attached = false
            target.removeEventListener(name, handler, phase === EventPhase.CAPTURING_PHASE)
        }
    }
}

private class NoneListener(override val target: EventTarget) : Listener {
    override fun detach() {}
    override fun attach() {}
}

interface ListenersBuilder {
    fun <T : Event> listen(name: String, phase: EventPhase, handler: (e: T) -> Unit)
    fun <T : Event> listen(name: String, handler: (e: T) -> Unit) = listen(name, EventPhase.BUBBLING_PHASE, handler)
}

fun listeners(target: EventTarget, children: El, active: Param<Boolean> = trueByDefault, listeners: ListenersBuilder.() -> Unit): El {
    var items: Listener = NoneListener(target)
    listeners(object : ListenersBuilder {
        override fun <T : Event> listen(name: String, phase: EventPhase, handler: (e: T) -> Unit) {
            val lower = name.toLowerCase()
            val trimmed = if (lower.startsWith("on"))  lower.substring(2) else lower
            items = items.then(DomListener(target, trimmed, phase, handler))
        }
    })
    var activated = active()
    if (activated) {
        items.attach()
    }
    return beforeDispose(beforeUpdate(children) {
        if (activated != active()) {
            activated = !activated
            if (activated) {
                items.attach()
            } else {
                items.detach()
            }
        }
    }) {
        if (activated) {
            items.detach()
        }
    }
}

fun windowListeners(children: El, active: Param<Boolean> = trueByDefault, listeners: ListenersBuilder.() -> Unit): El {
    return oneact.listeners(window, children, active, listeners)
}

fun nodeListeners(node: El, children: El = node, active: Param<Boolean> = trueByDefault, listeners: ListenersBuilder.() -> Unit): El {
    return oneact.listeners(node.node.only, children, active, listeners)
}

data class DragParameters(val x: Double, val y: Double, val bounds: DOMRect, val force: Double? = null)
typealias DragCallback = (e: DragParameters) -> Unit
interface DragListenersBuilder {
    fun onBeginDrag(callback: DragCallback)
    fun onEndDrag(callback: DragCallback)
    fun onDrag(callback: DragCallback)
    fun onlySelf()
}

private class DragState(
        val mouseStarted: Boolean = false,
        val touchStarted: Boolean = false,
        val touchId: Double = -1.0
) {
    val started
        get() = this.mouseStarted || this.touchStarted

    fun startTouch(cb: () -> Double) = if (!started) {
        DragState(false, true, cb())
    } else {
        this
    }

    fun startMouse(cb: () -> Unit) = if (!started) {
        cb()
        DragState(true)
    } else {
        this
    }

    fun processTouch(id: Double, cb: () -> Unit) {
        if (touchStarted && touchId == id) {
            cb()
        }
    }

    fun processMouse(cb: () -> Unit) {
        if (mouseStarted) {
            cb()
        }
    }

    fun endTouch(id: Double, cb: () -> Unit) = if (touchStarted && touchId == id) {
        cb()
        DragState()
    } else {
        this
    }

    fun endMouse(cb: () -> Unit) = if (mouseStarted) {
        cb()
        DragState()
    } else {
        this
    }
}

fun dragListeners(element: El, initListeners: DragListenersBuilder.() -> Unit): El {
    val node = element.node.only.toHtmlElement()
    var onBeginDrag: DragCallback = { }
    var onDrag: DragCallback = { }
    var onEndDrag: DragCallback = { }
    var onlySelf = false

    initListeners(object : DragListenersBuilder {
        override fun onBeginDrag(callback: DragCallback) {
            onBeginDrag = callback
        }
        override fun onEndDrag(callback: DragCallback) {
            onEndDrag = callback
        }
        override fun onDrag(callback: DragCallback) {
            onDrag = callback
        }
        override fun onlySelf() {
            onlySelf = true
        }
    })

    return state(DragState()) { getState, setState ->
        windowListeners(nodeListeners(element, element, param(true)) {
            listen("onMouseDown") { e: MouseEvent ->
                if (e.button == 0.toShort() && !e.defaultPrevented && (!onlySelf || e.target === e.currentTarget)) {
                    setState { it.startMouse {
                        val bounds = node.getBoundingClientRect()
                        val x = e.clientX.toDouble()
                        val y = e.clientY.toDouble()
                        val ev = DragParameters(x, y, bounds)
                        onBeginDrag(ev)
                        onDrag(ev)
                        node.focus()
                        e.preventDefault()
                    }}
                }
            }

            listen("onTouchStart") { e: TouchEvent ->
                if (e.changedTouches.length > 0 && !e.defaultPrevented  && (!onlySelf || e.target === e.currentTarget)) {
                    val touch = e.changedTouches.item(0)
                    setState { it.startTouch {
                        val bounds = node.getBoundingClientRect()
                        val x = touch.clientX
                        val y = touch.clientY
                        val force = touch.force
                        val ev = DragParameters(x, y, bounds, force)
                        onBeginDrag(ev)
                        onDrag(ev)
                        node.focus()
                        e.preventDefault()
                        touch.identifier
                    }}
                }
            }
        }, map(getState) { it.started }) {

            listen("onMouseMove") { e: MouseEvent ->
                getState().processMouse {
                    val bounds = node.getBoundingClientRect()
                    val x = e.clientX.toDouble()
                    val y = e.clientY.toDouble()
                    onDrag(DragParameters(x, y, bounds))
                }
            }

            listen("onMouseUp") { e: MouseEvent ->
                if (e.button == 0.toShort()) {
                    setState { it.endMouse {
                        val bounds = node.getBoundingClientRect()
                        val x = e.clientX.toDouble()
                        val y = e.clientY.toDouble()
                        val ev = DragParameters(x, y, bounds)
                        onDrag(ev)
                        onEndDrag(ev)
                        e.preventDefault()
                    }}
                }
            }

            listen("onTouchMove") { e: TouchEvent ->
                (0..(e.changedTouches.length.toInt() - 1)).map { e.changedTouches.item(it) }.map { touch ->
                    getState().processTouch(touch.identifier) {
                        val bounds = node.getBoundingClientRect()
                        val x = touch.clientX
                        val y = touch.clientY
                        val force = touch.force
                        onDrag(DragParameters(x, y, bounds, force))
                    }
                }
            }

            listen("onTouchEnd") { e: TouchEvent ->
                (0..(e.changedTouches.length.toInt() - 1)).map { e.changedTouches.item(it) }.map { touch ->
                    setState { it.endTouch(touch.identifier) {
                        val bounds = node.getBoundingClientRect()
                        val x = touch.clientX
                        val y = touch.clientY
                        val force = touch.force
                        val ev = DragParameters(x, y, bounds, force)
                        onDrag(ev)
                        onEndDrag(ev)
                    }}
                }
            }
        }
    }
}

fun outsideClickListener(el: El, handler: (e: UIEvent) -> Unit): El {
    val outsideClickHandler = { event: UIEvent ->
        val target = event.target.unsafeCast<Node>()
        if (el.node.all { node -> !node.contains(target) }) {
            handler(event)
        }
    }
    return windowListeners(el) {
        listen("onMouseDown", outsideClickHandler)
        listen("onTouchStart", outsideClickHandler)
    }
}

fun resizeListener(el: El, callback: (width: Double, height: Double) -> Unit): El {
    var size: Pair<Double, Double>? = null
    fun update() {
        val node = el.node.only as HTMLElement
        val width = node.offsetWidth.toDouble()
        val height = node.offsetHeight.toDouble()
        val newSize = Pair(width, height)
        if (newSize != size) {
            size = newSize
            callback(width, height)
        }
    }
    fun request() {
        window.setTimeout({ update() }, 250)
    }
    return afterMount(windowListeners(beforeUpdate(el) { update() }) {
        val handler: (Event) -> Unit = {
            update()
            request()
        }
        listen("onResize", handler)
        listen("onOrientationChange", handler)
    }) {
        update()
    }
}

fun withSize(callback: (Param<Pair<Double, Double>>) -> El) = state(Pair(0.0, 0.0)) { state, setState ->
    val el = callback(state)
    resizeListener(el) { width, height ->
        setState(Pair(width, height))
    }
}
