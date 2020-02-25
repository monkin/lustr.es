package art

import core.JsArray
import core.context
import draw.*
import kotlin.math.*

private fun Block.pencilOpacity(radius: Float1, offset: Float1, hardness: Float1): Float1 {
    val l = mem(radius * hardness)
    val g = mem(radius + 1)
    val d = mem(1 - (max(l, min(g, offset)) - l) / (g - l))
    return cond1(
            d lt float(0.5),
            { 2 * d * d },
            { -1 + (4 - 2 * d) * d }
    )
}

private fun Block.pencilScale(pressure: Float1, flow: Float1) = ((pressure - 0.5) * flow + 0.5) * 2

private fun <R> prepareMask(
        gl: Gl,
        resolution: Pair<Int, Int>,
        path: TouchPath,
        size: Double,
        hardness: Double,
        flow: Double,
        callback: (Texture) -> R
): R {
    val (width, height) = resolution
    return context {
        val optimizedPath = path
                .optimize(min(max(size / 10.0, 4.0), 10.0))
                .smooth(PI / 128.0)
                .split(max(size / 10.0, 8.0))
                .smoothForce(2)
        val normals = optimizedPath.normals()
        val dots = JsArray<Pair<Vector, Double>>().also { out ->
            out += Pair(optimizedPath.first().point, optimizedPath.first().force)
            out += Pair(optimizedPath.last().point, optimizedPath.last().force)
            for (i in 1..normals.lastIndex) {
                val n1 = normals[i - 1]
                val n2 = normals[i]
                if (n1 dot n2 < 0.975) {
                    val touch = optimizedPath[i]
                    out += Pair(touch.point, touch.force)
                }
            }
        }

        gl.texture(
                width = width,
                height = height,
                format = TextureFormat.RGB,
                type = TextureType.UNSIGNED_BYTE,
                filter = TextureFilter.NEAREST
        ) { texture ->
            gl.renderBuffer(width, height) { renderBuffer ->
                gl.settings()
                        .frameBuffer(texture, renderBuffer)
                        .viewport(0, 0, width, height)
                        .depthTest(true)
                        .depthFunction(DepthFunction.GEQUAL)
                        .clearDepth(0.0)
                        .clearColor(0.0, 0.0, 0.0, 0.0)
                        .blend(false)
                        .apply {
                            gl.cleanBuffers()

                            val lineAttributes = disposable(gl.attributes(
                                    (optimizedPath.size - 1) * 4,
                                    "a_position" to 2,
                                    "a_normal" to 2,
                                    "a_pressure" to 1,
                                    "a_offset" to 1
                            ) { (setPosition, setNormal, setPressure, setOffset) ->
                                for (i in 0..normals.lastIndex) {
                                    val normal = normals[i]

                                    for (j in 0..1) {
                                        val touch = optimizedPath[i + j]
                                        setOffset(0.0)
                                        setPosition(touch.x, touch.y)
                                        setNormal(normal.x, normal.y)
                                        setPressure(touch.force)

                                        setOffset(1.0)
                                        setPosition(touch.x, touch.y)
                                        setNormal(normal.x, normal.y)
                                        setPressure(touch.force)
                                    }
                                }
                            })

                            val linesInstances = disposable(gl.instances(2, "i_side" to 1) { (append) ->
                                append(1.0)
                                append(-1.0)
                            })

                            gl.draw(PrimitivesType.TRIANGLE_STRIP, lineAttributes, linesInstances) {
                                val aPosition = attribute2("a_position")
                                val aNormal = attribute2("a_normal")
                                val aPressure = attribute1("a_pressure")
                                val aOffset = attribute1("a_offset")
                                val iSide = index1("i_side")
                                val uResolution = uniform("u_resolution", resolution.first.toDouble(), resolution.second.toDouble())
                                val uHardness = uniform("u_hardness", hardness)
                                val uFlow = uniform("u_flow", flow)
                                val uRadius = uniform("u_radius", size / 2)

                                val vSize = varying1("v_size")
                                val vOffset = varying1("v_offset")

                                vertex {
                                    val radius = mem(uRadius * pencilScale(aPressure, uFlow))
                                    val offset = mem((radius + 1) * aOffset)
                                    vSize assign radius
                                    vOffset assign offset
                                    glPosition assign float(
                                            (aPosition + offset * aNormal * iSide) / uResolution * float(2, -2) + float(-1, 1),
                                            1 - abs(offset / (radius + 1)),
                                            float(1)
                                    )
                                }

                                fragment {
                                    glFragColor assign float(
                                            pencilOpacity(vSize, vOffset, uHardness),
                                            float(0, 0, 1)
                                    )
                                }
                            }

                            val dotsAttributes = gl.attributes(
                                    48 + 2,
                                    "a_offset" to 1,
                                    "a_normal" to 2
                            ) { (setOffset, setNormal) ->
                                setOffset(0.0)
                                setNormal(0.0, 0.0)

                                for (i in 0..48) {
                                    setOffset(1.0)
                                    (i / 48.0 * 2 * PI).also { angle ->
                                        setNormal(cos(angle), sin(angle))
                                    }
                                }
                            }

                            val dotsInstances = gl.instances(
                                    dots.size,
                                    "i_position" to 2,
                                    "i_pressure" to 1
                            ) { (setPosition, setPressure) ->
                                dots.forEach { (position, pressure) ->
                                    setPosition(position.x, position.y)
                                    setPressure(pressure)
                                }
                            }

                            gl.draw(PrimitivesType.TRIANGLE_FAN, dotsAttributes, dotsInstances) {
                                val iPosition = index2("i_position")
                                val iPressure = index1("i_pressure")

                                val aNormal = attribute2("a_normal")
                                val aOffset = attribute1("a_offset")

                                val uResolution = uniform("u_resolution", resolution.first.toDouble(), resolution.second.toDouble())
                                val uHardness = uniform("u_hardness", hardness)
                                val uFlow = uniform("u_flow", flow)
                                val uRadius = uniform("u_radius", size / 2)

                                val vSize = varying1("v_size")
                                val vOffset = varying1("v_offset")

                                vertex {
                                    val radius = mem(uRadius * pencilScale(iPressure, uFlow))
                                    val offset = mem((radius + 1) * aOffset)
                                    vSize assign radius
                                    vOffset assign offset
                                    glPosition assign float(
                                            (iPosition + offset * aNormal) / uResolution * float(2, -2) + float(-1, 1),
                                            1 - abs(offset / (radius + 1)),
                                            float(1)
                                    )
                                }

                                fragment {
                                    glFragColor assign float(
                                            pencilOpacity(vSize, vOffset, uHardness),
                                            float(0, 0, 1)
                                    )
                                }
                            }

                        }
            }
            callback(texture)
        }
    }
}


