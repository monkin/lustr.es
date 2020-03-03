import editor.lustres
import material.*
import oneact.children
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

fun main() {
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
            console.log("Message:", e.unsafeCast<MessageEvent>().data)
        })
    }
}
