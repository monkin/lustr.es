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

private val sprayViewClass = cl("spray-view")

private val styled = style {
    ".$sprayViewClass" {
        flexGrow = 1.0
        flexShrink = 1.0
    }
}

private fun curve(width: Double, height: Double): JsArray<Touch> {
    val totalTime = Vector(width, height).length
    val random = Random()
    return JsArray<Touch>().also { out ->
        for (i in 0..500) {
            val t = i.toDouble() / 500.0
            out += Touch(
                    point = Vector(sin(t * 3 * PI) * width * 0.3 * (1 - t * 0.3) + width / 2, height * 0.1 + t * height * 0.8) + random.nextDirection() * 0.5,
                    force = 1 - t * t,
                    time = totalTime * t
            )
        }
    }
}

fun sprayInput(
        setSize: (Double) -> Unit,
        setIntensity: (Double) -> Unit,
        setGranularity: (Double) -> Unit,
        setOpacity: (Double) -> Unit,
        dispersion: Param<Double>,
        intensity: Param<Double>,
        granularity: Param<Double>,
        opacity: Param<Double>,
        active: Param<Boolean>
): El {
    return styled(children(label(
            text = param("Dispersion"),
            note = map(dispersion) { it.toReadable() },
            content = exponentialSlider(
                    min = param(10.0),
                    max = param(200.0),
                    value = dispersion,
                    onChange = setSize
            )
    ), label(
            text = param("Intensity"),
            note = map(intensity) { (it * 100).toReadable() },
            content = slider(
                    min = param(0.0),
                    max = param(1.0),
                    value = intensity,
                    onChange = setIntensity
            )
    ), label(
            text = param("Granularity"),
            note = map(granularity) { it.toReadable() },
            content = slider(
                    min = param(1.0),
                    max = param(5.0),
                    value = granularity,
                    onChange = setGranularity
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
    ), canvas(param(sprayViewClass), active) { gl ->
        { width, height ->
            gl.artCanvas(width, height) { art ->
                val bounds = Bounds(0.0, 0.0, width.toDouble(), height.toDouble())
                gl.settings()
                        .blend(false)
                        .viewport(0, 0, width, height)
                        .apply {
                            art.clean().spray(
                                    curve(width.toDouble(), height.toDouble()),
                                    dispersion(),
                                    intensity(),
                                    granularity(),
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

fun sprayInputConnected(store: Store<LustresState>): El {
    val state = store.state
    return sprayInput(
            setSize = { store.dispatch(SprayAction.SetSize(it)) },
            setGranularity = { store.dispatch(SprayAction.SetGranularity(it)) },
            setIntensity = { store.dispatch(SprayAction.SetIntensity(it)) },
            setOpacity = { store.dispatch(SprayAction.SetOpacity(it)) },
            dispersion = map(state) { selectSprayDispersion(it) },
            granularity = map(state) { selectSprayGranularity(it) },
            intensity = map(state) { selectSprayIntensity(it) },
            opacity = map(state) { selectSprayOpacity(it) },
            active = map(state) { selectMenuTab(it) == MenuTab.SPRAY }
    )
}