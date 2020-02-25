package material

import core.Param
import core.map
import core.param
import core.toFixed
import oneact.*
import kotlin.math.E
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

private val sliderPointClass = cl("slider-point")
private val sliderFocusClass = cl("slider-focus")
private val sliderContainerClass = cl("slider-container")
private val sliderProgressClass = cl("slider-progress")
private val pointsContainerClass = cl("points-container")

private val styled = style("""
.$sliderPointClass {
    border-radius: 50%;
    background-color: ${ThemeColor.ACCENT};
    width: 12px;
    height: 12px;
    position: absolute;
    left: calc(100% - 6px);
    top: calc(50% - 6px);
    transition: transform 0.3s;
}
.$sliderFocusClass {
    border-radius: 50%;
    background-color: ${ThemeColor.ACCENT};
    opacity: 0;
    width: 12px;
    height: 12px;
    position: absolute;
    left: calc(100% - 6px);
    top: calc(50% - 6px);
    transition: opacity 0.3s, transform 0.3s;
}
.$sliderContainerClass {
    background-clip: content-box;
    padding: 8px 0;
    height: 2px;
    background-color: ${ThemeColor.BORDER};
    outline: none;
}
.$sliderContainerClass:active .$sliderPointClass {
    transform: scale(1.5);
}
.$sliderContainerClass:focus .$sliderFocusClass {
    opacity: 0.24;
    transform: scale(2);
}
.$sliderContainerClass:focus:active .$sliderFocusClass {
    opacity: 0;
}
.$sliderProgressClass {
    position: relative;
    height: 100%;
    background-color: ${ThemeColor.ACCENT}
}
.$pointsContainerClass {
    margin: 0 6px;
    position: relative;
    height: 100%;
}""")

private val nullByDefault = param<String?>(null)
private val falseByDefault = param<Boolean>(false)

fun slider(
        min: Param<Double>,
        max: Param<Double>,
        value: Param<Double>,
        className: Param<String?> = nullByDefault,
        isInteger: Param<Boolean> = falseByDefault,
        onChange: (value: Double) -> Unit
): El {
    val truncate = { v: Double -> if (isInteger()) round(v) else v }
    val clamp = { v: Double -> kotlin.math.min(max(), kotlin.math.max(min(), v)) }

    return styled(dragListeners(el(
            className {
                c(className)
                c(sliderContainerClass)
            },
            attr("tabindex", "0"),
            el(
                    className(sliderProgressClass),
                    attr("style", inline(
                            "width" to map(max, min, value) { pMax, pMin, pValue ->
                                ((pValue - pMin) / (pMax - pMin) * 100.0).toFixed(4) + "%"
                            }
                    )),
                    el(
                            attr("class", pointsContainerClass),
                            el("div", attr("class", sliderPointClass)),
                            el("div", attr("class", sliderFocusClass))
                    )
            )
    )) {
        onDrag { (x, _, bounds) ->
            onChange(clamp(truncate(
                    (x - bounds.left) / bounds.width * (max() - min()) + min()
            )))
        }
    })
}

fun exponentialSlider(
        min: Param<Double>,
        max: Param<Double>,
        value: Param<Double>,
        className: Param<String?> = nullByDefault,
        isInteger: Param<Boolean> = falseByDefault,
        onChange: (value: Double) -> Unit
): El {
    val truncate = { v: Double -> if (isInteger()) round(v) else v }
    val clamp = { v: Double -> kotlin.math.min(max(), kotlin.math.max(min(), v)) }
    return slider(
        className = className,
        min = map(min) { it.pow(0.1) },
        max = map(max) { it.pow(0.1) },
        value = map(value) { it.pow(0.1) },
        isInteger = param(false),
        onChange = { onChange(clamp(truncate(it.pow(10.0)))) }
    )
}
