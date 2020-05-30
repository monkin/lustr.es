package editor.ui

import core.*
import draw.Bounds
import draw.Gl
import draw.blend
import draw.toSRgb
import editor.renderer.Renderer
import editor.renderer.StreamItem
import editor.state.*
import imports.*
import kotlinx.css.*
import material.*
import oneact.*
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.url.URL
import state.Store
import kotlin.browser.document

private val trueByDefault = param(true)

private val dividerClass = cl("main-menu-button-divider")
private val buttonsSection = cl("main-menu-buttons-section")
private val styled = style {
    ".$dividerClass" {
        borderBottom = "solid 1px ${ThemeColor.BORDER}"
        width = 24.px
        margin = "4px"
    }
    ".$buttonsSection" {
        margin(0.px, (-16).px)
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
        redo: () -> Unit,
        download: () -> Unit,
        sprayInput: () -> El,
        eraserInput: () -> El,
        brushInput: () -> El,
        pencilInput: () -> El
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
                            MenuTab.MAIN_MENU to {
                                    el(
                                            className(buttonsSection),
                                            menuButton(
                                                    iconImage = param(mdiDownload),
                                                    caption = param("Download PNG"),
                                                    action = download
                                            )
                                    )
                            },
                            MenuTab.SPRAY to { sprayInput() },
                            MenuTab.ERASER to { eraserInput() },
                            MenuTab.BRUSH to { brushInput() },
                            MenuTab.PENCIL to { pencilInput() }
                    )
            ),
            open = isOpen,
            onHideRequest = closeMenu
    ))
}

fun lustresMenuConnected(store: Store<LustresState>): El {
    val state = store.state
    return lustresMenu(
            openMenu = { tab -> store.dispatch(MenuAction.Open(tab)) },
            closeMenu = { store.dispatch(MenuAction.Close) },
            selectTool = { tool -> store.dispatch(MenuAction.SelectTool(tool)) },
            isOpen = map(state) { selectIsMenuOpen(it) },
            tab = map(state) { selectMenuTab(it) },
            tool = map(state) { selectActiveTool(it) },
            undo = { store.dispatch(StreamAction.Insert(StreamItem.Undo())) },
            redo = { store.dispatch(StreamAction.Insert(StreamItem.Redo())) },
            download = {
                    val width = selectDocumentWidth(state())
                    val height = selectDocumentHeight(state())

                    val canvas = document.createElement("canvas") as HTMLCanvasElement
                    canvas.style.width = "${width.toFixed(0)}px"
                    canvas.style.height = "${height.toFixed(0)}px"
                    canvas.style.position = "fixed"
                    canvas.style.top = "100%"
                    canvas.style.left = "0"
                    canvas.width = width
                    canvas.height = height

                    document.body!!.appendChild(canvas)
                    context {

                            val gl = disposable(Gl(canvas))
                            val renderer = disposable(Renderer(gl))

                            val layers = disposable(renderer.render(selectDrawStream(state())))

                            layers.compose { texture ->
                                    gl.settings()
                                            .clearColor(1.0, 1.0, 1.0, 1.0)
                                            .viewport(0, 0, width, height)
                                            .blend(false)
                                            .apply {
                                                    gl.cleanColorBuffer()
                                                    texture.draw(
                                                            Bounds(0.0, 0.0, 1.0, 1.0),
                                                            Bounds(0.0, 0.0, 1.0, 1.0)
                                                    ) { color ->
                                                            toSRgb(blend(color, float4(1.0)))
                                                    }
                                            }

                            }

                            canvas.toBlob({ blob ->
                                    canvas.remove()

                                    val a = document.createElement("a") as HTMLAnchorElement
                                    val url = URL.createObjectURL(blob!!)
                                    a.href = url
                                    a.download = "image.png"
                                    a.click()
                                    URL.revokeObjectURL(url)
                            }, "image/png")

                    }
            },
            brushInput = { brushInputConnected(store) },
            eraserInput = { eraserInputConnected(store) },
            pencilInput = { pencilInputConnected(store) },
            sprayInput = { sprayInputConnected(store) }
    )
}