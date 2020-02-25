package material

import core.Param
import core.param
import oneact.*

private val flatButtonClass = cl("flat-button")

private val styled = style("""
.$flatButtonClass {
    border-radius: 2px;
    overflow: hidden;
    outline: none;
    padding: 8px 16px;
}""")

private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)
private val noop = {}

fun flatButton(
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
            c(flatButtonClass)
        },
        rippleOrigin = RippleOrigin.MOUSE,
        enabled = enabled
))