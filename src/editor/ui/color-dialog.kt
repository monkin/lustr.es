package editor.ui

import core.Param
import core.map
import core.param
import core.toFixed
import draw.Color
import editor.state.*
import kotlinx.css.*
import kotlinx.css.properties.ms
import kotlinx.css.properties.transition
import material.ThemeColor
import material.flatButton
import material.hsvInput
import material.shadow
import oneact.*
import state.Store
import kotlin.math.max
import kotlin.math.min

private val containerClass = cl("color-dialog-container")
private val dialogClass = cl("color-dialog")
private val visibleClass = cl("color-dialog-visible")
private val appearKeyframes = cl("appear-keyframes")
private val closeButtonClass = cl("close-button")
private val previewClass = cl("color-dialog-preview")

private val keyframed = style("""
@keyframes $appearKeyframes {
    from { opacity: 0; }
    to { opacity: 1; }
}
.$dialogClass {
    animation: $appearKeyframes 0.3s;
}
""")

private val styled = style {
    ".$containerClass" {
        position = Position.absolute
        left = 0.px
        top = 0.px
        right = 0.px
        bottom = 0.px
        pointerEvents = PointerEvents.none
    }
    ".$dialogClass" {
        position = Position.absolute
        borderRadius = 2.px
        backgroundColor = ThemeColor.PAPER_NORMAL
        padding = "16px 16px 8px 16px"
        opacity = 0
        display = Display.flex
        flexDirection = FlexDirection.column
        alignItems = Align.flexEnd
        transition("opacity", 300.ms)
        shadow(8.0)
    }
    ".$visibleClass" {
        opacity = 1
        pointerEvents = PointerEvents.auto
    }
    ".$closeButtonClass" {
        marginTop = 8.px
    }
    ".$previewClass" {
        width = 100.pct
        height = 24.px
        marginBottom = 8.px
        borderRadius = 2.px
        pointerEvents = PointerEvents.none
        shadow(1.0)
    }
}

fun colorDialog(
        color: Param<Color>,
        onChange: (Color) -> Unit,
        onClose: () -> Unit,
        visible: Param<Boolean>
) = state(Pair(8.0, 8.0)) { position, setPosition ->
    keyframed(styled(delayedOptional(visible, 300) {
        withSize { containerSize ->
            el(
                    className(containerClass),
                    withSize { dialogSize ->
                        var dragPosition = Pair(0.0, 0.0)
                        var dragPoint = Pair(0.0, 0.0)
                        val right = map(position, containerSize, dialogSize) { (x), (containerWidth), (dialogWidth) ->
                            min(containerWidth - dialogWidth - 8, max(8.0, x))
                        }
                        val top = map(position, containerSize, dialogSize) { (_, y), (_, containerHeight), (_, dialogHeight) ->
                            min(containerHeight - dialogHeight - 8, max(8.0, y))
                        }
                        dragListeners(el(
                                className {
                                    c(dialogClass)
                                    c(visibleClass, visible)
                                },
                                attr("style", inline(
                                        Pair("right", map(right) { "${it.toFixed(4)}px" }),
                                        Pair("top", map(top) { "${it.toFixed(4)}px" })
                                )),
                                el(
                                        className(previewClass),
                                        attr("style", inline(Pair("background-color", map(color) { it.toString() })))
                                ),
                                hsvInput(
                                        value = color,
                                        onChange = onChange
                                ),
                                flatButton(
                                        caption = param("Close"),
                                        action = onClose,
                                        className = param(closeButtonClass)
                                )
                        )) {
                            onlySelf()
                            onBeginDrag { e ->
                                dragPoint = Pair(e.x, e.y)
                                dragPosition = Pair(right(), top())
                            }
                            onDrag { e ->
                                setPosition(
                                    Pair(
                                            dragPosition.first - (e.x - dragPoint.first),
                                            dragPosition.second + (e.y - dragPoint.second)
                                    )
                                )
                            }
                        }
                    }
            )
        }
    }))
}

fun colorDialogConnected(): El {
    val store = Context.get<Store<LustresState>>()
    val state = store.state
    return colorDialog(
            color = map(state, selectColor),
            visible = map(state, selectIsColorDialogOpen),
            onChange = { color -> store.dispatch(PaletteAction.SetColor(color)) },
            onClose = { store.dispatch(ColorDialogAction.Close) }
    )
}