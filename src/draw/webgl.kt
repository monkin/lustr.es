package draw

import core.*
import org.khronos.webgl.*
import org.khronos.webgl.WebGLRenderingContext.Companion.ACTIVE_ATTRIBUTES
import org.khronos.webgl.WebGLRenderingContext.Companion.ACTIVE_UNIFORMS
import org.khronos.webgl.WebGLRenderingContext.Companion.ALIASED_POINT_SIZE_RANGE
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.ARRAY_BUFFER_BINDING
import org.khronos.webgl.WebGLRenderingContext.Companion.CLAMP_TO_EDGE
import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_ATTACHMENT0
import org.khronos.webgl.WebGLRenderingContext.Companion.COLOR_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.COMPILE_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.CURRENT_PROGRAM
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_ATTACHMENT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_BUFFER_BIT
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_COMPONENT16
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.ELEMENT_ARRAY_BUFFER_BINDING
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT_MAT2
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT_MAT3
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT_MAT4
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT_VEC2
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT_VEC3
import org.khronos.webgl.WebGLRenderingContext.Companion.FLOAT_VEC4
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAMEBUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAMEBUFFER_BINDING
import org.khronos.webgl.WebGLRenderingContext.Companion.FRAMEBUFFER_COMPLETE
import org.khronos.webgl.WebGLRenderingContext.Companion.FUNC_ADD
import org.khronos.webgl.WebGLRenderingContext.Companion.FUNC_REVERSE_SUBTRACT
import org.khronos.webgl.WebGLRenderingContext.Companion.FUNC_SUBTRACT
import org.khronos.webgl.WebGLRenderingContext.Companion.INT
import org.khronos.webgl.WebGLRenderingContext.Companion.INT_VEC2
import org.khronos.webgl.WebGLRenderingContext.Companion.INT_VEC3
import org.khronos.webgl.WebGLRenderingContext.Companion.INT_VEC4
import org.khronos.webgl.WebGLRenderingContext.Companion.LINK_STATUS
import org.khronos.webgl.WebGLRenderingContext.Companion.RENDERBUFFER
import org.khronos.webgl.WebGLRenderingContext.Companion.RENDERBUFFER_BINDING
import org.khronos.webgl.WebGLRenderingContext.Companion.RGBA
import org.khronos.webgl.WebGLRenderingContext.Companion.SAMPLER_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_BINDING_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MAG_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_MIN_FILTER
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_S
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_WRAP_T
import org.khronos.webgl.WebGLRenderingContext.Companion.UNPACK_FLIP_Y_WEBGL
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_SHORT
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

enum class BlendEquation(val value: Int) {
    ADD(FUNC_ADD),
    SUB(FUNC_SUBTRACT),
    RSUB(FUNC_REVERSE_SUBTRACT);
}

enum class DepthFunction(val value: Int) {
    NEVER(WebGLRenderingContext.NEVER),
    LESS(WebGLRenderingContext.LESS),
    EQUAL(WebGLRenderingContext.EQUAL),
    LEQUAL(WebGLRenderingContext.LEQUAL),
    GREATER(WebGLRenderingContext.GREATER),
    NOTEQUAL(WebGLRenderingContext.NOTEQUAL),
    GEQUAL(WebGLRenderingContext.GEQUAL),
    ALWAYS(WebGLRenderingContext.ALWAYS);
}

