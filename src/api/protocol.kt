package api

import core.hash
import editor.renderer.StreamItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMessage {
    companion object {
        private var counter: Long = 0L
        private fun nextId() = "cm${counter++}"
    }

    /**
     * Message id
     */
    val mid: String = nextId()

    @Serializable
    @SerialName("Ping")
    class Ping() : ClientMessage()

    @Serializable
    @SerialName("CreateAnonymousAccount")
    class CreateAnonymousAccount : ClientMessage()

    @Serializable
    @SerialName("LoginAnonymous")
    data class LoginAnonymous(val token: String) : ClientMessage()

    @Serializable
    @SerialName("CreateDocument")
    class CreateDocument() : ClientMessage()

    @Serializable
    @SerialName("Disconnect")
    class Logout : ClientMessage()

    @Serializable
    @SerialName("SaveChunk")
    data class SaveChunk(
        val document: String,
        val time: Double,
        val parent: Double?,
        val content: Array<StreamItem>
    ) : ClientMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class.js != other::class.js) return false

            other as SaveChunk

            if (document != other.document) return false
            if (time != other.time) return false
            if (parent != other.parent) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode() =
            document hash
            time hash
            (parent ?: 0) hash
            content.contentHashCode()
    }
}

@Serializable
sealed class ServerMessage {
    val rid: String? = null

    @Serializable
    @SerialName("Pong")
    data class Pong(val time: Double) : ServerMessage()

    @Serializable
    @SerialName("ServerError")
    data class ServerError(val code: String) : ServerMessage()

    @Serializable
    @SerialName("AnonymousAccountCreated")
    data class AnonymousAccountCreated(val id: String, val token: String) : ServerMessage()

    @Serializable
    @SerialName("LoginSuccess")
    data class LoginSuccess(val user: String) : ServerMessage()

    @Serializable
    @SerialName("DocumentCreated")
    data class DocumentCreated(val document: String) : ServerMessage()

    @Serializable
    @SerialName("ChunkSaved")
    class ChunkSaved : ServerMessage()

    @Serializable
    class None : ServerMessage()
}