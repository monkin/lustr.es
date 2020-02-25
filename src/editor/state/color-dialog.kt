package editor.state

import state.Action
import state.selector

sealed class ColorDialogAction : Action<LustresState, Boolean> {

    object Open : ColorDialogAction() {
        override fun apply(state: Boolean) = true
    }
    object Close : ColorDialogAction() {
        override fun apply(state: Boolean) = false
    }

    override fun read(state: LustresState) = state.isColorDialogOpen
    override fun write(state: LustresState, local: Boolean) = state.copy(isColorDialogOpen = local)
}

val selectIsColorDialogOpen = selector<LustresState, Boolean> { it.isColorDialogOpen }