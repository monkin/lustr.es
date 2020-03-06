package core

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
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


@Serializable(with = IdSerializer::class)
data class Id(val value: String = floor(Date.now()).toLong().let {
    if (it == time) {
        stringify(it) + "~" + stringify(counter++)
    } else {
        time = it
        counter = 0
        stringify(it)
    }
})

@Serializer(forClass = Id::class)
class IdSerializer : KSerializer<Id> {
    private val serializer = String.serializer()

    override val descriptor = PrimitiveDescriptor("Id", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Id) = serializer.serialize(encoder, value.value)
    override fun deserialize(decoder: Decoder): Id = Id(serializer.deserialize(decoder))
}