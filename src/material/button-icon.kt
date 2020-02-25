package material

import core.Param
import core.param
import kotlinx.css.Color
import oneact.*
import org.w3c.dom.events.FocusEvent

private val iconButtonClass = cl("flat-button")

private val styled = style("""
.$iconButtonClass {
    border-radius: 50%;
    overflow: hidden;
    outline: none;
    width: 24px;
    height: 24px;
    padding: 4px;
    overflow: visible;
}""")

private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)
private val textColorByDefault = param(ThemeColor.PRIMARY_TEXT)
private val noop = {}

fun iconButton(
        image: Param<String>,
        color: Param<Color> = textColorByDefault,
        href: Param<String?> = nullByDefault,
        target: Param<String?> = nullByDefault,
        enabled: Param<Boolean> = trueByDefault,
        className: Param<String?> = nullByDefault,
        action: () -> Unit = noop
) = styled(state(false) { getFocused, setFocused ->
    nodeListeners(baseButton(
            href = href,
            target = target,
            action = action,
            content = icon(
                    image = image,
                    enabled = enabled,
                    focused = getFocused,
                    color = color
            ),
            className = classes {
                c(className)
                c(iconButtonClass)
            },
            rippleOrigin = RippleOrigin.CENTER,
            enabled = enabled
    )) {
        listen<FocusEvent>("onFocus") {
            setFocused(true)
        }
        listen<FocusEvent>("onBlur") {
            setFocused(false)
        }
    }
})