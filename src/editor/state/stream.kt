package editor.state
import core.ImmutableList
import core.ListNode
import core.immutableListOf
import editor.renderer.StreamItem
import editor.renderer.StreamItemType
import state.Action
import state.selector

sealed class StreamAction : Action<LustresState, DrawStream?> {
    object Reset : StreamAction() {
        override fun apply(state: DrawStream?) = DrawStream(null)
    }

    data class Insert(val item: StreamItem) : StreamAction() {
        override fun apply(state: DrawStream?): DrawStream? {
            return state?.let { _ ->
                val head = state.items?.head
                if (head?.type == StreamItemType.DRAW && item.type == StreamItemType.DRAW) {
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

            /**/
        }
    }

    override fun read(state: LustresState) = state.drawStream
    override fun write(state: LustresState, local: DrawStream?) = state.copy(drawStream = local)
}

/**
 * Sequence of Pair(ItemId, Item)
 */
data class DrawStream(val items: ImmutableList<StreamItem>)

val selectDrawStreamState = selector { state: LustresState -> state.drawStream ?: DrawStream(immutableListOf()) }
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