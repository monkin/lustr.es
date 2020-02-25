package core

import kotlin.browser.window

fun interval(interval: Int, action: () -> Unit): Disposable {
    val id = window.setInterval(action, interval)
    return object : Disposable {
        override fun dispose() {
            window.clearInterval(id)
        }
    }
}

fun timeout(timeout: Int, action: () -> Unit): Disposable {
    var triggered = false
    val id = window.setTimeout({
        triggered = true
        action()
    }, timeout)
    return object : Disposable {
        override fun dispose() {
            if (!triggered) {
                window.clearTimeout(id)
            }
        }
    }
}