enum class BlendFunction(val value: Int) {
    ZERO(WebGLRenderingContext.ZERO),
    ONE(WebGLRenderingContext.ONE),
    SRC_COLOR(WebGLRenderingContext.SRC_COLOR),
    ONE_MINUS_SRC_COLOR(WebGLRenderingContext.ONE_MINUS_SRC_COLOR),
    DST_COLOR(WebGLRenderingContext.DST_COLOR),
    ONE_MINUS_DST_COLOR(WebGLRenderingContext.ONE_MINUS_DST_COLOR),
    SRC_ALPHA(WebGLRenderingContext.SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(WebGLRenderingContext.ONE_MINUS_SRC_ALPHA),
    DST_ALPHA(WebGLRenderingContext.DST_ALPHA),
    ONE_MINUS_DST_ALPHA(WebGLRenderingContext.ONE_MINUS_DST_ALPHA),
    SRC_ALPHA_SATURATE(WebGLRenderingContext.SRC_ALPHA_SATURATE);
}

enum class TextureFilter(val value: Int) {
    NEAREST(WebGLRenderingContext.NEAREST),
    LINEAR(WebGLRenderingContext.LINEAR);
}

enum class TextureType(val value: Int) {
    FLOAT(WebGLRenderingContext.FLOAT),
    UNSIGNED_BYTE(WebGLRenderingContext.UNSIGNED_BYTE),
    HALF_FLOAT(0x8D61);
}

enum class TextureFormat(val value: Int) {
    ALPHA(WebGLRenderingContext.ALPHA),
    LUMINANCE(WebGLRenderingContext.LUMINANCE),
    LUMINANCE_ALPHA(WebGLRenderingContext.LUMINANCE_ALPHA),
    RGB(WebGLRenderingContext.RGB),
    RGBA(WebGLRenderingContext.RGBA);
}

enum class ShaderType(val value: Int) {
    VERTEX(WebGLRenderingContext.VERTEX_SHADER),
    FRAGMENT(WebGLRenderingContext.FRAGMENT_SHADER);
}

const val TEXTURES_COUNT = 16

enum class ErrorCode(val value: Int) {
    NO_ERROR(WebGLRenderingContext.NO_ERROR),
    INVALID_ENUM(WebGLRenderingContext.INVALID_ENUM),
    INVALID_VALUE(WebGLRenderingContext.INVALID_VALUE),
    INVALID_OPERATION(WebGLRenderingContext.INVALID_OPERATION),
    OUT_OF_MEMORY(WebGLRenderingContext.OUT_OF_MEMORY);
    companion object {
        fun getValue(v: Int) = values().find { it.value == v } ?: throw IllegalArgumentException()
    }
}

enum class BufferUsage(val value: Int) {
    /**
     * The data store contents will be modified once and used at most a few times.
     */
    STREAM_DRAW(WebGLRenderingContext.STREAM_DRAW),
    /**
     * The data store contents will be modified once and used many times.
     */
    STATIC_DRAW(WebGLRenderingContext.STATIC_DRAW),
    /**
     * The data store contents will be modified repeatedly and used many times.
     */
    DYNAMIC_DRAW(WebGLRenderingContext.DYNAMIC_DRAW)
}

enum class BufferTarget(val value: Int) {
    ARRAY_BUFFER(WebGLRenderingContext.ARRAY_BUFFER),
    ELEMENT_ARRAY_BUFFER(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER)
}

enum class PrimitivesType(val value: Int) {
    POINTS(WebGLRenderingContext.POINTS),
    LINES(WebGLRenderingContext.LINES),
    LINE_STRIP(WebGLRenderingContext.LINE_STRIP),
    LINE_LOOP(WebGLRenderingContext.LINE_LOOP),
    TRIANGLES(WebGLRenderingContext.TRIANGLES),
    TRIANGLE_STRIP(WebGLRenderingContext.TRIANGLE_STRIP),
    TRIANGLE_FAN(WebGLRenderingContext.TRIANGLE_FAN)
}

enum class DataType(val value: Int) {
    FLOAT(WebGLRenderingContext.FLOAT) {
        override val size = 4
        override fun wrap(data: Array<Double>) = Float32Array(data.unsafeCast<Array<Float>>())
    },
    BYTE(WebGLRenderingContext.BYTE) {
        override val size = 1
        override fun wrap(data: Array<Double>) = Int8Array(data.unsafeCast<Array<Byte>>())
    },
    UNSIGNED_BYTE(WebGLRenderingContext.UNSIGNED_BYTE) {
        override val size = 1
        override fun wrap(data: Array<Double>) = Uint8Array(data.unsafeCast<Array<Byte>>())
    },
    SHORT(WebGLRenderingContext.SHORT) {
        override val size = 2
        override fun wrap(data: Array<Double>) = Int16Array(data.unsafeCast<Array<Short>>())
    },
    UNSIGNED_SHORT(WebGLRenderingContext.UNSIGNED_SHORT) {
        override val size = 2
        override fun wrap(data: Array<Double>) = Uint16Array(data.unsafeCast<Array<Short>>())
    },
    INT(WebGLRenderingContext.INT) {
        override val size = 4
        override fun wrap(data: Array<Double>) = Int32Array(data.unsafeCast<Array<Int>>())
    },
    UNSIGNED_INT(WebGLRenderingContext.UNSIGNED_INT) {
        override val size = 4
        override fun wrap(data: Array<Double>) = Uint32Array(data.unsafeCast<Array<Int>>())
    },
    SAMPLER_2D(WebGLRenderingContext.SAMPLER_2D) {
        override val size = 4
        override fun wrap(data: Array<Double>): ArrayBufferView {
            throw Error("Can't wrap sampler object")
        }
    };
    abstract fun wrap(data: Array<Double>): ArrayBufferView
    abstract val size: Int
    companion object {
        fun getArrayType(array: Any) = when (array::class.js) {
            js("Float32Array") -> DataType.FLOAT
            js("Int8Array") -> DataType.BYTE
            js("Uint8Array") -> DataType.UNSIGNED_BYTE
            js("Int16Array") -> DataType.SHORT
            js("Uint16Array") -> DataType.UNSIGNED_SHORT
            js("Int32Array") -> DataType.INT
            js("Uint32Array") -> DataType.UNSIGNED_INT
            else -> throw IllegalArgumentException()
        }
        fun getValue(v: Int) = values().find { it.value == v } ?: throw IllegalArgumentException()
    }
}

external interface InstancedArrays {
    val VERTEX_ATTRIB_ARRAY_DIVISOR_ANGLE: Int
    fun drawArraysInstancedANGLE(mode: Int, first: Int, count: Int, primcount: Int): Unit
    fun drawElementsInstancedANGLE(mode: Int, count: Int, type: Int, offset: Int, primcount: Int): Unit
    fun vertexAttribDivisorANGLE(index: Int, divisor: Int): Unit
}

class Gl(val handle: WebGLRenderingContext) : Disposable {
    constructor(source: HTMLCanvasElement, settings: WebGLContextAttributes) : this(source.getContext("webgl", settings).unsafeCast<WebGLRenderingContext>())
    constructor(source: HTMLCanvasElement) : this(source.getContext("webgl") as WebGLRenderingContext)

    val instancedArrays: InstancedArrays = handle.getExtension("ANGLE_instanced_arrays")!!.unsafeCast<InstancedArrays>()

    init {
        getExtension("OES_texture_half_float")
        getExtension("EXT_color_buffer_half_float")
    }

    fun isContextLost() = handle.isContextLost()
    fun getExtension(extension: String) = handle.getExtension(extension)
    fun getPointSizeRange(): Array<Double> {
        @Suppress("UnsafeCastFromDynamic")
        return handle.getParameter(ALIASED_POINT_SIZE_RANGE)!!.asDynamic()
    }
    fun cleanColorBuffer(): Gl {
        handle.clear(COLOR_BUFFER_BIT);
        return this
    }
    fun cleanDepthBuffer(): Gl {
        handle.clear(DEPTH_BUFFER_BIT);
        return this
    }
    /**
     * Clear color and depth buffer
     */
    fun cleanBuffers(): Gl {
        handle.clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)
        return this
    }

    fun drawArrays(primitivesType: PrimitivesType, verticesCount: Int): Gl {
        handle.drawArrays(primitivesType.value, 0, verticesCount)
        return this
    }
    fun drawsElements(primitivesType: PrimitivesType, elementsCount: Int): Gl {
        handle.drawElements(primitivesType.value, elementsCount, UNSIGNED_SHORT, 0)
        return this
    }
    fun drawInstancedArrays(primitivesType: PrimitivesType, verticesCount: Int, instancesCount: Int): Gl {
        instancedArrays.drawArraysInstancedANGLE(primitivesType.value, 0, verticesCount, instancesCount)
        return this
    }
    fun drawsInstancedElements(primitivesType: PrimitivesType, elementsCount: Int, instancesCount: Int): Gl {
        instancedArrays.drawElementsInstancedANGLE(primitivesType.value, elementsCount, UNSIGNED_SHORT, 0, instancesCount)
        return this
    }

    private val textureCache = Pool<TexturePoolKey, Texture>(8)
    fun <T> texture(
            width: Int,
            height: Int,
            format: TextureFormat = TextureFormat.RGBA,
            type: TextureType = TextureType.UNSIGNED_BYTE,
            filter: TextureFilter = TextureFilter.NEAREST,
            callback: (texture: Texture) -> T
    ) = textureCache.take(TexturePoolKey(width, height, type, format), {
        texture(
                width = width,
                height = height,
                format = format,
                type = type,
                filter = filter
        )
    }) { texture ->
        texture.size = Pair(width, height)
        texture.filter = filter
        callback(texture)
    }


