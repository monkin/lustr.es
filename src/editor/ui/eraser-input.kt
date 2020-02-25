package editor.ui

import core.Param
import core.map
import core.param
import core.toReadable
import editor.state.*
import kotlinx.css.flexGrow
import kotlinx.css.flexShrink
import material.exponentialSlider
import material.label
import material.slider
import oneact.*
import state.Store

private val eraserViewClass = cl("eraser-view")

private val styled = style {
    ".$eraserViewClass" {
        flexGrow = 1.0
        flexShrink = 1.0
    }
}

fun eraserInput(
        setSize: (Double) -> Unit,
        setHardness: (Double) -> Unit,
        setOpacity: (Double) -> Unit,
        size: Param<Double>,
        hardness: Param<Double>,
        opacity: Param<Double>,
        isActive: Param<Boolean>
): El {
    return styled(children(label(
            text = param("Size"),
            note = map(size) { it.toReadable() },
            content = exponentialSlider(
                    min = param(5.0),
                    max = param(100.0),
                    value = size,
                    onChange = setSize
            )
    ), label(
            text = param("Hardness"),
            note = map(hardness) { (it * 100).toReadable() },
            content = slider(
                    min = param(0.0),
                    max = param(1.0),
                    value = hardness,
                    onChange = setHardness
            )
    ), label(
            text = param("Opacity"),
            note = map(opacity) { (it * 100).toReadable() },
            content = slider(
                    min = param(0.0),
                    max = param(1.0),
                    value = opacity,
                    onChange = setOpacity
            )
    ), canvas(param(eraserViewClass), isActive) { _ ->
        { _, _ ->

        }
    }))
}

fun eraserInputConnected(): El {
    val store = Context.get<Store<LustresState>>()
    val state = store.state
    return eraserInput(
            setSize = { store.dispatch(EraserAction.SetSize(it)) },
            setOpacity = { store.dispatch(EraserAction.SetOpacity(it)) },
            setHardness = { store.dispatch(EraserAction.SetHardness(it)) },
            size = map(state) { selectEraserSize(it) },
            opacity = map(state) { selectEraserOpacity(it) },
            hardness = map(state) { selectEraserHardness(it) },
            isActive = map(state) { selectMenuTab(it) == MenuTab.ERASER }
    )
}