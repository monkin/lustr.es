package oneact

import core.Param
import core.action
import core.param
import kotlin.browser.window
import kotlin.js.Promise

private class DirtySet {
    private var items = js("new Set()")
    private var inUpdate = false

    operator fun invoke() {
        try {
            inUpdate = true
            val items = items
            this.items = js("new Set()")
            items.forEach { f -> f() }
        } finally {
            Promise.resolve(Unit).then {
                if (isEmpty) {
                    inUpdate = false
                    requested = false
                } else {
                    invoke()
                }
            }
        }
    }

    var requested = false
    fun append(item: () -> Unit) {
        items.add(item)
        if (!inUpdate && !requested) {
            requested = true
            window.requestAnimationFrame { invoke() }
        }
    }

    val isEmpty
        get() = items.size == 0
    val isUpdating
        get() = inUpdate
}


private val dirty = DirtySet()


interface StateWriter<T> {
    operator fun invoke(value: T): T
    operator fun invoke(update: (value: T) -> T): T
}

private class State<T>(initial: T, private val onchange: () -> Unit) : StateWriter<T> {
    private var current = initial
    private var next = initial

    override fun invoke(value: T): T {
        next = value
        if (next != current) {
            onchange()
        }
        return next
    }

    override fun invoke(update: (value: T) -> T): T = this(update(next))

    val getter = param { current }

    fun commit() {
        current = next
    }
}

private val doNothing = {}

fun <T> state(initial: T, render: (state: Param<T>, write: StateWriter<T>) -> El): El {
    var updater = doNothing
    var disposed = false
    val state = State(initial) {
        dirty.append(updater)
    }
    val child = render(state.getter, state)
    updater = {
        if (!disposed) {
            if (dirty.isUpdating) {
                state.commit()
            }
            child.update()
        }
    }
    return object : El() {
        override val node = child.node
        override val update = action(updater)
        override val dispose = child.dispose.then { disposed = true }
    }
}