    fun texture(config: TextureConfig) = Texture(this, config)
    fun texture(
            image: TexImageSource? = null,
            data: ArrayBufferView? = null,
            width: Int = image?.asDynamic()!!.width!!.unsafeCast<Int>() ,
            height: Int = image?.asDynamic()!!.height!!.unsafeCast<Int>(),
            format: TextureFormat = TextureFormat.RGBA,
            type: TextureType = TextureType.UNSIGNED_BYTE,
            filter: TextureFilter = TextureFilter.NEAREST
    ) = texture(TextureConfig(image, data, width, height, format, type, filter))

    private val renderBufferCache = Pool<Pair<Int, Int>, RenderBuffer>(2)
    fun renderBuffer(width: Int, height: Int) = RenderBuffer(this, width, height)
    fun <R> renderBuffer(width: Int, height: Int, callback: (buffer: RenderBuffer) -> R): R {
        return renderBufferCache.take(
                Pair(width, height),
                { renderBuffer(width, height) },
                callback
        )
    }

    fun frameBuffer(texture: Texture, depth: RenderBuffer? = null) = FrameBuffer(this, texture, depth)
    fun buffer(config: BufferConfig) = Buffer(this, config)
    fun buffer(
           usage: BufferUsage = BufferUsage.DYNAMIC_DRAW,
           target: BufferTarget = BufferTarget.ARRAY_BUFFER,
           data: BufferDataSource? = null,
           array: Array<Double>? = null,
           dataType: DataType = if (data != null) DataType.getArrayType(data) else DataType.FLOAT,
           elementsCount: Int = (data?.asDynamic() ?: array?.asDynamic()).length!!.unsafeCast<Int>()
    ) = buffer(BufferConfig(usage, target, data, array, dataType, elementsCount))

    private val shaderCache = DisposableCache<String, Shader>(128)
    fun <T> shader(type: ShaderType, source: String, callback: (shader: Shader) -> T) = shaderCache.take(
            source,
            { Shader(this, type, source) },
            callback
    )

    private val programs = Pool<String, Program>(64)
    fun <T> program(vertex: String, fragment: String, callback: (program: Program) -> T): T {
        return programs.take(
                vertex + fragment,
                {
                    shader(ShaderType.VERTEX, vertex) { v ->
                        shader(ShaderType.FRAGMENT, fragment) {
                            f -> Program(this, v, f)
                        }
                    }
                },
                { program ->
                    ProgramSetting(this, program.handle).apply {
                        callback(program)
                    }
                }
        )
    }

    fun settings() = object : GlSettings(this) {
        override fun <T> apply(callback: () -> T) = callback()
    }

    fun getError() = ErrorCode.getValue(handle.getError())

    private val storage = JsMap<Any?>()
    internal fun <T> cache(key: String, callback: () -> T): T {
        return if (storage.has(key)) {
            storage[key].unsafeCast<T>()
        } else {
            val result = callback()
            storage[key] = result
            result
        }
    }
    internal  fun <T> cache(key: String, value: T) {
        storage[key] = value
    }

    override fun dispose() {
        handle.getExtension("WEBGL_lose_context")?.loseContext()
    }

    fun attributes(
            size: Int,
            vararg items: Pair<String, Int>,
            callback: (writers: Array<AttributeWriter>) -> Unit
    ) = Attributes(this, AttributeType.ATTRIBUTES, size, *items) { callback(it) }

    fun instances(
            size: Int,
            vararg items: Pair<String, Int>,
            callback: (writers: Array<AttributeWriter>) -> Unit
    ) = Attributes(this, AttributeType.INSTANCES, size, *items) { callback(it) }
}

abstract class GlSettings(val gl: Gl) {
    abstract fun <T> apply(callback: () -> T): T

    private fun then(settings: GlSettings) = object : GlSettings(gl) {
        override fun <T> apply(callback: () -> T) = this@GlSettings.apply {
            settings.apply(callback)
        }
    }
    fun lineWidth(value: Double) = then(LineWidthSetting(gl, value))
    fun scissorTest(value: Boolean) = then(ScissorTestSetting(gl, value))
    fun blendEquation(rgb: BlendEquation, alpha: BlendEquation = rgb) = then(
            BlendEquationSetting(gl, BlendEquationConfig(rgb.value, alpha.value))
    )
    fun blendFunction(
            srcRgb: BlendFunction,
            dstRgb: BlendFunction,
            srcAlpha: BlendFunction = srcRgb,
            dstAlpha: BlendFunction = dstRgb
    ) = then(BlendFunctionSetting(gl, BlendFunctionValue(
            srcRgb.value, dstRgb.value, srcAlpha.value, dstAlpha.value
    )))
    fun blend(value: Boolean) = then(BlendSetting(gl, value))
    fun depthTest(value: Boolean) = then(DepthTestSetting(gl, value))
    fun depthFunction(value: DepthFunction) = then(DepthFunctionSetting(gl, value.value))
    fun clearColor(r: Double, g: Double, b: Double, alpha: Double) = then(ClearColorSetting(
            gl,
            ClearColor(r, g, b, alpha)
    ))
    fun clearDepth(value: Double) = then(ClearDepthSetting(gl, value))

    fun viewport(x: Int, y: Int, width: Int, height: Int) = then(ViewportSetting(
            gl,
            Area(x, y, width, height)
    ))
    fun scissorBox(x: Int, y: Int, width: Int, height: Int) = then(ScissorBoxSetting(
            gl,
            Area(x, y, width, height)
    ))
    fun activeTexture(i: Int) = then(ActiveTextureSetting(gl, i))
    fun texture(index: Int, texture: Texture) = then(TextureBindingSetting(
            gl,
            TextureBinding(index, texture.handle)
    ))
    fun renderBuffer(buffer: RenderBuffer) = then(RenderBufferSetting(gl, buffer.handle))
    fun frameBuffer(buffer: FrameBuffer) = then(FrameBufferSetting(gl, buffer.handle))
    fun frameBuffer(texture: Texture) = then(object : GlSettings(gl) {
        override fun <T> apply(callback: () -> T): T {
            return gl.frameBuffer(texture).use { buffer ->
                FrameBufferSetting(gl, buffer.handle).apply(callback)
            }
        }
    })
    fun frameBuffer(texture: Texture, renderBuffer: RenderBuffer) = then(object : GlSettings(gl) {
        override fun <T> apply(callback: () -> T): T {
            return gl.frameBuffer(texture, renderBuffer).use { buffer ->
                FrameBufferSetting(gl, buffer.handle).apply(callback)
            }
        }
    })
    fun buffer(buffer: Buffer) = then(BufferSetting(buffer))
    fun program(program: Program) = then(ProgramSetting(gl, program.handle))
}

