package api

import core.Disposable
import core.JsArray
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.w3c.dom.WebSocket
import kotlin.coroutines.*
import kotlinx.coroutines.launch
import org.w3c.dom.ErrorEvent
import org.w3c.dom.events.Event
import kotlin.math.min


private fun WebSocket.on(name: String, handler: (event: Event) -> Unit): () -> Unit {
    this.addEventListener(name, handler)
    return { this.removeEventListener(name, handler) }
}

private suspend fun WebSocket.connect(): Boolean {
    return if (this.readyState == 0.toShort()) {
        suspendCoroutine { continuation ->
            val disposers = JsArray<() -> Unit>()
            val clean = { disposers.forEach { f -> f() } }
            disposers.add(this.on("open") {
                clean()
                continuation.resume(true)
            }).add(this.on("error") {
                clean()
                this.close()
                continuation.resume(false)
            }).add(this.on("close") {
                clean()
                continuation.resume(false)
            })
        }
    } else {
        this.readyState == 1.toShort()
    }
}

private suspend fun WebSocket.listen(
    onMessage: (String) -> Unit
) {
    if (this.readyState == 1.toShort()) {
        var disposed = false
        this.onmessage = { event ->
            if (!disposed) {
                onMessage(event.data.unsafeCast<String>())
            }
        }

        suspendCoroutine<Unit> { continuation ->
            val disposers = JsArray<() -> Unit>()
            val clean = {
                disposed = true
                this.onmessage = null
                disposers.forEach { f -> f() }
            }

            disposers.add(this.on("close") {
                clean()
                continuation.resume(Unit)
            }).add(this.on("error") { event ->
                clean()
                this.close()
                continuation.resumeWithException(Error((event as? ErrorEvent)?.message ?: "Socket error"))
            })
        }
    }
}

class LustresApi(
    private val url: String,
    private val onConnect: suspend (LustresApi) -> Unit
) {
    private var disposed = false

    private val json = Json(JsonConfiguration.Stable)
    private val serverSerializer = ServerMessage.serializer()
    private val clientSerializer = ClientMessage.serializer()

    private var listeners = JsArray<Continuation<ServerMessage>>()
    private fun eachListener(callback: (Continuation<ServerMessage>) -> Unit) {
        listeners.let { items ->
            listeners = JsArray()
            items.forEach { continuation ->
                callback(continuation)
            }
        }
    }

    private var queue = Channel<ClientMessage>()

    private val worker = GlobalScope.launch {
        var interval = 100L
        while (true) {
            val socket = WebSocket(url)
            try {
                if (socket.connect()) {
                    val old = queue
                    queue = Channel<ClientMessage>()

                    val listen = launch {
                        socket.listen { message ->
                            val parsed = json.parse(serverSerializer, message)
                            eachListener { continuation ->
                                continuation.resume(parsed)
                            }
                        }
                    }

                    val send = launch {
                        while (true) {
                            val message = queue.receive()
                            val data = json.stringify(clientSerializer, message)
                            socket.send(data)
                        }
                    }

                    onConnect(this@LustresApi)
                    while (old.poll()?.let { message ->
                            queue.send(message)
                            true
                        } == true) {}

                    listen.join()
                    send.cancelAndJoin()
                }
            } catch (e: Error) {
                delay(interval)
                interval = min(interval * 2, 1000)
            } finally {
                socket.close()
                val error = Error("Socket closed")
                eachListener { continuation ->
                    continuation.resumeWithException(error)
                }
            }
        }
    }

    private suspend fun send(message: ClientMessage) {
        GlobalScope.launch {
            queue.send(message)
        }
    }

    private suspend fun receive(): ServerMessage = suspendCoroutine { continuation ->
        listeners.add(continuation)
    }

    private suspend fun receive(rid: String): ServerMessage {
        while (true) {
            val response = receive()
            if (response.rid == rid) {
                return response
            }
        }
    }

    suspend fun createAnonymousAccount(): ServerMessage.AnonymousAccountCreated {
        val request = ClientMessage.CreateAnonymousAccount()
        send(request)
        return when (val response = receive(request.mid)) {
            is ServerMessage.AnonymousAccountCreated -> response
            is ServerMessage.ServerError -> throw Error(response.code)
            else -> throw Error("Unexpected message type")
        }
    }

    suspend fun loginAnonymous(token: String): Boolean {
        val request = ClientMessage.LoginAnonymous(token)
        send(request)
        return when (val response = receive(request.mid)) {
            is ServerMessage.LoginSuccess -> true
            is ServerMessage.ServerError -> throw Error(response.code)
            else -> throw Error("Unexpected message type")
        }
    }

    suspend fun createDocument(): ServerMessage.DocumentCreated {
        val request = ClientMessage.CreateAnonymousAccount()
        send(request)
        return when (val response = receive(request.mid)) {
            is ServerMessage.DocumentCreated -> response
            is ServerMessage.ServerError -> throw Error(response.code)
            else -> throw Error("Unexpected message type")
        }
    }

    suspend fun dispose() {
        disposed = true
        worker.cancelAndJoin()
    }
}
