package material

import core.Param
import core.map
import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.css.properties.Timing.Companion.easeInOut
import kotlinx.css.properties.Timing.Companion.easeOut
import oneact.*
import org.w3c.dom.Node
import org.w3c.dom.events.UIEvent

private val buttonClass = cl("main-menu-button")
private val openClass = cl("main-menu-button-inactive")
private val selectedClass = cl("main-menu-button-selected")
private val disabledClass = cl("main-menu-button-disabled")
private val styledButton = style {
    ".$buttonClass" {
        transition("box-shadow", 150.ms)
        shadow(0.0)
        fillShadow(ThemeColor.PAPER_NORMAL)
        borderRadius = 2.px
    }
    ".$selectedClass" {
        shadow(0.0)
        fillShadow(ThemeColor.BORDER)
    }
    ".$openClass" {
        shadow(2.0)
        fillShadow(ThemeColor.PAPER_LIGHT)
        borderRadius(2.px, 0.px, 0.px, 2.px)
        position = Position.relative
        zIndex = 1
        pointerEvents = PointerEvents.none
    }
    ".$disabledClass" {
        shadow(0.0)
        fillShadow(ThemeColor.PAPER_NORMAL)
        pointerEvents = PointerEvents.none
        cursor = Cursor.default
    }
}

fun mainMenuButton(
        image: Param<String>,
        selected: Param<Boolean>,
        enabled: Param<Boolean>,
        open: Param<Boolean>,
        action: () -> Unit
) = styledButton(el(
        iconButton(
                image = image,
                action = action,
                enabled = enabled
        ),
        attr("class", classes {
            c(buttonClass)
            c(openClass, open)
            c(selectedClass, selected)
            c(disabledClass, map(enabled) { !it })
        })
))

private val mainMenuClass = cl("main-menu")
private val buttonsClass = cl("main-menu-buttons")
private val contentClass = cl("main-menu-content")
private val visibleClass = cl("main-menu-visible")

private val styledMenu = style {
    ".$mainMenuClass" {
        display = Display.block
        height = 100.pct
        backgroundColor = ThemeColor.PAPER_NORMAL
        position = Position.relative
        width = 40.px
        shadow(2.0)
        zIndex = 1
    }
    ".$buttonsClass" {
        display = Display.flex
        flexDirection = FlexDirection.column
        alignItems = Align.flexEnd
        height = 100.pct
        overflow = Overflow.hidden
        position = Position.relative
        width = 36.px
    }
    ".$contentClass" {
        position = Position.absolute
        boxSizing = BoxSizing.borderBox
        padding(0.px, 16.px)
        put("left", "calc(100% - 4px)")
        top = 0.px
        height = 100.pct
        width = 320.px
        put("maxWidth", "calc(100vw - 48px)")
        transition("transform", 0.15.s, easeInOut)
        transition("opacity", 0.15.s, easeOut)
        opacity = 0
        put("transformOrigin", "0 0");
        transform {
            scaleX(0)
        }
        pointerEvents = PointerEvents.none
        backgroundColor = ThemeColor.PAPER_LIGHT
        shadow(2.0)
    }
    ".$contentClass.$visibleClass" {
        transform = Transforms.none
        opacity = 1
        pointerEvents = PointerEvents.auto
    }
}

fun mainMenu(
        buttons: El,
        content: El,
        open: Param<Boolean>,
        onHideRequest: () -> Unit
): El {
    val menu = el(
            attr("class", mainMenuClass),
            el(
                    buttons,
                    attr("class", buttonsClass)
            ),
            el(
                    attr("style", inline(
                            "transform" to map(open) { if (it) "none" else "scaleX(0)" }
                    )),
                    attr("class", classes {
                        c(contentClass)
                        c(visibleClass, open)
                    }),
                    content
            )
    )
    return outsideClickListener(styledMenu(menu)) {
        if (open()) {
            onHideRequest()
        }
    }
}