private abstract class CachedSetting<V>(gl: Gl, val value: V) : GlSettings(gl) {
    val handle = gl.handle

    override fun <T> apply(callback: () -> T): T {
        val oldValue = getCached()
        try {
            setCached(value)
            return callback()
        } finally {
            setCached(oldValue)
        }
    }

    protected abstract fun get(): V
    protected abstract fun set(value: V)

    protected abstract fun getCacheKey(): String

    private fun getCached() = gl.cache(getCacheKey()) { get() }
    private fun setCached(value: V) {
        if (getCached() != value) {
            gl.cache(this.getCacheKey(), value)
            set(value)
        }
    }
}

private class LineWidthSetting(gl: Gl, value: Double) : CachedSetting<Double>(gl, value) {
    override fun get(): Double = handle.getParameter(WebGLRenderingContext.LINE_WIDTH).unsafeCast<Double>()
    override fun set(value: Double) = handle.lineWidth(value.toFloat())
    override fun getCacheKey() = "LineWidth"
}

private class ScissorTestSetting(gl: Gl, value: Boolean) : CachedSetting<Boolean>(gl, value) {
    override fun get(): Boolean = handle.getParameter(WebGLRenderingContext.SCISSOR_TEST).unsafeCast<Boolean>()
    override fun set(value: Boolean) = if (value) {
        handle.enable(WebGLRenderingContext.SCISSOR_TEST)
    } else {
        handle.disable(WebGLRenderingContext.SCISSOR_TEST)
    }
    override fun getCacheKey() = "ScissorTest"
}

private class BlendSetting(gl: Gl, value: Boolean) : CachedSetting<Boolean>(gl, value) {
    override fun get(): Boolean = handle.getParameter(WebGLRenderingContext.BLEND).unsafeCast<Boolean>()
    override fun set(value: Boolean) = if (value) {
        handle.enable(WebGLRenderingContext.BLEND)
    } else {
        handle.disable(WebGLRenderingContext.BLEND)
    }

    override fun getCacheKey() = "Blend"
}

private class DitherSetting(gl: Gl, value: Boolean) : CachedSetting<Boolean>(gl, value) {
    override fun get(): Boolean = handle.getParameter(WebGLRenderingContext.DITHER).unsafeCast<Boolean>()
    override fun set(value: Boolean) = if (value) {
        handle.enable(WebGLRenderingContext.DITHER)
    } else {
        handle.disable(WebGLRenderingContext.DITHER)
    }

    override fun getCacheKey() = "Dither"
}

private data class BlendEquationConfig(val rgb: Int, val alpha: Int)

private class BlendEquationSetting(gl: Gl, value: BlendEquationConfig) : CachedSetting<BlendEquationConfig>(gl, value) {
    override fun get() = BlendEquationConfig(
            handle.getParameter(WebGLRenderingContext.BLEND_EQUATION_RGB).unsafeCast<Int>(),
            handle.getParameter(WebGLRenderingContext.BLEND_EQUATION_ALPHA).unsafeCast<Int>()
    )
    override fun set(value: BlendEquationConfig) = handle.blendEquationSeparate(value.rgb, value.alpha)
    override fun getCacheKey() = "BlendEquation"
}

private data class BlendFunctionValue(val srcRgb: Int, val dstRgb: Int, val srcAlpha: Int, val dstAlpha: Int);

private class BlendFunctionSetting(gl: Gl, value: BlendFunctionValue) : CachedSetting<BlendFunctionValue>(gl, value) {
    override fun get() = BlendFunctionValue(
            handle.getParameter(WebGLRenderingContext.BLEND_SRC_RGB).unsafeCast<Int>(),
            handle.getParameter(WebGLRenderingContext.BLEND_DST_RGB).unsafeCast<Int>(),
            handle.getParameter(WebGLRenderingContext.BLEND_SRC_ALPHA).unsafeCast<Int>(),
            handle.getParameter(WebGLRenderingContext.BLEND_DST_ALPHA).unsafeCast<Int>()
    )
    override fun set(value: BlendFunctionValue) = handle.blendFuncSeparate(
            value.srcRgb,
            value.dstRgb,
            value.srcAlpha,
            value.dstAlpha
    )

    override fun getCacheKey() = "BlendFunction"
}

private class DepthTestSetting(gl: Gl, value: Boolean) : CachedSetting<Boolean>(gl, value) {
    override fun get(): Boolean = handle.getParameter(WebGLRenderingContext.DEPTH_TEST).unsafeCast<Boolean>()
    override fun set(value: Boolean) = if (value) {
        handle.enable(WebGLRenderingContext.DEPTH_TEST)
    } else {
        handle.disable(WebGLRenderingContext.DEPTH_TEST)
    }

    override fun getCacheKey() = "DepthTest"
}

private class DepthFunctionSetting(gl: Gl, value: Int) : CachedSetting<Int>(gl, value) {
    override fun get(): Int = handle.getParameter(WebGLRenderingContext.DEPTH_FUNC).unsafeCast<Int>()
    override fun set(value: Int) = handle.depthFunc(value)
    override fun getCacheKey() = "DepthFunction"
}

private data class ClearColor(val r: Double, val g: Double, val b: Double, val alpha: Double)

private class ClearColorSetting(gl: Gl, value: ClearColor) : CachedSetting<ClearColor>(gl, value) {
    override fun get(): ClearColor {
        val v = handle.getParameter(WebGLRenderingContext.COLOR_CLEAR_VALUE).unsafeCast<Array<Double>?>()
        return if (v !== null) {
            val (r, g, b, alpha) = v
            ClearColor(r, g, b, alpha)
        } else {
            ClearColor(0.0, 0.0, 0.0, 0.0)
        }
    }
    override fun set(value: ClearColor) = handle.clearColor(
            value.r.toFloat(),
            value.g.toFloat(),
            value.b.toFloat(),
            value.alpha.toFloat()
    )

    override fun getCacheKey() = "ClearColor"
}

private class ClearDepthSetting(gl: Gl, value: Double) : CachedSetting<Double>(gl, value) {
    override fun get(): Double = handle.getParameter(WebGLRenderingContext.DEPTH_CLEAR_VALUE).unsafeCast<Double>()
    override fun set(value: Double) = handle.clearDepth(value.toFloat())
    override fun getCacheKey() = "ClearDepth"
}

