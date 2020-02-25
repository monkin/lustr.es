package material

import core.Param
import core.map
import core.param
import core.toFixed
import kotlinx.css.*
import kotlinx.css.properties.Transition
import kotlinx.css.properties.ms
import kotlinx.css.properties.transition
import oneact.*
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document

enum class RippleOrigin {
    MOUSE,
    CENTER
}

private data class RippleItem(val x: String, val y: String, val id: String)

private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)
private val transparentByDefault = param(ThemeColor.TRANSPARENT)

private val baseButtonClass = cl("base-button")
private val disabledClass = cl("disabled")
private val styled = style {
    ".$baseButtonClass" {
        position = Position.relative
        display = Display.inlineBlock
        verticalAlign = VerticalAlign.middle
        userSelect = UserSelect.none
        color = ThemeColor.PRIMARY_TEXT
        fontFamily = uiFont()
        fontWeight = FontWeight.w500
        fontSize = 14.px
        textTransform = TextTransform.uppercase
        outline = Outline.none
        cursor =  Cursor.pointer
        fillShadow(ThemeColor.TRANSPARENT)
        transition("box-shadow", 150.ms)
    }
    ".$baseButtonClass:hover, .$baseButtonClass:active" {
        fillShadow(ThemeColor.BORDER)
    }
    ".$disabledClass" {
        cursor = Cursor.default
        pointerEvents = PointerEvents.none
    }
}

private var buttonCounter = 0
private var rippleIdCounter = 0

fun baseButton(
        href: Param<String?> = nullByDefault,
        target: Param<String?> = nullByDefault,
        rippleOrigin: RippleOrigin = RippleOrigin.CENTER,
        enabled: Param<Boolean> = trueByDefault,
        className: Param<String?> = nullByDefault,
        color: Param<Color> = transparentByDefault,
        content: El = none(),
        action: () -> Unit
): El {
    val isLink = map(href) { it != null }
    val nodeId = "button_${buttonCounter++}"

    return styled(state(ArrayList<RippleItem>()) { getRipples, setRipples ->
        val clickHandler = { e: MouseEvent ->
            if (enabled()) {
                val id = "r${rippleIdCounter++}"
                var x = "50%"
                var y = "50%"

                if (rippleOrigin === RippleOrigin.MOUSE && e.clientX != 0 && e.clientY != 0) {
                    val node = document.getElementById(nodeId)
                    if (node != null) {
                        val bounds = node.getBoundingClientRect()
                        x = (e.clientX.toDouble() - bounds.left).toFixed(2) + "px"
                        y = (e.clientY.toDouble() - bounds.top).toFixed(2) + "px"
                    }
                }

                setRipples { ripples ->
                    val r = ArrayList(ripples)
                    r.add(RippleItem(x, y, id))
                    r
                }

                if (!isLink()) {
                    action()
                }
            } else {
                if (!isLink()) {
                    e.preventDefault()
                }
            }
        }
        val ripples = list(getRipples, { _, v -> v.id}) { _, v -> ripple(
                x = map(v) { it.x },
                y = map(v) { it.y },
                onRemoveRequest = {
                    setRipples {
                        ArrayList(it.filter { it.id != v().id })
                    }
                }
        )}
        children(content, ripples) { content ->
            conditional(isLink, {
                el("a",
                        attr("id", nodeId),
                        attr("href", href),
                        attr("target", target),
                        attr("tabindex", map(enabled) { if (it) "0" else "-1" }),
                        attr("class", classes {
                            c(className)
                            c(baseButtonClass)
                            c(map(enabled) {
                                if (it)
                                    disabledClass
                                else
                                    null
                            })
                        }),
                        attr("style", inline(
                                "background-color" to map(color, { it.toString() })
                        )),
                        handler("onClick", clickHandler),
                        content
                )
            }, {
                el("div",
                        attr("id", nodeId),
                        attr("tabindex", map(enabled) { if (it) "0" else "-1" }),
                        attr("class", classes {
                            c(className)
                            c(baseButtonClass)
                        }),
                        attr("style", inline(
                                "background-color" to map(color) { it.toString() }
                        )),
                        handler("onClick", clickHandler),
                        content
                )
            })
        }
    })
}

