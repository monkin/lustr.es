@file:Suppress("NOTHING_TO_INLINE")

package core

sealed class Action {
    abstract operator fun invoke()
    abstract fun then(action: Action): Action

    inline fun then(noinline callback: () -> Unit) = then(action(callback))

    class Function(val callback: () -> Unit): Action() {
        override fun invoke() = callback()
        override fun then(action: Action): Action = when (action) {
            is Noop -> this
            is Function -> Function {
                callback()
                action.callback()
            }
        }
    }

    object Noop : Action() {
        override fun invoke() {
            // nothing to do there
        }
        override fun then(action: Action) = action
    }
}

fun action(callback: () -> Unit): Action = Action.Function(callback)