private data class Area(val x: Int, val y: Int, val width: Int, val height: Int)

private class ViewportSetting(gl: Gl, value: Area) : CachedSetting<Area>(gl, value) {
    override fun get(): Area {
        val (x, y, width, height) = handle.getParameter(WebGLRenderingContext.VIEWPORT).unsafeCast<Array<Int>>()
        return Area(x, y, width, height)
    }
    override fun set(value: Area) = handle.viewport(
            value.x,
            value.y,
            value.width,
            value.height
    )

    override fun getCacheKey() = "Viewport"
}

private class ScissorBoxSetting(gl: Gl, value: Area) : CachedSetting<Area>(gl, value) {
    override fun get(): Area {
        val (x, y, width, height) = handle.getParameter(WebGLRenderingContext.SCISSOR_BOX).unsafeCast<Array<Int>>()
        return Area(x, y, width, height)
    }
    override fun set(value: Area) = handle.scissor(
            value.x,
            value.y,
            value.width,
            value.height
    )

    override fun getCacheKey() = "ScissorBox"
}

private class ActiveTextureSetting(gl: Gl, value: Int) : CachedSetting<Int>(gl, value) {
    override fun get() = handle.getParameter(WebGLRenderingContext.ACTIVE_TEXTURE).unsafeCast<Int>() - WebGLRenderingContext.TEXTURE0
    override fun set(value: Int) = handle.activeTexture(value + WebGLRenderingContext.TEXTURE0)
    override fun getCacheKey() = "ActiveTexture"
}

private class RenderBufferSetting(gl: Gl, value: WebGLRenderbuffer?) : CachedSetting<WebGLRenderbuffer?>(gl, value) {
    override fun get() = handle.getParameter(RENDERBUFFER_BINDING).unsafeCast<WebGLRenderbuffer?>()
    override fun set(value: WebGLRenderbuffer?) =  handle.bindRenderbuffer(RENDERBUFFER, value)
    override fun getCacheKey() = "RenderBuffer"
}

private class FrameBufferSetting(gl: Gl, value: WebGLFramebuffer?) : CachedSetting<WebGLFramebuffer?>(gl, value) {
    override fun get() = handle.getParameter(FRAMEBUFFER_BINDING).unsafeCast<WebGLFramebuffer?>()
    override fun set(value: WebGLFramebuffer?) =  handle.bindFramebuffer(FRAMEBUFFER, value)
    override fun getCacheKey() = "FrameBuffer"
}

private class ArrayBufferSetting(gl: Gl, value: WebGLBuffer?) : CachedSetting<WebGLBuffer?>(gl, value) {
    override fun get() = handle.getParameter(ARRAY_BUFFER_BINDING).unsafeCast<WebGLBuffer?>()
    override fun set(value: WebGLBuffer?) =  handle.bindBuffer(ARRAY_BUFFER, value)
    override fun getCacheKey() = "ArrayBuffer"
}

private class ElementsArrayBufferSetting(gl: Gl, value: WebGLBuffer?) : CachedSetting<WebGLBuffer?>(gl, value) {
    override fun get() = handle.getParameter(ELEMENT_ARRAY_BUFFER_BINDING).unsafeCast<WebGLBuffer?>()
    override fun set(value: WebGLBuffer?) =  handle.bindBuffer(ELEMENT_ARRAY_BUFFER, value)
    override fun getCacheKey() = "ElementsArrayBuffer"
}

private class BufferSetting(val value: Buffer) : GlSettings(value.gl) {
    override fun <T> apply(callback: () -> T): T = when (value.target) {
        BufferTarget.ARRAY_BUFFER -> ArrayBufferSetting(gl, value.handle).apply(callback)
        BufferTarget.ELEMENT_ARRAY_BUFFER -> ElementsArrayBufferSetting(gl, value.handle).apply(callback)
    }
}

private class ProgramSetting(gl: Gl, value: WebGLProgram?) : CachedSetting<WebGLProgram?>(gl, value) {
    override fun get() = handle.getParameter(CURRENT_PROGRAM).unsafeCast<WebGLProgram?>()
    override fun set(value: WebGLProgram?) = handle.useProgram(value)
    override fun getCacheKey() = "Program"
}

private data class TextureBinding(val index: Int, val texture: WebGLTexture?)

private class TextureBindingSetting(gl: Gl, value: TextureBinding) : CachedSetting<TextureBinding>(gl, value) {
    override fun get(): TextureBinding {
        return ActiveTextureSetting(gl, value.index).apply {
            TextureBinding(
                    value.index,
                    handle.getParameter(TEXTURE_BINDING_2D).unsafeCast<WebGLTexture?>()
            )
        }
    }
    override fun set(value: TextureBinding) {
        return ActiveTextureSetting(gl, value.index).apply {
            handle.bindTexture(TEXTURE_2D, value.texture)
        }
    }

    override fun getCacheKey() = "texture_${value.index}"
}

private data class TexturePoolKey(val size: Int, val type: TextureType, val format: TextureFormat) {
    constructor(width: Int, height: Int, type: TextureType, format: TextureFormat) : this(
            (2.0).pow(ceil(log2((width * height).toDouble()))).toInt(),
            type,
            format
    )
}

data class TextureConfig(
        val image: TexImageSource? = null,
        val data: ArrayBufferView? = null,
        val width: Int = image?.asDynamic()!!.width!!.unsafeCast<Int>(),
        val height: Int = image?.asDynamic()!!.height!!.unsafeCast<Int>(),
        val format: TextureFormat = TextureFormat.RGBA,
        val type: TextureType = TextureType.UNSIGNED_BYTE,
        val filter: TextureFilter = TextureFilter.NEAREST
)

class Texture(val gl: Gl, config: TextureConfig) : Disposable {
    val handle = gl.handle.createTexture()
    var config = config.copy(image = null)

