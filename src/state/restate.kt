package state

import core.Param
import core.param

typealias Operation<State> = (context: Context<State>) -> Unit


interface Context<State> {
    val state: Param<State>
    fun dispatch(action: Action<State, *>)
    fun dispatch(operation: Operation<State>) = operation(this)
}

interface Action<GlobalState, LocalState> {
    fun read(state: GlobalState): LocalState
    fun write(state: GlobalState, local: LocalState): GlobalState
    fun apply(state: LocalState): LocalState
    fun apply(state: GlobalState): GlobalState {
        val old = read(state)
        val new = apply(old)
        return if (old !== new) {
            write(state, new)
        } else {
            state
        }
    }
}

class Store<State>(
        initial: State,
        private val listener: (state: State) -> Unit
): Context<State> {
    private var content = initial
    init { listener(content) }

    override val state
        get() = param { content }

    override fun dispatch(action: Action<State, *>) {
        val updated = action.apply(content)
        // console.log("Dispatch:", action, " -> ", updated)
        if (updated !== content) {
            content = updated
            listener(content)
        }
    }
}
