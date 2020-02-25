package oneact

import core.Action
import kotlin.browser.document

fun none() = object : El() {
    override val node = Nodes.Single(document.createComment("none"))
    override val dispose = Action.Noop
    override val update = Action.Noop
}