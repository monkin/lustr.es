package oneact

import core.Param
import core.action
import kotlin.browser.document

fun conditional(condition: Param<Boolean>, ifTrue: () -> El, ifFalse: () -> El): El {
    val context = Context.context()
    return if (condition is Param.Value) {
        if (condition()) ifTrue() else ifFalse()
    } else {
        val fragment = document.createDocumentFragment()
        val node = Nodes.Range(fragment.appendChild(document.createComment("conditional")),
                fragment.appendChild(document.createComment("/conditional")))
        var value = condition()
        var child = if (value) ifTrue() else ifFalse()
        child.node.insertAfter(node.first)

        object : El() {
            override val node = node
            override val update = action {
                val newValue = condition()
                if (newValue != value) {
                    value = newValue
                    child.remove()
                    child = context.use {
                        if (value) ifTrue() else ifFalse()
                    }.also {
                        it.node.insertAfter(node.first)
                    }
                } else {
                    child.update()
                }
            }
            override val dispose = action { child.dispose() }
        }
    }
}