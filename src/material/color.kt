package material

import core.Param
import core.map
import core.param
import core.toFixed
import draw.Color
import kotlinx.css.*
import kotlinx.css.properties.s
import kotlinx.css.properties.scale
import kotlinx.css.properties.transform
import kotlinx.css.properties.transition
import oneact.*
import kotlin.math.max
import kotlin.math.min

private fun clamp(v: Double) = max(0.0, min(1.0, v))

private val hueInputClass = cl("hue-input")
private val huePointClass = cl("hue-input-point")

private val styledHueInput = oneact.style {
    ".$hueInputClass" {
        position = Position.relative
        height = 8.px
        padding(8.px, 0.px)
        put("background", """linear-gradient(to right,
            rgb(255, 0, 0) 0%,
            rgb(255, 255, 0) 16.6667%,
            rgb(0, 255, 0) 33.3333%,
            rgb(0, 255, 255) 50%,
            rgb(0, 0, 255) 66.6667%,
            rgb(255, 0, 255) 83.3333%, rgb(255, 0, 0) 100%
        )""")
        put("backgroundClip", "content-box")
        outline = Outline.none
    }
    ".$huePointClass" {
        width = 16.px
        height = 16.px
        margin((-8).px, 0.px, 0.px, (-8).px)
        position = Position.absolute
        top = 50.pct
        transition("transform", 0.3.s)
        shadow(2.0)
        borderRadius = 50.pct
    }
    ".$hueInputClass:active > .$huePointClass" {
        transform {
            scale(1.5)
        }
    }
}

private val nullByDefault = param<String?>(null)

fun hueInput(
        value: Param<Double>,
        className: Param<String?> = nullByDefault,
        onChange: (value: Double) -> Unit
) = styledHueInput(dragListeners(el(
        attr("class", classes {
            c(hueInputClass)
            c(className)
        }),
        attr("tabindex", "0"),
        el(
                attr("class", huePointClass),
                attr("style", inline(
                        "left" to map(value) { (it * 100).toFixed(4) + "%" },
                        "background-color" to map(value) {
                            Color.HsvColor(it, 1.0, 1.0).toString()
                        }
                ))
        )
)) {
    onDrag { (x, _, bounds) ->
        onChange(
                max(0.0, min(0.9999,(x - bounds.left) / bounds.width))
        )
    }
})

private val svInputClass = cl("sv-input")
private val svInputPointClass = cl("sv-input-point")
private val styledSvInput = oneact.style {
    ".$svInputClass" {
        position = Position.relative
        minHeight = 256.px
        minWidth = 256.px
        outline = Outline.none
    }
    ".$svInputPointClass" {
        transition("transform", 0.3.s)
        width = 16.px
        height = 16.px
        margin((-8).px, 0.px, 0.px, (-8).px)
        position = Position.absolute
        boxSizing = BoxSizing.borderBox
        border = "solid 2px rgba(255, 255, 255, 0.84)"
        borderRadius = 50.pct
        shadow(2.0)
    }
    ".$svInputClass:active > .$svInputPointClass" {
        transform {
            scale(1.5)
        }
    }
    ".$svInputClass:before" {
        content = QuotedString("")
        display = Display.block
        position = Position.absolute
        left = 0.px
        top = 0.px
        width = 100.pct
        height = 100.pct
        background = "linear-gradient(to top, black, rgba(0, 0, 0, 0)), linear-gradient(to right, white, rgba(255, 255, 255, 0))"
    }
}

fun saturationValueInput(
        value: Param<Color.HsvColor>,
        className: Param<String?> = nullByDefault,
        onChange: (value: Color.HsvColor) -> Unit
) = styledSvInput(dragListeners(el(
        attr("class", classes {
            c(svInputClass)
            c(className)
        }),
        attr("tabindex", "0"),
        attr("style", inline(
                "background" to map(value) {
                    Color.HsvColor(it.h, 1.0, 1.0).toString()
                }
        )),
        el(
                attr("class", svInputPointClass),
                attr("style", inline(
                        "left" to map(value) {
                            (it.s * 100.0).toFixed(4) + "%"
                        },
                        "top" to map(value) {
                            ((1.0 - it.v) * 100.0).toFixed(4) + "%"
                        },
                        "background" to map(value) { it.toString() }
                ))
        )
)) {
    onDrag { (x, y, bounds) ->
        onChange(Color.HsvColor(
                value().h,
                clamp((x - bounds.left) / bounds.width),
                clamp(1.0 - (y - bounds.top) / bounds.height)
        ))
    }
})

private class HsvState(
        val hsv: Color.HsvColor = Color.HsvColor(0.0, 0.0, 0.0),
        val rgb: Color.RgbColor = hsv.toRgb()
) {
    fun setHsv(color: Color.HsvColor) = HsvState(color, color.toRgb())
    fun setRgb(color: Color.RgbColor): HsvState {
        val (h, s, v) = color.toHsv()
        return when {
            v < 0.0001 -> HsvState(Color.HsvColor(hsv.h, hsv.s, v), color)
            s < 0.0001 -> HsvState(Color.HsvColor(hsv.h, s, v), color)
            else -> HsvState(Color.HsvColor(h, s, v), color)
        }
    }
}

private val hsvInputHueClass = cl("hsv-input-hue-class")
private val hsvInputStyled = style("""
.$hsvInputHueClass {
    margin-bottom: 4px;
}
""")

fun hsvInput(
        value: Param<Color>,
        className: Param<String?> = nullByDefault,
        onChange: (color: Color) -> Unit
): El {
    var state = HsvState(value().toHsv())
    return hsvInputStyled(beforeUpdate(el(
        attr("class", className),
        hueInput(
                value = param { state.hsv.h },
                className = param(hsvInputHueClass),
                onChange = { h ->
                    val (_, s, v) = state.hsv
                    val color = Color.HsvColor(h, s, v);
                    state = state.setHsv(color)
                    onChange(color)
                }
        ),
        saturationValueInput(
                value = map(value) { it.toHsv() },
                onChange = { color ->
                    state = state.setHsv(color)
                    onChange(color)
                }
        )
        )) {
        state = state.setRgb(value().toRgb())
    })
}


