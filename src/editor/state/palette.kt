package editor.state

import draw.Color
import state.Action
import state.selector

private val defaultColors: Array<Color> = arrayOf(
        Color.RgbColor(0.0, 0.0, 0.0),
        Color.RgbColor(1.0, 1.0, 1.0)
) + (0..9).map { i ->
    Color.HsvColor(i.toDouble() / 10.0, 0.75, 0.75).toRgb()
}

class Palette(
        val color: Color = Color.RgbColor(0.0, 0.0, 0.0),
        val items: Array<Color> = defaultColors
) {
    fun copy(color: Color = this.color, items: Array<Color> = this.items) = Palette(color, items)
}

sealed class PaletteAction : Action<LustresState, Palette> {
    data class SetColor(val color: Color) : PaletteAction() {
        override fun apply(state: Palette) = state.copy(color = color)
    }
    data class UseColor(val color: Color) : PaletteAction() {
        override fun apply(state: Palette) = if (state.items.isNotEmpty() && color == state.items.first()) {
            state
        } else {
            state.copy(items = (arrayOf(color) + state.items.filter { it != color }).take(12).toTypedArray())
        }
    }

    override fun read(state: LustresState) = state.palette
    override fun write(state: LustresState, local: Palette) = state.copy(palette = local)
}

val selectPaletteState = selector<LustresState, Palette> { it.palette }
val selectColor = selector(selectPaletteState) { it.color }
val selectPalette = selector(selectPaletteState) { it.items }

