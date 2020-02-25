package oneact

import core.Param
import core.toFixed
import org.w3c.dom.DOMRect
import kotlin.browser.window
import kotlin.js.Promise

fun <T> flip(stage: Param<T>, duration: Int = 300, node: El): El {
    return node.afterMount {
        val el = node.node.only.toHtmlElement()
        el.style.opacity = "0"
        window.requestAnimationFrame { window.requestAnimationFrame {
            el.style.transition = "opacity ${duration.toFixed(0)}ms"
            el.style.opacity = "1"
        } }
    }.effectBeforeUpdate(stage) {
        val el = node.node.only.toHtmlElement()
        val originPosition = el.getBoundingClientRect()
        val originOpacity = el.style.opacity
        Promise.resolve(Unit).then {
            el.removeAttribute("style")
            val targetPosition = el.getBoundingClientRect()
            val targetOpacity = window.getComputedStyle(el).opacity

            el.style.transformOrigin = "0 0"
            el.style.transform = "translate(${(originPosition.left - targetPosition.left).toFixed(2)}px, ${(originPosition.top - targetPosition.top).toFixed(2)}px) " +
                    "scale(${(originPosition.width / targetPosition.width).toFixed(4)}, ${(originPosition.height / targetPosition.height).toFixed(4)})"
            el.style.opacity = originOpacity
            window.requestAnimationFrame { window.requestAnimationFrame {
                el.style.transition = "transform ${duration.toFixed(0)}ms, opacity ${duration.toFixed(0)}ms"
                el.style.transform = "none"
                el.style.opacity = targetOpacity
            } }
        }
        ({})
    }
}