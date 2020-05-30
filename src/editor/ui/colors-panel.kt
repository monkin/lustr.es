package editor.ui

import core.Param
import core.param
import core.map
import draw.Color
import editor.state.*
import kotlinx.css.*
import material.RippleOrigin
import material.ThemeColor
import material.baseButton
import material.shadow
import oneact.*
import state.Store

private val colorButtonClass = cl("color-button")
private val smallClass = cl("small")
private val largeClass = cl("large")
private val styledButton = style {
    ".$colorButtonClass" {
        marginBottom = 8.px
    }
    ".$smallClass" {
        width = 24.px
        height = 24.px
        borderRadius = 2.px
        shadow(1.0)
    }
    ".$largeClass" {
        width = 32.px
        height = 32.px
        borderRadius = 2.px
        shadow(2.0)
    }
}

private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)
private val smallByDefault = param(ColorButtonSize.SMALL)
private val buttonColor = param(ThemeColor.TRANSPARENT)
private val noop = {}

enum class ColorButtonSize(val cssClass: String) {
    SMALL(smallClass),
    LARGE(largeClass)
}

fun colorButton(
        color: Param<Color>,
        href: Param<String?> = nullByDefault,
        target: Param<String?> = nullByDefault,
        enabled: Param<Boolean> = trueByDefault,
        className: Param<String?> = nullByDefault,
        size: Param<ColorButtonSize> = smallByDefault,
        action: () -> Unit = noop
) = styledButton(baseButton(
        href = href,
        target = target,
        action = action,
        color = buttonColor,
        content = el(
                className {
                    c(map(size) { it.cssClass })
                },
                attr("style", inline(
                        Pair("background", map(color) { it.toString() })
                ))
        ),
        className = classes {
            c(colorButtonClass)
            c(className)
        },
        rippleOrigin = RippleOrigin.CENTER,
        enabled = enabled
))


val panelClass = cl("color-panel")
val paletteItemClass = cl("color-panel-item")
val selectedColorClass = cl("selected-color")
val styledPanel = style {
    ".$panelClass" {
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
        padding = "0 4px"
        backgroundColor = ThemeColor.PAPER_NORMAL
        shadow(2.0)
        zIndex = 1
    }
    ".$selectedColorClass" {
        margin = "4px auto 4px auto"
    }
    ".$paletteItemClass" {
        margin = "2px auto"
    }
}

fun colorsPanel(
        color: Param<Color>,
        palette: Param<Collection<Color>>,
        onColorClick: (Color) -> Unit
) = styledPanel(el(
        className(panelClass),
        colorButton(
                color = color,
                size = param(ColorButtonSize.LARGE),
                className = param(selectedColorClass),
                action = { onColorClick(color()) }
        ),
        list(palette, { v -> v.toString() }) { i, item ->
            flip(
                    i,
                    300,
                    colorButton(
                            color = item,
                            size = param(ColorButtonSize.SMALL),
                            className = param(paletteItemClass),
                            action = { onColorClick(item()) }
                    )
            )
        }
))

fun colorsPanelConnected(store: Store<LustresState>): El {
    val state = store.state
    return colorsPanel(
            color = map(state) { selectColor(it) },
            palette = map(state) { selectPalette(it).toList() },
            onColorClick = { color ->
                if (color == selectColor(state())) {
                    store.dispatch(ColorDialogAction.Open)
                } else {
                    store.dispatch(PaletteAction.SetColor(color))
                }
            }
    )
}