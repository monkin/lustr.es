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

private val brushViewClass = cl("brush-view")

private val styled = style {
    ".$brushViewClass" {
        flexGrow = 1.0
        flexShrink = 1.0
    }
}

fun easeInOutQuad(t: Double) = when {
    t <= 0 -> 0.0
    t >= 1 -> 1.0
    t < 0.5 -> 2.0 * t * t
    else -> (4.0 - 2.0 * t) * t - 1
}

private fun curve(width: Double, height: Double): JsArray<Touch> {
    val totalTime = Vector(width, height).length
    return JsArray<Touch>().also { out ->
        for (i in 0..500) {
            val t = i.toDouble() / 500.0
            out += Touch(
                    point = Vector(sin(t * 3 * PI) * width * 0.3 * (1 - t * 0.3) + width / 2, height * 0.1 + t * height * 0.8),
                    force = ((1 - t * t) * easeInOutQuad(t * 10.0)) * 0.75,
                    time = totalTime * t
            )
        }
    }
}

fun brushInput(
        size: Param<Double>,
        density: Param<Double>,
        sharpness: Param<Double>,
        opacity: Param<Double>,
        setSize: (Double) -> Unit,
        setDensity: (Double) -> Unit,
        setSharpness: (Double) -> Unit,
        setOpacity: (Double) -> Unit,
        active: Param<Boolean>
): El {
    return styled(children(label(
            text = param("Size"),
            note = map(size) { it.toReadable() },
            content = exponentialSlider(
                    min = param(10.0),
                    max = param(200.0),
                    value = size,
                    onChange = setSize
            )
    ), label(
            text = param("Density"),
            note = map(density) { (it * 100).toReadable() },
            content = slider(
                    min = param(0.0),
                    max = param(1.0),
                    value = density,
                    onChange = setDensity
            )
    ), label(
            text = param("Sharpness"),
            note = map(sharpness) { (it * 100).toReadable() },
            content = slider(
                    min = param(0.0),
                    max = param(1.0),
                    value = sharpness,
                    onChange = setSharpness
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
    ), canvas(param(brushViewClass), active) { gl ->
        { width, height ->
            gl.artCanvas(width, height) { art ->
                val bounds = Bounds(0.0, 0.0, width.toDouble(), height.toDouble())
                gl.settings()
                        .blend(false)
                        .viewport(0, 0, width, height)
                        .apply {
                            art.clean().brush(
                                    path = curve(width.toDouble(), height.toDouble()),
                                    sharpness = sharpness(),
                                    size = size(),
                                    density = density(),
                                    color = Color.LinearColor(0.0, 0.0, 0.0),
                                    opacity = opacity(),
                                    seed = 0
                            ).draw(bounds, bounds) { color ->
                                toSRgb(blend(color, float4(1.0)))
                            }
                        }
            }
        }
    }))
}

fun brushInputConnected(): El {
    val store = Context.get<Store<LustresState>>()
    val state = store.state
    return brushInput(
            size = map(state) { selectBrushSize(it) },
            opacity = map(state) { selectBrushOpacity(it) },
            density = map(state) { selectBrushDensity(it) },
            sharpness = map(state) { selectBrushSharpness(it) },
            active = map(state) { selectMenuTab(it) == MenuTab.BRUSH },
            setSize = { store.dispatch(BrushAction.SetSize(it)) },
            setSharpness = { store.dispatch(BrushAction.SetSharpness(it)) },
            setOpacity = { store.dispatch(BrushAction.SetOpacity(it)) },
            setDensity = { store.dispatch(BrushAction.SetDensity(it)) }
    )
}