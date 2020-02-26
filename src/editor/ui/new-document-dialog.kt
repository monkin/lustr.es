package editor.ui

import core.Id
import core.Param
import core.param
import core.map
import editor.renderer.Layer
import editor.renderer.StreamItem
import editor.state.*
import imports.mdiSwapHorizontal
import kotlinx.css.*
import material.*
import oneact.*
import state.Store

private data class DialogState(val name: String = "Untitled", val width: Int = 1920, val height: Int = 1080)

private val formRowClass = cl("form-row")
private val sizeInputClass = cl("size-input")
private val swapButtonClass = cl("swap-button")

private val styled = style {
    ".$formRowClass" {
        display = Display.flex
        flexDirection = FlexDirection.row
        alignItems = Align.flexEnd
    }
    ".$sizeInputClass" {
        flexBasis = FlexBasis.zero
        flexGrow = 1.0
        flexShrink = 1.0
    }
    ".$swapButtonClass" {
        margin(0.px, 8.px)
    }
}

fun newDocumentDialog(
        create: (name: String?, size: Pair<Int, Int>) -> Unit,
        isOpen: Param<Boolean>
) = styled(state(DialogState()) { s, setState ->
    dialog(
            open = isOpen,
            title = { dialogTitle("New Document") },
            body = {
                dialogBody(
                        stringInput(
                                label = param("Name"),
                                value = map(s) { it.name },
                                valid = map(s) { it.name != "" },
                                onChange = { v -> setState { s -> s.copy(name = v) } }
                        ),
                        el(
                                className(formRowClass),
                                el(
                                        className(sizeInputClass),
                                        numberInput(
                                                label = param("Width"),
                                                value = map(s) { it.width.toDouble() },
                                                onChange = { w -> setState { s -> s.copy(width = w.toInt()) } },
                                                showSlider = param(false),
                                                isInteger = param(true),
                                                min = param(1.0),
                                                max = param(6000.0)
                                        )
                                ),
                                iconButton(
                                        className = param(swapButtonClass),
                                        image = param(mdiSwapHorizontal),
                                        action = {
                                            setState { s -> s.copy(width = s.height, height = s.width) }
                                        }
                                ),
                                el(
                                        className(sizeInputClass),
                                        numberInput(
                                                label = param("Height"),
                                                value = map(s) { it.height.toDouble() },
                                                onChange = { w -> setState { s -> s.copy(height = w.toInt()) } },
                                                showSlider = param(false),
                                                isInteger = param(true),
                                                min = param(1.0),
                                                max = param(6000.0)
                                        )
                                )
                        )
                )
            },
            buttons = {
                dialogButtons(
                        flatButton(
                                caption = param("Create")
                        ) {
                            val (title, width, height) = s()
                            create(title, Pair(width, height))
                        }
                )
            }
    )
})

fun newDocumentDialogConnected(
        isOpen: Param<Boolean>
): El {
    val store = Context.get<Store<LustresState>>()
    return newDocumentDialog(
            create = { title, size ->
                val layer = Id()
                store.dispatch(DocumentAction.Create(Id(), title))
                store.dispatch(StreamAction.Reset)
                store.dispatch(StreamAction.Insert(StreamItem.Init(size)))
                store.dispatch(StreamAction.Insert(StreamItem.CreateLayer(layer)))
                store.dispatch(StreamAction.Insert(StreamItem.SelectLayer(layer)))
                store.dispatch(StreamAction.Insert(StreamItem.ResetCancellation()))
            },
            isOpen = isOpen
    )
}
