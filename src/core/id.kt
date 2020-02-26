package core

import kotlinx.serialization.*
import kotlinx.serialization.internal.PrimitiveDescriptor
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.StringSerializer
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
    private val serializer = StringSerializer

    override val descriptor = PrimitiveDescriptorWithName("Id", StringDescriptor)
    override fun serialize(encoder: Encoder, obj: Id) = serializer.serialize(encoder, obj.value)
    override fun deserialize(decoder: Decoder): Id = Id(serializer.deserialize(decoder))
}