    init {
        TextureBindingSetting(gl, TextureBinding (0, handle)).apply() {
            gl.handle.pixelStorei(UNPACK_FLIP_Y_WEBGL, 1)
            gl.handle.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, config.filter.value)
            gl.handle.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, config.filter.value)
            gl.handle.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
            gl.handle.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
            val format = config.format.value
            val type = config.type.value
            if (config.image !== null) {
                gl.handle.texImage2D(
                        TEXTURE_2D,
                        0,
                        format,
                        format,
                        type,
                        config.image
                )
            } else {
                gl.handle.texImage2D(
                        TEXTURE_2D,
                        0,
                        format,
                        config.width,
                        config.height,
                        0,
                        format,
                        type,
                        config.data
                )
            }
        }
    }
    val width
        get() = config.width
    val height
        get() = config.height
    val format
        get() = config.format
    val type
        get() = config.type
    var filter
        get() = config.filter
        set(filter: TextureFilter) {
            if (this.filter != filter) {
                gl.handle.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, filter.value)
                gl.handle.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, filter.value)
            }
        }

    var size
        get() = Pair(width, height)
        set(value: Pair<Int, Int>) {
            val (width, height) = value;
            if (this.width != width || this.height != height) {
                this.config = this.config.copy(width = width, height = height, data = null)
                gl.settings().activeTexture(0).texture(0, this).apply {
                    gl.handle.texImage2D(
                            TEXTURE_2D,
                            0,
                            config.format.value,
                            width,
                            height,
                            0,
                            config.format.value,
                            config.type.value,
                            null
                    )
                }
            }
        }

    fun readBytes(): Uint8Array {
        val r = Uint8Array(this.width * this.height * 4)
        gl.handle.readPixels(0, 0, this.width, this.height, RGBA, UNSIGNED_BYTE, r)
        return r
    }

    fun readFloats(): Float32Array {
        val r = Float32Array(this.width * this.height * 4)
        gl.handle.readPixels(0, 0, this.width, this.height, RGBA, FLOAT, r)
        return r
    }

    fun draw(canvas: Bounds, area: Bounds, colorTransform: Block.(color: Float4) -> Float4 = { it }) {
        val (x1, y1, x2, y2) = (
                Matrix.shift(-canvas.x1, -canvas.y1)
                        .scale(2.0 / canvas.width, 2.0 / canvas.height)
                        .shift(-1.0, -1.0) * area
                ).toLtrb()
        gl.attributes(4, "a_point" to 2, "a_texturePoint" to 2) { (setPoint, setTexturePoint) ->
            setPoint(x1, y1)
            setTexturePoint(0.0, 0.0)
            setPoint(x2, y1)
            setTexturePoint(1.0, 0.0)
            setPoint(x1, y2)
            setTexturePoint(0.0, 1.0)
            setPoint(x2, y2)
            setTexturePoint(1.0, 1.0)
        }.use { attributes ->
            gl.settings().texture(0, this).apply {
                gl.draw(PrimitivesType.TRIANGLE_STRIP, attributes) {
                    val aPoint = attribute2("a_point")
                    val aTexturePoint = attribute2("a_texturePoint")
                    val vTexturePoint = varying2("v_texturePoint")
                    val sampler = texture("u_texture", 0)

                    vertex {
                        vTexturePoint assign float(aTexturePoint.x, aTexturePoint.y)
                        glPosition assign float(aPoint, float(0, 1))
                    }
                    fragment {
                        glFragColor assign colorTransform(texture2D(sampler, vTexturePoint))
                    }
                }
            }
        }
    }

    fun half(
            type: TextureType = this.type,
            format: TextureFormat = this.format,
            filter: TextureFilter = this.filter,
            reduce: Block.(p1: Float4, p2: Float4, p3: Float4, p4: Float4) -> Float4 = { p1, p2, p3, p4 ->
                (p1 + p2 + p3 + p4) * 0.25
            }
    ): Texture = context {
        val target = constructing(gl.texture(
                width = width / 2,
                height = height / 2,
                filter = filter,
                format = format,
                type = type
        ))
        half(target, reduce)
        target
    }

    fun <T> half(
            type: TextureType = this.type,
            format: TextureFormat = this.format,
            filter: TextureFilter = this.filter,
            reduce: Block.(p1: Float4, p2: Float4, p3: Float4, p4: Float4) -> Float4 = { p1, p2, p3, p4 ->
                (p1 + p2 + p3 + p4) * 0.25
            },
            callback: (texture: Texture) -> T
    ) = gl.texture(
            width = width / 2,
            height = height / 2,
            type = type,
            format = format,
            filter = filter
    ) { texture ->
        half(texture, reduce)
        callback(texture)
    }

    private fun half(
            target: Texture,
            reduce: Block.(p1: Float4, p2: Float4, p3: Float4, p4: Float4) -> Float4
    ) {
        gl.attributes(4, "a_point" to 2) { (setPoint) ->
            setPoint(-1.0, -1.0)
            setPoint(1.0, -1.0)
            setPoint(-1.0, 1.0)
            setPoint(1.0, 1.0)
        }.use { attributes ->
            gl.settings()
                    .viewport(0, 0, target.width, target.height)
                    .frameBuffer(target)
                    .texture(0, this)
                    .apply {
                        gl.draw(PrimitivesType.TRIANGLE_STRIP, attributes) {
                            val aPoint = attribute2("a_point")
                            val uSource = texture("u_source", 0)
                            val uResolution = uniform(
                                    "u_resolution",
                                    target.width.toDouble(),
                                    target.height.toDouble()
                            )
                            vertex {
                                val (x, y) = aPoint
                                glPosition assign float(x, y, float(0), float(1))
                            }
                            fragment {
                                val (x, y) = glFragCoord
                                val p = float(x, y)
                                glFragColor assign reduce(
                                        texture2D(uSource, (p + float2(0.25)) / uResolution),
                                        texture2D(uSource, (p + float(0.75, 0.25)) / uResolution),
                                        texture2D(uSource, (p + float(0.75, 0.25)) / uResolution),
                                        texture2D(uSource, (p + float(0.75, 0.75)) / uResolution)
                                )
                            }
                        }
                    }
        }
    }

    override fun dispose() {
        gl.handle.deleteTexture(handle)
    }
}

class FrameBuffer(val gl: Gl, val colorBuffer: Texture, depthBuffer: RenderBuffer? = null) : Disposable {
    val handle = gl.handle.createFramebuffer()

    init {
        FrameBufferSetting(gl, this.handle).apply() {
            gl.handle.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, colorBuffer.handle, 0);
            if (depthBuffer != null) {
                gl.handle.framebufferRenderbuffer(FRAMEBUFFER, DEPTH_ATTACHMENT, RENDERBUFFER, depthBuffer.handle);
            }
        }
    }

    val isComplete: Boolean
        get() = FrameBufferSetting(gl, handle).apply() {
            gl.handle.checkFramebufferStatus(FRAMEBUFFER) == FRAMEBUFFER_COMPLETE
        }

    override fun dispose() {
        this.gl.handle.deleteFramebuffer(this.handle);
    }
}

/**
 * Depth buffer
 */
class RenderBuffer(val gl: Gl, private var widthValue: Int, private var heightValue: Int) : Disposable {
    val handle = gl.handle.createRenderbuffer().unsafeCast<WebGLRenderbuffer>()

