package material

import core.Param
import core.map
import core.param
import oneact.*

private val labelClass = cl("label")
private val labelContainerClass = cl("label-container")
private val labelTextClass = cl("label-text")
private val disabledClass = cl("disabled")
private val invalidClass = cl("invalid")

private val styled = style("""
.$labelClass {
    -moz-user-select: none;
    user-select: none;
}
.$labelClass .$labelTextClass {
    margin: 16px 0 8px 0;
    font-size: 12px;
    font-family: ${uiFont()};
    color: ${ThemeColor.SECONDARY_TEXT};
    transition: color 0.15s;
}
.$labelClass:focus-within .$labelTextClass {
    color: ${ThemeColor.ACCENT};
}
.$labelContainerClass {
    user-select: none;
    display: flex;
    justify-content: space-between;
}
.$labelContainerClass.$invalidClass .$labelTextClass {
    color: ${ThemeColor.DANGER};
}
.$labelContainerClass.$disabledClass .$labelTextClass {
    color: ${ThemeColor.BORDER};
}""")

private val trueByDefault = param(true)
private val nullByDefault = param<String?>(null)

fun label(
        text: Param<String>,
        note: Param<String?> = nullByDefault,
        enabled: Param<Boolean> = trueByDefault,
        valid: Param<Boolean> = trueByDefault,
        content: El
): El = styled(el(
        className(labelClass),
        el(
                className {
                    c(labelContainerClass)
                    c(disabledClass, map(enabled) { !it })
                    c(invalidClass, map(valid) { !it })
                },
                el(
                        attr("class", labelTextClass),
                        oneact.text(text)
                ),
                optional(map(note) { it != null }) {
                    el(
                            attr("class", labelTextClass),
                            oneact.text(map(note) { it ?: "" })
                    )
                }
        ),
        content
))