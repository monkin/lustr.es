package art

import core.Disposable
import core.context
import core.use
import draw.*

class ArtCanvas(val gl: Gl, val texture: Texture): Disposable {
    val width = texture.width
    val height = texture.height

    fun clean(color: Color = Color.LinearColor(0.0, 0.0, 0.0), opacity: Double = 0.0): ArtCanvas {
        val (r, g, b) = color.toLinear()
        gl.settings()
                .frameBuffer(texture)
                .viewport(0, 0, width, height)
                .clearColor(r, g, b, opacity)
                .apply {
                    gl.cleanColorBuffer()
                }
        return this
    }

    fun spray(
            path: TouchPath,
            size: Double,
            intensity: Double,
            granularity: Double,
            color: Color,
            opacity: Double,
            seed: Int = 0
    ): ArtCanvas {
        gl.settings()
                .frameBuffer(texture)
                .viewport(0, 0, width, height)
                .apply {
                    art.spray(
                            gl = gl,
                            path = path,
                            area = Bounds(0.0, 0.0, width.toDouble(), height.toDouble()),
                            color = color,
                            opacity = opacity,
                            size = size,
                            granularity = granularity,
                            intensity = intensity,
                            seed = seed
                    )
                }
        return this
    }

    fun brush(
            path: TouchPath,
            size: Double,
            density: Double,
            sharpness: Double,
            color: Color,
            opacity: Double,
            seed: Int = 0
    ): ArtCanvas {
        gl.settings()
                .frameBuffer(texture)
                .viewport(0, 0, width, height)
                .apply {
                    brush(
                            gl = gl,
                            path = path,
                            area = Bounds(0.0, 0.0, width.toDouble(), height.toDouble()),
                            resolution = Pair(width, height),
                            size = size,
                            density = density,
                            sharpness = sharpness,
                            color = color,
                            opacity = opacity,
                            seed = seed
                    )
                }
        return this
    }

    fun pencil(
            path: TouchPath,
            size: Double,
            hardness: Double,
            flow: Double,
            color: Color,
            opacity: Double
    ): ArtCanvas {
        gl.settings()
                .frameBuffer(texture)
                .viewport(0, 0, width, height)
                .apply {
                    pencil(
                            gl = gl,
                            resolution = Pair(width, height),
                            path = path,
                            size = size,
                            hardness = hardness,
                            flow = flow,
                            color = color,
                            opacity = opacity
                    )
                }
        return this
    }

    fun draw(canvas: Bounds, area: Bounds, colorTransform: Block.(color: Float4) -> Float4 = { color -> color }) {
        texture.draw(canvas, area, colorTransform)
    }

    fun clone(): ArtCanvas {
        val copy = gl.texture(
                width = width,
                height = height,
                format = TextureFormat.RGBA,
                filter = TextureFilter.NEAREST,
                type = TextureType.HALF_FLOAT
        )
        gl.settings()
                .frameBuffer(copy)
                .viewport(0, 0, width, height)
                .blend(false)
                .depthTest(false)
                .scissorTest(false)
                .apply {
                    val bounds = Bounds(0.0, 0.0, texture.width.toDouble(), texture.height.toDouble())
                    texture.draw(bounds, bounds) { color -> color }
                }
        return ArtCanvas(gl, copy)
    }

    fun <T> withCopy(callback: (canvas: ArtCanvas) -> T): T {
        return gl.texture(
                width = width,
                height = height,
                format = TextureFormat.RGBA,
                filter = TextureFilter.NEAREST,
                type = TextureType.HALF_FLOAT
        ) { copy ->
            gl.settings()
                    .frameBuffer(copy)
                    .viewport(0, 0, width, height)
                    .blend(false)
                    .depthTest(false)
                    .scissorTest(false)
                    .apply {
                        val bounds = Bounds(0.0, 0.0, width.toDouble(), height.toDouble())
                        texture.draw(bounds, bounds)
                    }
            callback(ArtCanvas(gl, copy))
        }
    }

    override fun dispose() = texture.dispose()
}

fun Gl.artCanvas(width: Int, height: Int): ArtCanvas {
    return ArtCanvas(this, texture(
            width = width,
            height = height,
            format = TextureFormat.RGBA,
            filter = TextureFilter.NEAREST,
            type = TextureType.HALF_FLOAT
    ))
}

fun <T> Gl.artCanvas(width: Int, height: Int, callback: (canvas: ArtCanvas) -> T): T {
    return texture(
            width = width,
            height = height,
            format = TextureFormat.RGBA,
            filter = TextureFilter.NEAREST,
            type = TextureType.HALF_FLOAT
    ) { texture ->
        callback(ArtCanvas(this, texture))
    }
}