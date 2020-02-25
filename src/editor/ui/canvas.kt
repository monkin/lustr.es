package editor.ui

import core.Param
import core.map
import core.param
import draw.Gl
import oneact.*
import org.khronos.webgl.WebGLContextAttributes
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import kotlin.browser.window
import kotlin.math.round

private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)

fun canvas(
        className: Param<String?> = nullByDefault,
        active: Param<Boolean> = trueByDefault,
        render: (context: Gl) -> (width: Int, height: Int) -> Unit
): El {
    val result = state(Pair(0.0, 0.0)) { size, setSize ->
        val ratio = param { window.devicePixelRatio }
        val realWidth = map(size, ratio) { (w, _), r -> round(w * r).toInt() }
        val realHeight = map(size, ratio) { (_, h), r -> round(h * r).toInt() }
        val canvas = el(
                "canvas",
                attr("width", map(realWidth) { w -> w.toString()  }),
                attr("height", map(realHeight) { h -> h.toString() } ),
                attr("style", inline(
                        "width" to map(size) { (width, _) -> "${width}px" },
                        "height" to map(size) { (_, height) -> "${height}px" }
                ))
        )
        val node = canvas.node.only.unsafeCast<HTMLCanvasElement>()
        val context = Gl(
                node,
                object : WebGLContextAttributes {
                    override var premultipliedAlpha: Boolean? = true
                    override var preserveDrawingBuffer: Boolean? = true
                    override var antialias: Boolean? = false
                    override var alpha: Boolean? = true
                    override var depth: Boolean? = true
                }
        )
        val repaint = render(context)

        resizeListener(
                afterUpdate(el(
                        "div",
                        attr("class", className),
                        attr("style", "overflow: hidden;"),
                        canvas
                )) {
                    if (active()) {
                        repaint(realWidth(), realHeight())
                    }
                }
        ) { width, height ->
            setSize(Pair(width, height))
        }
    }

    window.requestAnimationFrame { result.update() }

    return result
}