    init {
        RenderBufferSetting(gl, handle).apply() {
            gl.handle.renderbufferStorage(RENDERBUFFER, DEPTH_COMPONENT16, width, height)
        }
    }

    val width
        get() = widthValue
    val height
        get() = heightValue

    fun resize(width: Int, height: Int): RenderBuffer {
        if (width != widthValue || height != heightValue) {
            widthValue = width
            heightValue = height
            RenderBufferSetting(gl, handle).apply() {
                gl.handle.renderbufferStorage(RENDERBUFFER, DEPTH_COMPONENT16, width, height)
            }
        }
        return this
    }

    override fun dispose() {
        gl.handle.deleteRenderbuffer(handle)
    }
}

data class BufferConfig(
        val usage: BufferUsage = BufferUsage.DYNAMIC_DRAW,
        val target: BufferTarget = BufferTarget.ARRAY_BUFFER,
        val data: BufferDataSource? = null,
        val array: Array<Double>? = null,
        val dataType: DataType = if (data != null) DataType.getArrayType(data) else DataType.FLOAT,
        val elementsCount: Int = (data?.asDynamic() ?: array?.asDynamic()).length!!.unsafeCast<Int>()
)

class Buffer(val gl: Gl, config: BufferConfig) : Disposable {
    private var config = config.copy(
            data = null,
            array = null
    )
    val handle = gl.handle.createBuffer()
    init {
        BufferSetting(this).apply {
            when {
                config.data != null -> gl.handle.bufferData(
                        config.target.value,
                        config.data,
                        config.usage.value
                )
                config.array != null -> gl.handle.bufferData(
                        config.target.value,
                        config.dataType.wrap(config.array),
                        config.usage.value
                )
                else -> gl.handle.bufferData(
                        config.target.value,
                        config.elementsCount * config.dataType.size,
                        config.usage.value
                )
            }
        }
    }

    val target
        get() = config.target
    val elementsCount
        get() = config.elementsCount
    val usage
        get() = config.usage
    val dataType
        get() = config.dataType

    fun resize(elementsCount: Int) {
        BufferSetting(this).apply() {
            gl.handle.bufferData(target.value, elementsCount, usage.value)
            config = config.copy(elementsCount = elementsCount)
        }
    }
    fun write(data: Array<Double>) {
        BufferSetting(this).apply() {
            gl.handle.bufferData(target.value, dataType.wrap(data), usage.value)
            config = config.copy(elementsCount = data.size)
        }
    }
    fun write(data: BufferDataSource) {
        BufferSetting(this).apply() {
            gl.handle.bufferData(target.value, data, usage.value)
            config = config.copy(elementsCount = data.asDynamic().length)
        }
    }

    override fun dispose() {
        gl.handle.deleteBuffer(handle)
    }
}

class Shader(val gl: Gl, val type: ShaderType, val source: String) : Disposable {
    val handle = gl.handle.createShader(type.value)
    init {
        gl.handle.shaderSource(handle, source)
        gl.handle.compileShader(handle)
        if (gl.handle.getShaderParameter(handle, COMPILE_STATUS) == false) {
            throw Error("WebGL error '${gl.handle.getShaderInfoLog(handle)}' in '$source'")
        }
    }

    override fun dispose() {
        gl.handle.deleteShader(handle)
    }
}

data class UniformRecord(val location: WebGLUniformLocation, val type: Int)
data class AttributeRecord(val location: Int, val type: Int, val size: Int, val dimentions: Int)

class Program(val gl: Gl, val vertex: Shader, val fragment: Shader) : Disposable {
    val handle = gl.handle.createProgram()
    init {
        gl.handle.attachShader(handle, vertex.handle)
        gl.handle.attachShader(handle, fragment.handle)
        gl.handle.linkProgram(handle)
        if (gl.handle.getProgramParameter(handle, LINK_STATUS) == false) {
            throw Error(gl.handle.getProgramInfoLog(handle) ?: "Program linking error:\n${vertex.source};\n${fragment.source}")
        }
    }

    private val uniforms: JsMap<UniformRecord>
    init {
        val uniformsCount = gl.handle.getProgramParameter(handle, ACTIVE_UNIFORMS).unsafeCast<Int>()
        val collection = JsMap<UniformRecord>()
        for (i in 0 until uniformsCount) {
            val info = gl.handle.getActiveUniform(handle, i)
            if (info != null) {
                if (info.name.startsWith("u")) {
                    collection[info.name] = UniformRecord(
                            gl.handle.getUniformLocation(handle, info.name)!!,
                            info.type
                    )
                } else {
                    throw Error("Uniform '${info.name}' must starts with 'u'")
                }
            }
        }
        uniforms = collection
    }

    private val attributes: JsMap<AttributeRecord>
    init {
        val attributesCount = gl.handle.getProgramParameter(handle, ACTIVE_ATTRIBUTES).unsafeCast<Int>()
        val attributesCollection = JsMap<AttributeRecord>()
        ProgramSetting(gl, handle).apply {
            for (i in 0 until attributesCount) {
                val info = gl.handle.getActiveAttrib(handle, i)
                if (info != null) {
                    attributesCollection[info.name] = AttributeRecord(
                            gl.handle.getAttribLocation(handle, info.name),
                            info.type,
                            info.size,
                            when (info.type) {
                                FLOAT -> 1
                                FLOAT_VEC2 -> 2
                                FLOAT_VEC3 -> 3
                                FLOAT_VEC4 -> 4
                                else -> throw Error("Attribute ${info.name} has incompatible type")
                            }
                    )
                }
            }
        }
        attributes = attributesCollection
    }

    fun setUniform(name: String, value: Double): Program {
        val info = this.uniforms[name]
        if (info != null) {
            if (info.type == FLOAT) {
                ProgramSetting(gl, handle).apply {
                    gl.handle.uniform1f(info.location, value.toFloat())
                }
            } else {
                console.error("Invalid argument type, '${info.type}' expected")
            }
        } else {
            console.warn("Uniform '$name' not found")
        }
        return this
    }

    fun setUniform(name: String, value: Int): Program {
        val info = this.uniforms[name]
        if (info != null) {
            if (info.type == INT || info.type == SAMPLER_2D) {
                ProgramSetting(gl, handle).apply() {
                    gl.handle.uniform1i(info.location, value)
                }
            } else {
                console.error("Invalid argument '$name' type")
            }
        } else {
            console.warn("Uniform '$name' not found")
        }
        return this
    }

