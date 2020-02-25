import core.ImmutableListSerializer
import core.ListNode
import core.immutableListOf
import editor.lustres
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import material.*
import oneact.children
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    val list = immutableListOf(1, 2, 3, 4)

    val json = Json(JsonConfiguration.Stable)
    val s = json.stringify(ImmutableListSerializer(IntSerializer), list)
    val d = json.parse(ImmutableListSerializer(IntSerializer), s)

    console.log(list, ":", s, "<->", d)

    val isWorker = js("typeof WorkerGlobalScope !== 'undefined' && self instanceof WorkerGlobalScope").unsafeCast<Boolean>()
    if (!isWorker) {
        document.addEventListener("DOMContentLoaded", {
            children(
                theme(),
                fontLoader(),
                lustres()
            ).node.appendTo(document.body!!)
        })
    } else {
        window.addEventListener("message", { e: Event ->
            console.log("message>", e.unsafeCast<MessageEvent>().data)
        })
    }
}
