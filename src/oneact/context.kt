package oneact

import kotlin.reflect.KClass

class Context(val parent: Context? = context()) {
    val content = HashMap<KClass<*>, Any>()
    fun get(key: KClass<*>): Any? = content[key] ?: parent?.get(key)

    inline fun <reified T : Any> get(): T =
            (content[T::class] ?: parent?.get(T::class)) as T? ?: throw Error("Failed to find context value for class '${T::class.simpleName}'")
    fun put(value: Any) {
        content[value::class] = value
    }

    fun <R> use(f: () -> R): R {
        val previous = instance
        return try {
            instance = this
            f()
        } finally {
            instance = previous
        }
    }

    fun <R> bind(f: () -> R) = use(f)
    fun <R, A1> bind(f: (A1) -> R) = { a1: A1 ->
        use { f(a1) }
    }
    fun <R, A1, A2> bind(f: (A1, A2) -> R) = { a1: A1, a2: A2 ->
        use { f(a1, a2) }
    }
    fun <R, A1, A2, A3> bind(f: (A1, A2, A3) -> R) = { a1: A1, a2: A2, a3: A3 ->
        use { f(a1, a2, a3) }
    }
    fun <R, A1, A2, A3, A4> bind(f: (A1, A2, A3, A4) -> R) = { a1: A1, a2: A2, a3: A3, a4: A4 ->
        use { f(a1, a2, a3, a4) }
    }

    companion object {
        private var instance = Context(null)
        fun context() = instance
        inline fun <reified T : Any> get() = context().get<T>()
    }
}