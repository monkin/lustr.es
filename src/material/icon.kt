package material

import core.Param
import core.map
import core.param
import kotlinx.css.*
import kotlinx.css.properties.ms
import kotlinx.css.properties.transition
import oneact.*

// http://materialdesignicons.com

private val emptyByDefault = param<String>("")
private val falseByDefault = param(false)
private val trueByDefault = param(true)
private val primaryTextByDefault = param(ThemeColor.PRIMARY_TEXT)

private val iconClass = cl("icon")
private val disabledClass = cl("disabled")
private val focusedClass = cl("focused")

private val styled = style {
    ".$iconClass" {
        width = 24.px
        height = 24.px
        opacity = 0.7
        transition("opacity", 150.ms)
    }
    ".$focusedClass" {
        opacity = 1
    }
    ".$disabledClass" {
        opacity = 0.5
    }
}

fun icon(
        /**
         * See build/icons/icons.kt
         */
        image: Param<String>,
        focused: Param<Boolean> = falseByDefault,
        enabled: Param<Boolean> = trueByDefault,
        color: Param<Color> = primaryTextByDefault,
        className: Param<String?> = emptyByDefault
) = styled(el(
        attr("class", classes {
            c(iconClass)
            c(className)
            c(disabledClass, map(enabled) { !it })
            c(focusedClass, focused)
        }),
        svg(
                "svg",
                attr("version", "1.1"),
                attr("width", "24"),
                attr("height", "24"),
                attr("viewBox", "0 0 24 24"),
                attr("style", inline("fill" to map(color) { it.value })),
                svg("path", attr("d", image))
        )
))