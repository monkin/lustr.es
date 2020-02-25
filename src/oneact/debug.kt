package oneact

import core.Action
import core.Param
import core.action
import core.map
import kotlin.browser.document

fun debug(value: Param<Any>) = object : El() {
    private var anyContent = value()
    private val stringContent
        get() = when (anyContent) {
            is String -> "~~~ " + anyContent as String + " ~~~"
            else -> "~~~ " + JSON.stringify(anyContent) + " ~~~"
        }

    override val node = Nodes.Single(document.createComment(stringContent))
    override val dispose = Action.Noop
    override val update = action {
        val newContent = value()
        if (newContent != anyContent) {
            anyContent = newContent
            this.node.only.nodeValue = stringContent
        }
    }
}

fun debug(prefix: String, value: Param<Any>) = debug(map(value) { v ->
    prefix + when (v) {
        is String -> v
        else -> JSON.stringify(v)
    }
})