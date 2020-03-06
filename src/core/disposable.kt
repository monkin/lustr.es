package core

interface Disposable {
    fun dispose(): Unit
}

interface DisposableContext {
    fun <T: Disposable> disposable(value: T): T
    fun disposable(block: () -> Unit): Unit
    fun <T: Disposable> constructing(value: T): T
}

private class DisposableContextImplementation : Disposable, DisposableContext {

    private val disposables = JsArray<Disposable>()
    override fun <T : Disposable> disposable(value: T): T {
        disposables += value
        return value
    }

    override fun disposable(block: () -> Unit) {
        disposable(object : Disposable {
            override fun dispose() {
                block()
            }
        })
    }

    private val constructings = ArrayList<Disposable>()
    override fun <T: Disposable> constructing(value: T): T {
        constructings += value
        return value
    }

    override fun dispose() {
        disposables.reversed().forEach { v -> v.dispose() }
    }
    fun deconstruct() {
        constructings.reversed().forEach { it.dispose() }
    }
}

inline fun <T : Disposable, X> T.use(callback: (value: T) -> X): X {
    try {
        return callback(this)
    } finally {
        this.dispose()
    }
}

fun <X> context(callback: DisposableContext.() -> X): X {
    val context = DisposableContextImplementation()
    try {
        return callback(context)
    } catch (e: Throwable) {
        context.deconstruct()
        throw e
    } finally {
        context.dispose()
    }
}
