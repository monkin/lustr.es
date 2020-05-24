package editor.ui

import core.*
import draw.*
import draw.Touch
import editor.renderer.Renderer
import editor.renderer.StreamItem
import editor.state.*
import kotlinx.css.*
import kotlinx.css.Color
import oneact.*
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.WheelEvent
import state.Store
import kotlin.browser.window
import kotlin.js.Date
import kotlin.js.Promise

private val drawSurfaceClass = cl("document-view")
private val canvasClass = cl("document-canvas")

private sealed class TouchState {
    object None: TouchState()
    data class Draw(
            val pointerId: Double,
            val startTime: Double,
            val startPoint: Vector,
            val lastPoint: Vector,
            val lastPressure: Double
    ): TouchState() {
        fun isPinchable(time: Double) =
                (time - startTime) < 150
                && (lastPoint - startPoint).length < 20
    }

    data class Pinch(
            val pinch: Pair<Double, Double>,
            val points: Pair<Vector, Vector>,
            val transform: (Vector) -> Vector
    ): TouchState()
}

private val styled = style {
    ".$drawSurfaceClass" {
        position = Position.absolute
        left = 0.px
        top = 0.px
        right = 0.px
        bottom = 0.px
        backgroundColor = Color("#EEE")
        outline = Outline.none
        put("touch-action", "none")
    }
    ".$canvasClass" {
        position = Position.absolute
        backgroundColor = Color.white
        put("transform-origin", "0 0")
    }
}

private fun oncePerFrame(callback: () -> Unit): () -> Unit {
    var requested = false
    return {
        if (!requested) {
            requested = true
            Promise.resolve(Unit).then {
                try {
                    callback()
                } finally {
                    requested = false
                }
            }
        }
    }
}