internal fun pencil(
        gl: Gl,
        resolution: Pair<Int, Int>,
        path: TouchPath,
        size: Double,
        hardness: Double,
        flow: Double,
        color: Color,
        opacity: Double
) {
    if (path.size > 1) {
        context {
            prepareMask(gl, resolution, path, size, hardness, flow) { mask ->
                gl.settings()
                        .depthTest(false)
                        .blend(true)
                        .blendEquation(BlendEquation.ADD)
                        .blendFunction(BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA)
                        .texture(0, mask)
                        .apply {
                            val attributes = disposable(gl.attributes(4, "a_position" to 2) { (setPosition) ->
                                setPosition(0.0, 0.0)
                                setPosition(1.0, 0.0)
                                setPosition(0.0, 1.0)
                                setPosition(1.0, 1.0)
                            })

                            gl.draw(PrimitivesType.TRIANGLE_STRIP, attributes) {
                                val aPosition = attribute2("a_position")
                                val vPosition = varying2("v_position")
                                val uMask = texture("u_mask", 0)
                                val uColor = color.toLinear().let { (r, g, b) ->
                                    uniform("u_color", r * opacity, g * opacity, b * opacity, opacity)
                                }

                                vertex {
                                    vPosition assign aPosition
                                    glPosition assign float(aPosition * 2 - 1, float(0, 1))
                                }

                                fragment {
                                    glFragColor assign uColor * texture2D(uMask, vPosition).x
                                }
                            }
                        }
            }
        }
    }

}