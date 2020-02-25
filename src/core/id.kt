package core

import kotlin.js.Date
import kotlin.math.floor

private var time: Long = 0
private var counter: Long = 0
private val chars = (arrayOf(
        '0'.rangeTo('9'),
        'a'.rangeTo('z'),
        'A'.rangeTo('Z')
).fold(arrayOf<Char>()) { acc, range ->
    acc + range.toList()
} + arrayOf('-', '_')).map { it.toString() }

private tailrec fun stringify(value: Long, suffix: String = ""): String = if (value == 0L) {
    suffix
} else {
    stringify(
            value / chars.size,
            chars[(value % chars.size.toLong()).toInt()] + suffix
    )
}

data class Id<T>(val value: String = floor(Date.now()).toLong().let {
    if (it == time) {
        stringify(it) + "~" + stringify(counter++)
    } else {
        time = it
        counter = 0
        stringify(it)
    }
})