package editor.state
import core.Id
import core.ImmutableList
import core.ListNode
import core.immutableListOf
import editor.renderer.StreamItem
import editor.renderer.StreamItemType
import state.Action
import state.selector

sealed class StreamAction : Action<LustresState, DrawStream> {
    data class Reset(
        val title: String? = null,
        val size: Pair<Int, Int> = Pair(1280, 720)
    ) : StreamAction() {
        override fun apply(state: DrawStream): DrawStream {
            val layer = Id()
            return DrawStream(
                immutableListOf(
                    StreamItem.Init(size),
                    StreamItem.CreateLayer(layer),
                    StreamItem.SelectLayer(layer)
                )
            )
        }
    }

    /**
     *  store.dispatch(StreamAction.Reset)
     *   store.dispatch(StreamAction.Insert())
     *   store.dispatch(StreamAction.Insert())
     *   store.dispatch(StreamAction.Insert())
     */

    data class Insert(val item: StreamItem) : StreamAction() {
        override fun apply(state: DrawStream): DrawStream {
            val head = state.items?.head
            return if (head?.type == StreamItemType.DRAW && item.type == StreamItemType.DRAW) {
                val drawItem1 = head.unsafeCast<StreamItem.Draw>()
                val drawItem2 = item.unsafeCast<StreamItem.Draw>()
                if (drawItem1.point.point == drawItem2.point.point) {
                    state.copy(
                            items = ListNode(item, state.items.tail)
                    )
                } else {
                    state.copy(
                            items = ListNode(item, state.items)
                    )
                }
            } else {
                state.copy(
                        items = ListNode(item, state.items)
                )
            }
        }
    }

    override fun read(state: LustresState) = state.drawStream
    override fun write(state: LustresState, local: DrawStream) = state.copy(drawStream = local)
}

data class DrawStream(
    val items: ImmutableList<StreamItem> = Id().let { layer ->
        immutableListOf(
            StreamItem.Init(Pair(1280, 720)),
            StreamItem.CreateLayer(layer),
            StreamItem.SelectLayer(layer)
        )
    },
    val title: String? = null,
    val id: String? = null
)

val selectDrawStreamState = selector { state: LustresState -> state.drawStream }
val selectDrawStream = selector(selectDrawStreamState) { it.items }
val selectDocumentSize = selector(selectDrawStream) { stream ->
    stream?.find {
        it.type == StreamItemType.INIT
    }?.unsafeCast<StreamItem.Init>()?.size ?: Pair(0, 0)
}
val selectDocumentWidth = selector(selectDocumentSize) { it.first }
val selectDocumentHeight = selector(selectDocumentSize) { it.second }
val selectActiveLayer = selector(selectDrawStream) { stream ->
    stream?.find { it is StreamItem.SelectLayer }?.let {
        val item = it
        if (item is StreamItem.SelectLayer) {
            item.id
        } else {
            null
        }
    }
}
val selectTransactionBeginTime = selector(selectDrawStream) { stream ->
    stream?.find { item -> item is StreamItem.Begin }?.time
}