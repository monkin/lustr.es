package core

import kotlin.math.*

@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Double.toFixed(digits: Int): String = asDynamic().toFixed(digits)
@Suppress("NOTHING_TO_INLINE", "UnsafeCastFromDynamic")
inline fun Int.toFixed(digits: Int): String = asDynamic().toFixed(digits)

private val trimmer = Regex("\\.?0+$")

fun Double.toReadable(precision: Int = 3): String {
    val digits = min(precision, max(0, ceil(precision.toDouble() - log10(abs(this))).toInt() - 1))
    return if (digits == 0) {
        toFixed(digits)
    } else {
        toFixed(digits).replace(trimmer, "")
    }
}