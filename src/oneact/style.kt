package oneact

import core.Param
import core.param
import kotlinx.css.CSSBuilder
import kotlin.browser.document
import kotlin.browser.window

fun inject(style: String) {
    document.head!!.appendChild(document.createElement("style").also {
        it.appendChild(document.createTextNode(style))
    })
}

/**
 * Create a function that injects style
 */
fun style(style: String): (el: El) -> El {
    var injected = false
    return { el ->
        el.also {
            if (!injected) {
                injected = true
                inject(style)
            }
        }
    }
}

fun style(block: CSSBuilder.() -> Unit): (el: El) -> El {
    var injected = false
    return { el ->
        el.also {
            if (!injected) {
                injected = true
                inject(CSSBuilder("", false).also(block).toString())
            }
        }
    }
}

private class Counter {
    companion object {
        private var counter = 0.0
        fun get() = counter++
    }
}

/**
 * Generate CSS class name
 */
fun cl(prefix: String? = null) = when (prefix) {
    null -> "c-${Counter.get()}"
    else -> "c-$prefix-${Counter.get()}"
}

fun inline(vararg rules: Pair<String, Param<String?>>): Param<String?> = when {
    rules.isEmpty() -> param<String?>(null)
    rules.all { it.second is Param.Value } -> param(rules.filter { it.second() !== null }
            .joinToString(" ") { it.first + ": " + it.second() + ";" })
    else -> param {
        rules.filter { it.second() !== null }
                .joinToString(" ") { it.first + ": " + it.second() + ";" }
    }
}