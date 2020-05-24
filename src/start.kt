import api.ClientMessage
import api.LustresApi
import editor.lustres
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import material.*
import oneact.children
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    val isWorker = js("typeof WorkerGlobalScope !== 'undefined' && self instanceof WorkerGlobalScope").unsafeCast<Boolean>()
    if (!isWorker) {

        GlobalScope.launch {
            val api = LustresApi("wss://n35i5lcl76.execute-api.eu-west-1.amazonaws.com/default") { api ->
                val account = api.createAnonymousAccount()
                api.loginAnonymous(account.token)
            }
        }

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
