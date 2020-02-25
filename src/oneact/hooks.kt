@file:Suppress("NOTHING_TO_INLINE")

package oneact

import core.Action
import core.Disposable
import core.action
import kotlin.js.Promise

fun beforeUpdate(el: El, hook: Action) = object : El() {
    override val node = el.node
    override val dispose = el.dispose
    override val update = hook.then(el.update)
}

fun beforeUpdate(el: El, hook: () -> Unit) = beforeUpdate(el, action(hook))
fun El.beforeUpdate(hook: () -> Unit) = beforeUpdate(this, hook)

fun afterUpdate(el: El, hook: Action) = object : El() {
    override val node = el.node
    override val dispose = el.dispose
    override val update = el.update.then(hook)
}

fun afterUpdate(el: El, hook: () -> Unit) = afterUpdate(el, action(hook))
fun El.afterUpdate(hook: () -> Unit) = afterUpdate(this, hook)

fun beforeDispose(el: El, hook: Action) = object : El() {
    override val node = el.node
    override val dispose = hook.then(el.dispose)
    override val update = el.update
}

fun beforeDispose(el: El, hook: () -> Unit) = beforeDispose(el, action(hook))
fun beforeDispose(el: El, item: Disposable) = beforeDispose(el) { item.dispose() }
fun El.beforeDispose(hook: () -> Unit) = beforeDispose(this, action(hook))
fun El.beforeDispose(item: Disposable) = beforeDispose(this) { item.dispose() }

fun afterDispose(el: El, hook: Action) = object : El() {
    override val node = el.node
    override val dispose = el.dispose.then(hook)
    override val update = el.update
}

fun afterMount(el: El, hook: El.() -> Unit): El {
    Promise.resolve(Unit).then { hook(el) }
    return el
}

fun El.afterMount(hook: El.() -> Unit) = afterMount(this, hook)

fun afterDispose(el: El, hook: () -> Unit) = afterDispose(el, action(hook))
fun afterDispose(el: El, item: Disposable) = afterDispose(el) { item.dispose() }