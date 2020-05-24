package material

import core.Param
import core.map
import core.param
import oneact.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.InputEvent

private val wrapperClass = cl("string-input-wrapper")
private val stringInputClass = cl("string-input")
private val invalidClass = cl("invalid")

private val styled = style("""
.$wrapperClass {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: stretch;
}
.$stringInputClass {
    border-width: 0 0 1px 0;
    border-style: solid;
    border-color: ${ThemeColor.BORDER};
    padding: 8px 0;
    font-size: 16px;
    font-family: ${uiFont()};
    outline: none;
}
.$stringInputClass:disabled {
    border-style: dashed;
}
.$wrapperClass:after {
    content: "";
    position: absolute;
    left: 0;
    bottom: 0;
    height: 2px;
    width: 100%;
    transform: scaleX(0);
    opacity: 0;
    transition: transform 0.15s, opacity 0.15s;
    will-change: opacity, transform;
    background: ${ThemeColor.ACCENT};
}
.$wrapperClass:focus-within:after {
    opacity: 1;
    transform: none;
}
.$wrapperClass:disabled:after {
    opacity: 0;
}
.$invalidClass:after {
    background-color: ${ThemeColor.DANGER};
}
.$invalidClass > .$stringInputClass {
    border-color: ${ThemeColor.DANGER};
}""")


private val trueByDefault = param(true)
private val falseByDefault = param(false)
private val nullByDefault = param<String?>(null)

fun stringInput(
        value: Param<String>,
        label: Param<String?> = nullByDefault,
        note: Param<String?> = nullByDefault,
        enabled: Param<Boolean> = trueByDefault,
        valid: Param<Boolean> = trueByDefault,
        password: Param<Boolean> = falseByDefault,
        onChange: (value: String) -> Unit
): El {
    val input = {
        el(
                className {
                    c(wrapperClass)
                    c(invalidClass, map(valid) { !it })
                },
                el("input",
                        className(stringInputClass),
                        attr("type", map(password) { if (it) "password" else "text" }),
                        attr("value", value),
                        attr("disabled", map(enabled) { if (it) null else "disabled" }),
                        handler<InputEvent>("onInput") { onChange(it.target!!.unsafeCast<HTMLInputElement>().value) }
                )
        )
    }
    return styled(
            conditional(map(label) { it != null }, {
                material.label(
                        text = map(label) { it ?: "" },
                        note = note,
                        enabled = enabled,
                        valid = valid,
                        content = input()
                )
            }, input)
    )
}