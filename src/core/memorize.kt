package core

fun <A, R> memorize(fn: (a: A) -> R): (a: A) -> R {
    var memo: Pair<A, R>? = null
    return { a ->
        memo?.let { (oldA, oldR) ->
            if (oldA === a) {
                oldR
            } else {
                fn(a).also { r -> memo = Pair(a, r) }
            }
        } ?: fn(a).also { r -> memo = Pair(a, r) }
    }
}

fun <A1, A2, R> memorize(fn: (a1: A1, a2: A2) -> R): (a1: A1, a2: A2) -> R {
    val carried = memorize { a1: A1 ->
        memorize { a2: A2 -> fn(a1, a2) }
    }
    return { a1, a2 -> carried(a1)(a2) }
}

fun <A1, A2, A3, R> memorize(fn: (a1: A1, a2: A2, a3: A3) -> R): (a1: A1, a2: A2, a3: A3) -> R {
    val carried = memorize { a1: A1, a2: A2 ->
        memorize { a3: A3 -> fn(a1, a2, a3) }
    }
    return { a1, a2, a3 -> carried(a1, a2)(a3) }
}

fun <A1, A2, A3, A4, R> memorize(fn: (a1: A1, a2: A2, a3: A3, a4: A4) -> R): (a1: A1, a2: A2, a3: A3, a4: A4) -> R {
    val carried = memorize { a1: A1, a2: A2 ->
        memorize { a3: A3, a4: A4 -> fn(a1, a2, a3, a4) }
    }
    return { a1, a2, a3, a4 -> carried(a1, a2)(a3, a4) }
}

fun <A1, A2, A3, A4, A5, R> memorize(fn: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5) -> R): (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5) -> R {
    val carried = memorize { a1: A1, a2: A2 ->
        memorize { a3: A3, a4: A4, a5: A5 -> fn(a1, a2, a3, a4, a5) }
    }
    return { a1, a2, a3, a4, a5 -> carried(a1, a2)(a3, a4, a5) }
}

fun <A1, A2, A3, A4, A5, A6, R> memorize(fn: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6) -> R): (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6) -> R {
    val carried = memorize { a1: A1, a2: A2, a3: A3 ->
        memorize { a4: A4, a5: A5, a6: A6 -> fn(a1, a2, a3, a4, a5, a6) }
    }
    return { a1, a2, a3, a4, a5, a6 -> carried(a1, a2, a3)(a4, a5, a6) }
}

fun <A1, A2, A3, A4, A5, A6, A7, R> memorize(fn: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7) -> R): (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7) -> R {
    val carried = memorize { a1: A1, a2: A2, a3: A3 ->
        memorize { a4: A4, a5: A5, a6: A6, a7: A7 -> fn(a1, a2, a3, a4, a5, a6, a7) }
    }
    return { a1, a2, a3, a4, a5, a6, a7 -> carried(a1, a2, a3)(a4, a5, a6, a7) }
}

fun <A1, A2, A3, A4, A5, A6, A7, A8, R> memorize(fn: (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8) -> R): (a1: A1, a2: A2, a3: A3, a4: A4, a5: A5, a6: A6, a7: A7, a8: A8) -> R {
    val carried = memorize { a1: A1, a2: A2, a3: A3, a4: A4 ->
        memorize { a5: A5, a6: A6, a7: A7, a8: A8 -> fn(a1, a2, a3, a4, a5, a6, a7, a8) }
    }
    return { a1, a2, a3, a4, a5, a6, a7, a8 -> carried(a1, a2, a3, a4)(a5, a6, a7, a8) }
}