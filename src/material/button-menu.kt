package material

import core.Param
import core.map
import core.param
import kotlinx.css.*
import oneact.*

private val menuButtonClass = cl("menu-button")
private val captionClass = cl("menu-button-caption")
private val iconClass = cl("menu-button-icon")

private val styled = style {
    ".$menuButtonClass" {
        overflow = Overflow.hidden
        outline = Outline.none
        padding(8.px, 16.px)
        display = Display.flex
        alignItems = Align.center
    }
    ".$captionClass" {
        flexGrow = 1.0
        textAlign = TextAlign.center
    }
}

private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)
private val noop = {}

fun menuButton(
    caption: Param<String>,
    iconImage: Param<String?> = param<String?>(null),
    href: Param<String?> = nullByDefault,
    target: Param<String?> = nullByDefault,
    enabled: Param<Boolean> = trueByDefault,
    className: Param<String?> = nullByDefault,
    action: () -> Unit = noop
) = styled(baseButton(
    href = href,
    target = target,
    action = action,
    content = children(
        el(
            className(iconClass),
            optional(
                map(iconImage) { it !== null }
            ) {
                icon(
                    image = map(iconImage) { it!! }
                )
            }
        ),
        el(
            className(captionClass),
            text(caption)
        )
    ),
    className = classes {
        c(className)
        c(menuButtonClass)
    },
    rippleOrigin = RippleOrigin.MOUSE,
    enabled = enabled
))