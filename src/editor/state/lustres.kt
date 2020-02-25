package editor.state

import state.Store

data class LustresState(
        val menu: MenuState = MenuState(),
        val spray: Tool.Spray = Tool.Spray(),
        val eraser: Tool.Eraser = Tool.Eraser(),
        val brush: Tool.Brush = Tool.Brush(),
        val pencil: Tool.Pencil = Tool.Pencil(),
        val document: Document? = null,
        val orientation: Orientation = Orientation(),
        val drawStream: DrawStream? = null,
        val palette: Palette = Palette(),
        val isColorDialogOpen: Boolean = false
)

fun createLustresStore(handleChange: (state: LustresState) -> Unit): Store<LustresState> {
    return Store(LustresState(), handleChange)
}