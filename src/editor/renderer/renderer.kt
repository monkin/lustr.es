package editor.renderer

import art.ArtCanvas
import art.artCanvas
import core.*
import draw.*
import draw.Touch
import editor.state.Tool
import kotlin.js.Date

enum class StreamItemType {
    INIT,
    UNDO,
    REDO,
    RESET_CANCELLATION,
    BEGIN,
    DRAW,
    COMMIT,
    ROLLBACK,
    CREATE_LAYER,
    MOVE_LAYER,
    SELECT_LAYER,
    DELETE_LAYER
}

sealed class StreamItem(val type: StreamItemType) {
    abstract val time: Double

    data class Init(val size: Pair<Int, Int>, override val time: Double = Date.now()) : StreamItem(StreamItemType.INIT)
    data class Undo(override val time: Double = Date.now()) : StreamItem(StreamItemType.UNDO)
    data class Redo(override val time: Double = Date.now()) : StreamItem(StreamItemType.REDO)
    data class ResetCancellation(override val time: Double = Date.now()) : StreamItem(StreamItemType.RESET_CANCELLATION)
    data class Begin(val tool: Tool, val color: Color, override val time: Double = Date.now()) : StreamItem(StreamItemType.BEGIN)
    data class Draw(val point: Touch, override val time: Double = Date.now()) : StreamItem(StreamItemType.DRAW)
    data class Commit(override val time: Double = Date.now()) : StreamItem(StreamItemType.COMMIT)
    data class Rollback(override val time: Double = Date.now()) : StreamItem(StreamItemType.ROLLBACK)
    data class CreateLayer(val id: Id<Layer>, override val time: Double = Date.now()) : StreamItem(StreamItemType.CREATE_LAYER)
    data class MoveLayer(val id: Id<Layer>, val before: Id<Layer>?, override val time: Double = Date.now()) : StreamItem(StreamItemType.MOVE_LAYER)
    data class SelectLayer(val id: Id<Layer>, override val time: Double = Date.now()) : StreamItem(StreamItemType.SELECT_LAYER)
    data class DeleteLayer(val id: Id<Layer>, override val time: Double = Date.now()) : StreamItem(StreamItemType.DELETE_LAYER)
}

data class Layer(val id: Id<Layer>, val canvas: ArtCanvas) : Disposable {
    fun clone() = Layer(id, canvas.clone())
    override fun dispose() {
        canvas.dispose()
    }
}

class RenderedLayers(
        val gl: Gl,
        val active: Id<Layer>?,
        val resolution: Pair<Int, Int>,
        val layers: Array<Layer>
) : Disposable {
    val width = resolution.first
    val height = resolution.second

    fun createLayer(id: Id<Layer>) = RenderedLayers(
            gl = gl,
            active = active,
            resolution = resolution,
            layers = arrayOf(Layer(id, gl.artCanvas(width, height))) + layers
    )

    fun selectLayer(id: Id<Layer>) = RenderedLayers(
            gl = gl,
            active = id,
            resolution = resolution,
            layers = layers
    )

    val canvas
        get() = layers.find { it.id == active }?.let { (_, canvas) ->
            canvas
        }

    fun draw(tool: Tool, color: Color, seed: Int, points: TouchPath): RenderedLayers {
        when (tool) {
            is Tool.None -> { /* just ignore */ }
            is Tool.Spray -> {
                canvas?.spray(
                        path = points,
                        color = color,
                        opacity = tool.opacity,
                        intensity = tool.intensity,
                        granularity = tool.granularity,
                        size = tool.size,
                        seed = seed
                )
            }
            is Tool.Brush -> {
                canvas?.brush(
                        path = points,
                        color = color,
                        opacity = tool.opacity,
                        density = tool.density,
                        size = tool.size,
                        sharpness = tool.sharpness,
                        seed = seed
                )
            }
            is Tool.Pencil -> {
                canvas?.pencil(
                        path = points,
                        color = color,
                        opacity = tool.opacity,
                        size = tool.size,
                        flow = tool.flow,
                        hardness = tool.hardness
                )
            }
            else -> {
                // TODO("Not implemented")
                console.error("Tool not implemented", tool)
            }
        }
        return this
    }

    fun clone() = RenderedLayers(
            gl,
            active = this.active,
            resolution = this.resolution,
            layers = this.layers.map { v -> v.clone() }.toTypedArray()
    )

    fun <T> compose(callback: (Texture) -> T): T {
        return if (layers.size == 1) {
            return callback(layers[0].canvas.texture)
        } else {
            gl.texture(
                    width = width,
                    height = height,
                    format = TextureFormat.RGBA,
                    type = TextureType.HALF_FLOAT,
                    filter = TextureFilter.NEAREST
            ) { out ->
                gl.settings()
                        .viewport(0, 0, width, height)
                        .blend(true)
                        .depthTest(false)
                        .clearColor(0.0, 0.0, 0.0, 0.0)
                        .blendFunction(BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA)
                        .blendEquation(BlendEquation.ADD)
                        .frameBuffer(out)
                        .apply {
                            gl.cleanColorBuffer()
                            layers.forEach { layer ->
                                layer.canvas.draw(
                                        Bounds(0.0, 0.0, 1.0, 1.0),
                                        Bounds(0.0, 0.0, 1.0, 1.0)
                                ) { color -> color }
                            }
                        }
                callback(out)
            }
        }
    }

    override fun dispose() {
        layers.forEach { it.dispose() }
    }
}

