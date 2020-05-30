package editor.ui

import art.artCanvas
import core.*
import draw.*
import draw.Touch
import editor.state.*
import kotlinx.css.flexGrow
import kotlinx.css.flexShrink
import material.exponentialSlider
import material.label
import material.slider
import oneact.*
import state.Store
import kotlin.math.PI
import kotlin.math.sin

private val pencilViewClass = cl("pencil-view")

private val styled = style {
    ".$pencilViewClass" {
        flexGrow = 1.0
        flexShrink = 1.0
    }
}

private fun curve(width: Double, height: Double): JsArray<Touch> {
    val totalTime = Vector(width, height).length
    return JsArray<Touch>().also { out ->
        for (i in 0..500) {
            val t = i.toDouble() / 500.0
            out += Touch(
                    point = Vector(sin(t * 3 * PI) * width * 0.3 * (1 - t * 0.3) + width / 2, height * 0.1 + t * height * 0.8),
                    force = 1 - t * t,
                    time = totalTime * t
            )
        }
    }
}

fun pencilInput(
        setSize: (Double) -> Unit,
        setHardness: (Double) -> Unit,
        setFlow: (Double) -> Unit,
        setOpacity: (Double) -> Unit,
        size: Param<Double>,
        hardness: Param<Double>,
        flow: Param<Double>,
        opacity: Param<Double>,
        isActive: Param<Boolean>
): El {
    return styled(children(label(
            text = param("Size"),
            note = map(size) { it.toReadable() },
            content = exponentialSlider(
                    min = param(1.0),
                    max = param(200.0),
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
            text = param("Flow"),
            note = map(flow) { (it * 100).toReadable() },
            content = slider(
                    min = param(0.0),
                    max = param(1.0),
                    value = flow,
                    onChange = setFlow
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
    ), canvas(param(pencilViewClass), isActive) { gl ->
        { width, height ->
            gl.artCanvas(width, height) { art ->
                val bounds = Bounds(0.0, 0.0, width.toDouble(), height.toDouble())
                gl.settings()
                        .blend(false)
                        .viewport(0, 0, width, height)
                        .apply {
                            art.clean().pencil(
                                    path = curve(width.toDouble(), height.toDouble()),
                                    flow = flow(),
                                    hardness = hardness(),
                                    size = size(),
                                    color = Color.LinearColor(0.0, 0.0, 0.0),
                                    opacity = opacity()
                            ).draw(bounds, bounds) { color ->
                                toSRgb(blend(color, float4(1.0)))
                            }
                        }
            }
        }
    }))
}

fun pencilInputConnected(store: Store<LustresState>): El {
    val state = store.state
    return pencilInput(
            setSize = { store.dispatch(PencilAction.SetSize(it)) },
            setOpacity = { store.dispatch(PencilAction.SetOpacity(it)) },
            setHardness = { store.dispatch(PencilAction.SetHardness(it)) },
            setFlow = { store.dispatch(PencilAction.SetFlow(it)) },
            size = map(state) { selectPencilSize(it) },
            opacity = map(state) { selectPencilOpacity(it) },
            hardness = map(state) { selectPencilHardness(it) },
            flow = map(state) { selectPencilFlow(it) },
            isActive = map(state) { selectMenuTab(it) == MenuTab.PENCIL }
    )
}