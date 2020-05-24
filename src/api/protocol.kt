package api

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
}

@Serializable
sealed class ServerMessage {
    val rid: String? = null;

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
    data class DocumentCreated(val documentId: String) : ServerMessage()

    @Serializable
    class None : ServerMessage()
}