package editor.state

import core.Id
import state.Action
import state.selector

sealed class DocumentAction : Action<LustresState, Document?> {
    data class Create(val id: Id, val title: String?) : DocumentAction() {
        override fun apply(state: Document?) = Document(
                id,
                title
        )
    }

    data class Open(val id: Id) : DocumentAction() {
        override fun apply(state: Document?): Document? {
            TODO("Not implemented")
        }
    }

    object Close : DocumentAction() {
        override fun apply(state: Document?): Document? = null
    }

    override fun read(state: LustresState) = state.document
    override fun write(state: LustresState, local: Document?) = state.copy(document = local)
}

data class Document(
        val id: Id,
        val title: String? = null
)

val selectDocument = selector { state: LustresState -> state.document }
val selectHasDocument = selector(selectDocument) { it != null }