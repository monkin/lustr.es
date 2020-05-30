package editor

import core.map
import editor.state.*
import editor.ui.*
import kotlinx.css.*
import oneact.*

private val layoutClass = cl("layout")
private val middleClass = cl("middle")

private val styled = style {
    ".$layoutClass" {
        width = 100.pct
        height = 100.pct
        display = Display.flex
        alignItems = Align.stretch
    }
    ".$middleClass" {
        position = Position.relative
        flexGrow = 1.0
        flexShrink = 1.0
    }
}

fun lustres(): El {
    return state(LustresState()) { appState, setAppState ->
        val store = createLustresStore { setAppState(it) }
        val hasDocument = map(appState) { it.document != null }
        styled(el(
                className(layoutClass),
                newDocumentDialogConnected(
                    isOpen = map(hasDocument) { !it },
                    store = store
                ),
                optional(hasDocument) {
                    children(
                            lustresMenuConnected(store),
                            el(
                                    className(middleClass),
                                    drawSurfaceConnected(store),
                                    colorDialogConnected(store)
                            ),
                            colorsPanelConnected(store)
                    )
                }
        ))
    }
}