    fun setUniform(name: String, value: Array<Int>): Program {
        val info = this.uniforms[name]
        if (info !== null) {
            ProgramSetting(this.gl, handle).apply() {
                val location = info.location
                when (info.type) {
                    INT_VEC2 -> gl.handle.uniform2iv(location, value)
                    INT_VEC3 -> gl.handle.uniform3iv(location, value)
                    INT_VEC4 -> gl.handle.uniform4iv(location, value)
                    else -> throw Error("Uniform '$name' has incompatible type")
                }
            }
        } else {
            console.warn("Uniform '$name' not found")
        }
        return this
    }

    fun setUniform(name: String, value: Array<Double>): Program {
        val info = this.uniforms[name]
        if (info != null) {
            ProgramSetting(this.gl, handle).apply() {
                val location = info.location
                when (info.type) {
                    FLOAT_VEC2 -> gl.handle.uniform2fv(location, value.unsafeCast<Array<Float>>())
                    FLOAT_VEC3 -> gl.handle.uniform3fv(location, value.unsafeCast<Array<Float>>())
                    FLOAT_VEC4 -> gl.handle.uniform4fv(location, value.unsafeCast<Array<Float>>())
                    FLOAT_MAT2 -> gl.handle.uniformMatrix2fv(location, false, value.unsafeCast<Array<Float>>())
                    FLOAT_MAT3 -> gl.handle.uniformMatrix3fv(location, false, value.unsafeCast<Array<Float>>())
                    FLOAT_MAT4 -> gl.handle.uniformMatrix4fv(location, false, value.unsafeCast<Array<Float>>())
                    else -> throw Error("Uniform '$name' has incompatible type")
                }
            }
        } else {
            console.warn("Uniform '$name' not found")
        }
        return this
    }

    fun setUniform(name: String, value: Vector) = setUniform(name, arrayOf(value.x, value.y))
    fun setUniform(name: String, matrix: Matrix) = setUniform(name, matrix.data)

    fun <T> enableAttribute(name: String, callback: () -> T): T {
        val attr = attributes[name]
        return if (attr !== null) {
            gl.handle.enableVertexAttribArray(attr.location)
            try {
                callback()
            } finally {
                gl.handle.disableVertexAttribArray(attr.location)
            }
        } else {
            console.warn("Attribute '$name' not found")
            callback()
        }
    }
    fun <T> enableAttributes(names: Array<String>, callback: () -> T): T {
        fun enable(i: Int): T {
            return if (i == names.size) {
                callback()
            } else {
                enableAttribute(names[i]) {
                    enable(i + 1)
                }
            }
        }
        return enable(0)
    }

    fun <T> enableInstancing(name: String, callback: () -> T): T {
        val attr = attributes[name]
        return if (attr !== null) {
            gl.instancedArrays.vertexAttribDivisorANGLE(attr.location, 1)
            try {
                callback()
            } finally {
                gl.instancedArrays.vertexAttribDivisorANGLE(attr.location, 0)
            }
        } else {
            console.warn("Attribute '$name' not found")
            callback()
        }
    }

    fun <T> enableInstancing(names: Array<String>, callback: () -> T): T {
        fun enable(i: Int): T {
            return if (i == names.size) {
                callback()
            } else {
                enableInstancing(names[i]) {
                    enable(i + 1)
                }
            }
        }
        return enable(0)
    }

    fun setAttribute(name: String, buffer: Buffer, stride: Int, offset: Int) {
        val attr = attributes[name]
        if (attr != null) {
            ProgramSetting(gl, handle).apply {
                BufferSetting(buffer).apply {
                    gl.handle.vertexAttribPointer(
                            attr.location,
                            attr.dimentions,
                            FLOAT,
                            false,
                            stride * 4,
                            offset * 4
                    )
                }
            }
        } else {
            console.warn("Attribute '$name' not found")
        }
    }

    override fun dispose() {
        gl.handle.deleteProgram(handle)
    }
}

@Suppress("NOTHING_TO_INLINE")
class AttributeWriter(val buffer: Float32Array, val stride: Int, offset: Int) {
    var position = offset
    inline operator fun invoke(value: Double) {
        buffer[position] = value.toFloat()
        position += stride
    }
    inline operator fun invoke(v1: Double, v2: Double) {
        buffer[position] = v1.toFloat()
        buffer[position + 1] = v2.toFloat()
        position += stride
    }
    inline operator fun invoke(v1: Double, v2: Double, v3: Double) {
        buffer[position] = v1.toFloat()
        buffer[position + 1] = v2.toFloat()
        buffer[position + 2] = v3.toFloat()
        position += stride
    }
    inline operator fun invoke(v1: Double, v2: Double, v3: Double, v4: Double) {
        buffer[position] = v1.toFloat()
        buffer[position + 1] = v2.toFloat()
        buffer[position + 2] = v3.toFloat()
        buffer[position + 3] = v4.toFloat()
        position += stride
    }
}

enum class AttributeType {
    ATTRIBUTES,
    INSTANCES
}

class Attributes(val gl: Gl, val type: AttributeType, val size: Int, vararg val items: Pair<String, Int>, callback: (writers: Array<AttributeWriter>) -> Unit): Disposable {
    private val itemsArray = JsArray(items)
    private val names = itemsArray.map { v -> v.first }.toArray()
    private val stride = itemsArray.map { v -> v.second }.fold(0) { r, v -> r + v }
    private val offsets = itemsArray.fold(arrayListOf(0)) { r, v ->
        r.add(v.second + (r.lastOrNull() ?: 0))
        r
    }.slice(items.indices)
    private val buffer: Buffer

    init {
        val data = Float32Array(stride * size)
        val writers = JsArray<AttributeWriter>()
        items.indices.forEach { i ->
            writers += AttributeWriter(data, stride, offsets[i])
        }
        callback(writers.toArray())
        buffer = gl.buffer(data = data)
    }

    fun <T> apply(program: Program, callback: () -> T): T {
        fun apply() {
            for (i in 0 until items.size) {
                program.setAttribute(names[i], buffer, stride, offsets[i])
            }
        }

        return ProgramSetting(gl, program.handle).apply {
            program.enableAttributes(names) {
                if (type == AttributeType.INSTANCES) {
                    program.enableInstancing(names) {
                        apply()
                        callback()
                    }
                } else {
                    apply()
                    callback()
                }
            }
        }
    }
    fun contains(name: String) = names.find { it == name } != null
    fun size(name: String) = items.find { it.first == name }?.second ?: throw Error("Attribute '$name' not found")
    override fun dispose() = buffer.dispose()
}