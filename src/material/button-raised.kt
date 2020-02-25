package material

import core.Param
import core.param
import kotlinx.css.*
import kotlinx.css.Color.Companion.transparent
import kotlinx.css.properties.s
import kotlinx.css.properties.transition
import material.borderRadius
import oneact.*

private val raisedButtonClass = cl("flat-button")

private val styled = style {
    ".$raisedButtonClass" {
        borderRadius = 2.px
        overflow = Overflow.hidden
        outline = Outline.none
        padding(8.px, 16.px)
        shadow(2.0)
        fillShadow(transparent)
        transition("box-shadow", 0.15.s)
    }
    ".$raisedButtonClass:hover" {
        shadow(2.0)
        fillShadow(ThemeColor.BORDER)
    }
    ".$raisedButtonClass:active" {
        shadow(8.0)
        fillShadow(ThemeColor.BORDER)
    }
}

private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)
private val noop = {}

fun raisedButton(
        caption: Param<String>,
        href: Param<String?> = nullByDefault,
        target: Param<String?> = nullByDefault,
        enabled: Param<Boolean> = trueByDefault,
        className: Param<String?> = nullByDefault,
        action: () -> Unit = noop
) = styled(baseButton(
        href = href,
        target = target,
        action = action,
        content = el("span", text(caption)),
        className = classes {
            c(className)
            c(raisedButtonClass)
        },
        rippleOrigin = RippleOrigin.MOUSE,
        enabled = enabled
))