private data class Cancellation(var undoCounter: Int = 0, var rollbackFlag: Boolean = false) {
    fun undo() {
        undoCounter++
    }
    fun redo() {
        undoCounter -= 1
    }
    fun rollback() {
        rollbackFlag = true
    }
    fun reset() {
        undoCounter = 0
        rollbackFlag = false
    }

    val isEmpty
        get() = undoCounter <= 0 && !rollbackFlag

    inline fun drawAction(tail: () -> RenderedLayers, action: (RenderedLayers) -> RenderedLayers): RenderedLayers {
        return if (rollbackFlag) {
            rollbackFlag = false
            tail()
        } else {
            generalAction(tail, action)
        }
    }
    inline fun generalAction(tail: () -> RenderedLayers, action: (RenderedLayers) -> RenderedLayers): RenderedLayers {
        return if (undoCounter > 0) {
            undoCounter--
            tail()
        } else {
            action(tail())
        }
    }
}

private class RenderCache : Disposable {
    private var depth = 0

    private var lastCache: Pair<ListNode<StreamItem>, RenderedLayers>? = null
    private var fiveCache: Pair<ListNode<StreamItem>, RenderedLayers>? = null

    fun render(cancellation: Cancellation, key: ImmutableList<StreamItem>, render: () -> RenderedLayers): RenderedLayers {
        if (cancellation.isEmpty) {
            depth++
            try {
                val lastCache = this.lastCache
                val fiveCache = this.fiveCache
                return when {
                    key == null -> render()
                    lastCache?.first == key -> lastCache.second.clone()
                    fiveCache?.first == key -> fiveCache.second.clone()
                    depth == 1 -> {
                        val r = Pair(key, render())
                        this.lastCache?.let { (_, layers) -> layers.dispose() }
                        this.lastCache = r
                        r.second.clone()
                    }
                    depth == 5 -> {
                        val r = Pair(key, render())
                        this.fiveCache?.let { (_, layers) -> layers.dispose() }
                        this.fiveCache = r
                        r.second.clone()
                    }
                    else -> render()
                }
            } finally {
                depth--
            }
        } else {
            return render()
        }
    }

    override fun dispose() {
        this.lastCache?.second?.dispose()
        this.fiveCache?.second?.dispose()
        this.lastCache = null
        this.fiveCache = null
    }
}

class Renderer(val gl: Gl): Disposable {

    private val cache = RenderCache()

    fun render(stream: ImmutableList<StreamItem>) = render(stream, Cancellation())

    private fun render(stream: ImmutableList<StreamItem>, cancellation: Cancellation): RenderedLayers {
        if (stream == null) {
            return RenderedLayers(gl,null, Pair(0, 0), arrayOf())
        } else {
            val points = JsArray<Touch>()
            stream.eachNode { node ->
                val head: dynamic = node.head
                when (head.type) {
                    StreamItemType.INIT ->
                        return RenderedLayers(gl,null, Pair(head.size.first, head.size.second), arrayOf())
                    StreamItemType.UNDO -> cancellation.undo()
                    StreamItemType.REDO -> cancellation.redo()
                    StreamItemType.ROLLBACK -> cancellation.rollback()
                    StreamItemType.COMMIT -> points.clear()
                    StreamItemType.DRAW -> points += head.point.unsafeCast<Touch>()
                    StreamItemType.RESET_CANCELLATION -> cancellation.reset()
                    StreamItemType.BEGIN ->
                        return cancellation.drawAction({
                            cache.render(cancellation, node.tail) { render(node.tail, cancellation) }
                        }) {
                            it.draw(
                                    head.tool.unsafeCast<Tool>(),
                                    head.color.unsafeCast<Color>(),
                                    node.length,
                                    points.reversed()
                            )
                        }
                    StreamItemType.CREATE_LAYER ->
                        return cancellation.generalAction({
                            cache.render(cancellation, node.tail) { render(node.tail, cancellation) }
                        }) {
                            it.createLayer(head.id.unsafeCast<Id<Layer>>())
                        }
                    StreamItemType.SELECT_LAYER -> {
                        head as StreamItem.SelectLayer
                        return cancellation.generalAction({
                            cache.render(cancellation, node.tail) { render(node.tail, cancellation) }
                        }) {
                            it.selectLayer(head.id)
                        }
                    }
                    else -> throw Error("Unknown stream item '$head'")
                }
            }
            throw Error("Unexpected end of stream")
        }
    }

    override fun dispose() {
        cache.dispose()
    }
}