fun drawSurface(
        stream: Param<ImmutableList<StreamItem>>,

        width: Param<Int>,
        height: Param<Int>,
        orientation: Param<Orientation>,

        beginPinch: (Pair<Vector, Vector>) -> Unit,
        continuePinch: (Pair<Vector, Vector>) -> Unit,
        commitPinch: () -> Unit,
        rollbackPinch: () -> Unit,

        beginDraw: () -> Unit,
        continueDraw: (Touch) -> Unit,
        commitDraw: () -> Unit,
        rollbackDraw: () -> Unit,

        zoomInAt: (Vector) -> Unit,
        zoomOutAt: (Vector) -> Unit
) = state(Pair(0.0, 0.0)) { nodeSize, setNodeSize ->
    state<TouchState>(TouchState.None) { touchState, setTouchState ->
        val pixelRatio = param { window.devicePixelRatio }
        val transform = map(
                width,
                height,
                nodeSize,
                pixelRatio,
                orientation
        ) { width, height, (nodeWidth, nodeHeight), ratio, orientation ->
            Matrix
                    .shift(
                            -width.toDouble() / 2,
                            -height.toDouble() / 2
                    )
                    .scale(1.0 / ratio)
                    .shift(nodeWidth / 2, nodeHeight / 2) * orientation.transformation

        }
        var surfaceBounds = param { Bounds(0.0, 0.0, 0.0, 0.0) }
        val throttledZoomInAt = throttle(100, zoomInAt)
        val throttledZoomOutAt = throttle(100, zoomOutAt)
        val clientToDocumentSpace = {
            val bounds = surfaceBounds()
            val t = transform().inverse();
            { v: Vector ->
                 t * (v - Vector(bounds.x1, bounds.y1))
            }
        }
        val canvas = el("canvas",
                className(canvasClass),
                attr("width", map(width) { it.toFixed(0) }),
                attr("height", map(height) { it.toFixed(0) }),
                attr("style", inline(
                        "width" to map(width) { width ->
                            (width).toFixed(2) + "px"
                        },
                        "height" to map(height) { height ->
                            (height).toFixed(2) + "px"
                        },
                        "transform" to map(transform) { it.toCss() }
                ))
        )
        val gl = Gl(canvas.node.only as HTMLCanvasElement)
        val renderer = Renderer(gl)

        val node = styled(resizeListener(el(
                className(drawSurfaceClass),
                attr("tabindex", "0"),
                afterUpdate(canvas, oncePerFrame {
                    renderer.render(stream()).use { layers ->
                        layers.compose { texture ->
                            gl.settings()
                                    .clearColor(1.0, 1.0, 1.0, 1.0)
                                    .viewport(0, 0, width(), height())
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
                    }
                }),
                handler("mousewheel") { e: WheelEvent ->
                    val bounds = (e.currentTarget as HTMLElement).getBoundingClientRect()
                    val point = clientToDocumentSpace()(e.clientPoint - Vector(bounds.left, bounds.top))
                    when {
                        e.deltaY > 0 -> throttledZoomOutAt(point)
                        e.deltaY < 0 -> throttledZoomInAt(point)
                    }
                    e.preventDefault()
                },
                handler("pointerdown") { e: PointerEvent ->
                    setTouchState { touchState ->
                        when (touchState) {
                            TouchState.None -> {
                                val now = Date.now()
                                beginDraw()
                                continueDraw(Touch(
                                        clientToDocumentSpace()(e.clientPoint),
                                        now,
                                        e.pressure
                                ))
                                TouchState.Draw(e.pointerId, now, e.clientPoint, e.clientPoint, e.pressure)
                            }
                            is TouchState.Draw -> {
                                val now = Date.now()
                                val t = clientToDocumentSpace()
                                if (touchState.isPinchable(now)) {
                                    rollbackDraw()
                                    beginPinch(Pair(
                                            t(touchState.lastPoint),
                                            t(e.clientPoint)
                                    ))
                                    TouchState.Pinch(
                                            Pair(touchState.pointerId, e.pointerId),
                                            Pair(touchState.lastPoint, e.clientPoint),
                                            t
                                    )
                                } else {
                                    touchState
                                }
                            }
                            else -> touchState
                        }
                    }
                },
                handler("pointermove") { e: PointerEvent ->
                    setTouchState { touchState ->
                        val t = clientToDocumentSpace()
                        when (touchState) {
                            is TouchState.Draw -> {
                                if (e.pointerId == touchState.pointerId) {
                                    continueDraw(Touch(
                                            t(e.clientPoint),
                                            Date.now(),
                                            e.pressure
                                    ))
                                    touchState.copy(lastPoint = e.clientPoint, lastPressure = e.pressure)
                                } else {
                                    touchState
                                }
                            }
                            is TouchState.Pinch -> {
                                when (e.pointerId) {
                                    touchState.pinch.first -> {
                                        continuePinch(Pair(
                                            touchState.transform(e.clientPoint),
                                            touchState.transform(touchState.points.second)
                                        ))
                                        touchState.copy(
                                            points = Pair(
                                                e.clientPoint,
                                                touchState.points.second
                                            )
                                        )
                                    }
                                    touchState.pinch.second -> {
                                        continuePinch(Pair(
                                            touchState.transform(touchState.points.first),
                                            touchState.transform(e.clientPoint)
                                        ))
                                        touchState.copy(
                                            points = Pair(
                                                touchState.points.first,
                                                e.clientPoint
                                            )
                                        )
                                    }
                                    else -> touchState
                                }
                            }
                            else -> touchState
                        }
                    }
                },
                handler("pointerup") { e: PointerEvent ->
                    setTouchState { touchState ->
                        when {
                            touchState is TouchState.Draw && e.pointerId == touchState.pointerId -> {
                                commitDraw()
                                TouchState.None
                            }
                            touchState is TouchState.Pinch && (e.pointerId == touchState.pinch.first || e.pointerId == touchState.pinch.second) -> {
                                commitPinch()
                                TouchState.None
                            }
                            else -> touchState
                        }
                    }
                },
                handler<PointerEvent>("pointercancel") {
                    setTouchState { touchState ->
                        when (touchState) {
                            is TouchState.Draw -> rollbackDraw()
                            is TouchState.Pinch -> rollbackPinch()
                            else -> {}
                        }
                        TouchState.None
                    }
                }
        )) { width, height ->
            setNodeSize(Pair(width, height))
        }).afterUpdate {
            if (touchState() is TouchState.Draw) {
                window.requestAnimationFrame {
                    setTouchState { state ->
                        if (state is TouchState.Draw) {
                            val t = clientToDocumentSpace()
                            continueDraw(Touch(
                                    t(state.lastPoint),
                                    Date.now(),
                                    state.lastPressure
                            ))
                        }

                        state
                    }
                }
            }
        }

        surfaceBounds = param {
            val rect = (node.node.only as HTMLElement).getBoundingClientRect()
            Bounds(rect.left, rect.top, rect.right, rect.bottom)
        }
        node
    }
}

fun drawSurfaceConnected(): El {
    val store = Context.get<Store<LustresState>>()
    val state = store.state
    return drawSurface(
            stream = map(state) { selectDrawStream(it) },
            width = map(state) { selectDocumentWidth(it) },
            height = map(state) { selectDocumentHeight(it) },
            orientation = map(state) { it.orientation },

            beginPinch = { store.dispatch(OrientationAction.BeginPinch(it)) },
            continuePinch = { store.dispatch(OrientationAction.ContinuePinch(it)) },
            commitPinch = { store.dispatch(OrientationAction.CommitPinch) },
            rollbackPinch = { store.dispatch(OrientationAction.RollbackPinch) },

            zoomInAt = { point -> store.dispatch(OrientationAction.ZoomInAt(point)) },
            zoomOutAt = { point -> store.dispatch(OrientationAction.ZoomOutAt(point)) },

            beginDraw = {
                val color = selectColor(state())
                val tool = selectTool(state())
                store.dispatch(StreamAction.Insert(StreamItem.Begin(tool, color)))
                store.dispatch(PaletteAction.UseColor(color))
            },
            continueDraw = { touch ->
                store.dispatch(StreamAction.Insert(StreamItem.Draw(touch)))
            },
            commitDraw = {
                store.dispatch(StreamAction.Insert(StreamItem.Commit()))
            },
            rollbackDraw = {
                store.dispatch(StreamAction.Insert(StreamItem.Rollback()))
            }
    )
}