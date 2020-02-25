package editor.ui

import core.Param
import core.map
import core.param
import editor.renderer.StreamItem
import editor.state.*
import imports.*
import kotlinx.css.*
import material.*
import oneact.*
import state.Store

private val trueByDefault = param(true)

private val dividerClass = cl("main-menu-button-divider")
private val styled = style {
    ".$dividerClass" {
        borderBottom = "solid 1px ${ThemeColor.BORDER}"
        width = 24.px
        margin = "4px"
    }
}

fun lustresMenu(
        openMenu: (MenuTab) -> Unit,
        selectTool: (ActiveTool) -> Unit,
        closeMenu: () -> Unit,
        isOpen: Param<Boolean>,
        tab: Param<MenuTab>,
        tool: Param<ActiveTool>,
        undo: () -> Unit,
        redo: () -> Unit
): El {
    return styled(mainMenu(
            buttons = children(
                    mainMenuButton(
                            image = param(mdiMenu),
                            open = map(tab) { it == MenuTab.MAIN_MENU },
                            action = { openMenu(MenuTab.MAIN_MENU) },
                            selected = param(false),
                            enabled = trueByDefault
                    ),
                    mainMenuButton(
                            image = param(mdiPencil),
                            open = map(tab) { it == MenuTab.PENCIL },
                            action = {  selectTool(ActiveTool.PENCIL) },
                            selected = map(tool) { it == ActiveTool.PENCIL },
                            enabled = trueByDefault
                    ),
                    mainMenuButton(
                            image = param(mdiBrush),
                            open = map(tab) { it == MenuTab.BRUSH },
                            action = { selectTool(ActiveTool.BRUSH) },
                            selected = map(tool) { it == ActiveTool.BRUSH },
                            enabled = trueByDefault
                    ),
                    mainMenuButton(
                            image = param(mdiSpray),
                            open = map(tab) { it == MenuTab.SPRAY },
                            action = { selectTool(ActiveTool.SPRAY) },
                            selected = map(tool) { it == ActiveTool.SPRAY },
                            enabled = trueByDefault
                    ),
                    /*mainMenuButton(
                            image = param(iconImageEraser()),
                            open = map(tab) { it == MenuTab.ERASER },
                            action = { selectTool(ActiveTool.ERASER) },
                            selected = map(tool) { it == ActiveTool.ERASER },
                            enabled = trueByDefault
                    ),
                    mainMenuButton(
                            image = param(iconImageLayers()),
                            open = map(tab) { it == MenuTab.LAYERS },
                            action = { openMenu(MenuTab.LAYERS) },
                            selected = param(false),
                            enabled = trueByDefault
                    ),*/
                    el(className(dividerClass)),
                    iconButton(
                            image = param(mdiUndo),
                            action = undo
                    ),
                    iconButton(
                            image = param(mdiRedo),
                            action = redo
                    )
            ),
            content = sections(
                    active = tab,
                    items = linkedMapOf(
                            MenuTab.SPRAY to { sprayInputConnected() },
                            MenuTab.ERASER to { eraserInputConnected() },
                            MenuTab.BRUSH to { brushInputConnected() },
                            MenuTab.PENCIL to { pencilInputConnected() }
                    )
            ),
            open = isOpen,
            onHideRequest = closeMenu
    ))
}

fun lustresMenuConnected(): El {
    val store = Context.get<Store<LustresState>>()
    val state = store.state
    return lustresMenu(
            openMenu = { tab -> store.dispatch(MenuAction.Open(tab)) },
            closeMenu = { store.dispatch(MenuAction.Close) },
            selectTool = { tool -> store.dispatch(MenuAction.SelectTool(tool)) },
            isOpen = map(state) { selectIsMenuOpen(it) },
            tab = map(state) { selectMenuTab(it) },
            tool = map(state) { selectActiveTool(it) },
            undo = { store.dispatch(StreamAction.Insert(StreamItem.Undo())) },
            redo = { store.dispatch(StreamAction.Insert(StreamItem.Redo())) }
    )
}