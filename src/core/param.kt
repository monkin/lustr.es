@file:Suppress("NOTHING_TO_INLINE")

package core

sealed class Param<out T> {
    data class Value<out T>(val value: T): Param<T>() {
        override fun invoke() = value
    }
    data class Function<out T>(val compute: () -> T): Param<T>() {
        override fun invoke() = compute()
    }
    abstract operator fun invoke(): T
}

inline fun <T> param(value: T) = Param.Value(value)
inline fun <T> param(noinline compute: () -> T) = Param.Function(compute)

fun <T, X> map(p: Param<T>, mapping: (value: T) -> X): Param<X> = when (p) {
    is Param.Value -> Param.Value(mapping(p.value))
    is Param.Function -> memorize(mapping).let {
        Param.Function { it(p.compute()) }
    }
}

fun <T1, T2, X> map(p1: Param<T1>, p2: Param<T2>, mapping: (v1: T1, v2: T2) -> X): Param<X> {
    return if (p1 is Param.Value && p2 is Param.Value) {
        Param.Value(mapping(p1.value, p2.value))
    } else {
        memorize(mapping).let {
            Param.Function { it(p1(), p2()) }
        }
    }
}

fun <T1, T2, T3, X> map(p1: Param<T1>, p2: Param<T2>, p3: Param<T3>, mapping: (v1: T1, v2: T2, v3: T3) -> X): Param<X> {
    return if (p1 is Param.Value && p2 is Param.Value && p3 is Param.Value) {
        Param.Value(mapping(p1.value, p2.value, p3.value))
    } else {
        memorize(mapping).let {
            Param.Function { it(p1(), p2(), p3()) }
        }
    }
}

fun <T1, T2, T3, T4, X> map(p1: Param<T1>, p2: Param<T2>, p3: Param<T3>, p4: Param<T4>, mapping: (v1: T1, v2: T2, v3: T3, v4: T4) -> X): Param<X> {
    return if (p1 is Param.Value && p2 is Param.Value && p3 is Param.Value && p4 is Param.Value) {
        Param.Value(mapping(p1.value, p2.value, p3.value, p4.value))
    } else {
        memorize(mapping).let {
            Param.Function { it(p1(), p2(), p3(), p4()) }
        }
    }
}

fun <T1, T2, T3, T4, T5, X> map(p1: Param<T1>, p2: Param<T2>, p3: Param<T3>, p4: Param<T4>, p5: Param<T5>, mapping: (v1: T1, v2: T2, v3: T3, v4: T4, v5: T5) -> X): Param<X> {
    return if (p1 is Param.Value && p2 is Param.Value && p3 is Param.Value && p4 is Param.Value && p5 is Param.Value) {
        Param.Value(mapping(p1.value, p2.value, p3.value, p4.value, p5.value))
    } else {
        memorize(mapping).let {
            Param.Function { it(p1(), p2(), p3(), p4(), p5()) }
        }
    }
}

fun <T1, T2, T3, T4, T5, T6, X> map(p1: Param<T1>, p2: Param<T2>, p3: Param<T3>, p4: Param<T4>, p5: Param<T5>, p6: Param<T6>, mapping: (v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6) -> X): Param<X> {
    return if (p1 is Param.Value && p2 is Param.Value && p3 is Param.Value && p4 is Param.Value && p5 is Param.Value && p6 is Param.Value) {
        Param.Value(mapping(p1.value, p2.value, p3.value, p4.value, p5.value, p6.value))
    } else {
        memorize(mapping).let {
            Param.Function { it(p1(), p2(), p3(), p4(), p5(), p6()) }
        }
    }
}
