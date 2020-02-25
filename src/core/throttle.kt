package core
import kotlin.js.Date

fun throttle(delay: Int, f: () -> Unit): () -> Unit {
    var last = 0.0
    var handle: Disposable? = null
    return {
        if (handle === null) {
            val now = Date.now()
            if (now - last >= delay) {
                last = now
                f()
            } else {
                handle = timeout((delay - (now - last)).toInt()) {
                    last = Date.now()
                    handle = null
                    f()
                }
            }
        }
    }
}

fun <T> throttle(delay: Int, f: (a: T) -> Unit): (T) -> Unit {
    var call = {}
    val t = throttle(delay) {
        call()
    }
    return { v: T ->
        call = { f(v) }
        t()
    }
}
