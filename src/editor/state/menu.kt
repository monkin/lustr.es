package editor.state

import state.Action
import state.selector

enum class ActiveTool(val menuTab: MenuTab) {
    BRUSH(MenuTab.BRUSH),
    PENCIL(MenuTab.PENCIL),
    ERASER(MenuTab.ERASER),
    SPRAY(MenuTab.SPRAY)
}

enum class MenuTab {
    NONE,
    MAIN_MENU,
    BRUSH,
    PENCIL,
    SPRAY,
    ERASER,
    LAYERS,
}

data class MenuState(
        val tab: MenuTab = MenuTab.NONE,
        val tool: ActiveTool = ActiveTool.PENCIL
)

sealed class MenuAction : Action<LustresState, MenuState> {
    data class Open(val tab: MenuTab) : MenuAction() {
        override fun apply(state: MenuState) = state.copy(tab = tab)
    }

    object Close : MenuAction() {
        override fun apply(state: MenuState) = state.copy(tab = MenuTab.NONE)
    }
    class SelectTool(val tool: ActiveTool) : MenuAction() {
        override fun apply(state: MenuState) = if (state.tool != tool && state.tab == MenuTab.NONE) {
            state.copy(tool = tool)
        } else {
            state.copy(
                    tool = tool,
                    tab = tool.menuTab
            )
        }
    }

    override fun read(state: LustresState) = state.menu
    override fun write(state: LustresState, local: MenuState) = state.copy(menu = local)
}

val selectMenu = selector { state: LustresState -> state.menu }
val selectActiveTool = selector(selectMenu) { it.tool }
val selectMenuTab = selector(selectMenu) { it.tab }
val selectIsMenuOpen = selector(selectMenuTab) { it !== MenuTab.NONE }