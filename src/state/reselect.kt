package state

import core.Param
import core.memorize
import core.map

typealias Selector<S, R> = (state: S) -> R

fun <S, R> selector(compute: Selector<S, R>): Selector<S, R> = memorize(compute)

fun <S, A, R> selector(arg: Selector<S, A>, compute: (a: A) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg(state)) }
}

fun <S, A1, A2, R> selector(arg1: Selector<S, A1>, arg2: Selector<S, A2>, compute: (a1: A1, a2: A2) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg1(state), arg2(state)) }
}

fun <S, A1, A2, A3, R> selector(arg1: Selector<S, A1>, arg2: Selector<S, A2>, arg3: Selector<S, A3>, compute: (a1: A1, a2: A2, a3: A3) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg1(state), arg2(state), arg3(state)) }
}

fun <S, A1, A2, A3, A4, R> selector(arg1: Selector<S, A1>, arg2: Selector<S, A2>, arg3: Selector<S, A3>, arg4: Selector<S, A4>, compute: (a1: A1, a2: A2, a3: A3, a4: A4) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg1(state), arg2(state), arg3(state), arg4(state)) }
}

fun <S, A1, A2, A3, A4, A5, R> selector(arg1: Selector<S, A1>, arg2: Selector<S, A2>, arg3: Selector<S, A3>, arg4: Selector<S, A4>, arg5: Selector<S, A5>, compute: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg1(state), arg2(state), arg3(state), arg4(state), arg5(state)) }
}

fun <S, A1, A2, A3, A4, A5, A6, R> selector(arg1: Selector<S, A1>, arg2: Selector<S, A2>, arg3: Selector<S, A3>, arg4: Selector<S, A4>, arg5: Selector<S, A5>, arg6: Selector<S, A6>, compute: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg1(state), arg2(state), arg3(state), arg4(state), arg5(state), arg6(state)) }
}

fun <S, A1, A2, A3, A4, A5, A6, A7, R> selector(arg1: Selector<S, A1>, arg2: Selector<S, A2>, arg3: Selector<S, A3>, arg4: Selector<S, A4>, arg5: Selector<S, A5>, arg6: Selector<S, A6>, arg7: Selector<S, A7>, compute: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg1(state), arg2(state), arg3(state), arg4(state), arg5(state), arg6(state), arg7(state)) }
}

fun <S, A1, A2, A3, A4, A5, A6, A7, A8, R> selector(arg1: Selector<S, A1>, arg2: Selector<S, A2>, arg3: Selector<S, A3>, arg4: Selector<S, A4>, arg5: Selector<S, A5>, arg6: Selector<S, A6>, arg7: Selector<S, A7>, arg8: Selector<S, A8>, compute: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8) -> R): Selector<S, R> {
    val select = memorize(compute)
    return { state -> select(arg1(state), arg2(state), arg3(state), arg4(state), arg5(state), arg6(state), arg7(state), arg8(state)) }
}

fun <S, R> param(selector: Selector<S, R>): Selector<Param<S>, Param<R>> = { s -> map(s) { selector(it) }}