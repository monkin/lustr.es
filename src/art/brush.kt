package art

import core.Random
import core.context
import draw.*
import kotlin.math.*

/**
 * Sunflower bristles distribution
 */
private fun sunflower(count: Int): Array<Vector> {
    val random = Random(0x3749BAD2)
    val scale = 2 / sqrt(count.toDouble())
    return (0..count).map { i ->
        val angle = 2.0 * PI / 2.618 * i // Square of the golden ratio
        val r = sqrt(i.toDouble() / count.toDouble())
        Vector(
                cos(angle) * r + (random.nextDouble() - 0.5) * scale,
                sin(angle) * r + (random.nextDouble() - 0.5) * scale
        )
    }.toTypedArray()
}

internal fun brush(
        gl: Gl,
        area: Bounds,
        resolution: Pair<Int, Int>,
        size: Double,
        density: Double,
        /**
         * 0 - square
         * 1 - pointed round
         */
        sharpness: Double,
        path: TouchPath,
        color: Color,
        opacity: Double,
        seed: Int
) {
    val scaledDensity = (density + 0.1) / 1.1
    val bristlesCount = (scaledDensity * size * 24).toInt()
    if (path.size > 1) {
        val optimizedPath = path
                .optimize(max(size / 10.0, 4.0))
                .smooth(PI / 32.0)
                .split(max(size / 32.0, 2.0))

        context {
            val random1 = Random(seed)
            val attributes = disposable(gl.attributes(optimizedPath.size,
                    "a_position" to 2,
                    "a_normal" to 2,
                    "a_pressure" to 1,
                    "a_offset" to 1
            ) { (appendPosition, appendNormal, appendPressure, appendOffset) ->
                val normals = optimizedPath.smoothNormals()
                var offset = 0.0 // position relative to the beginning of the curve
                optimizedPath.forEach { i, touch ->
                    val normal = normals[i]
                    if (i != 0) {
                        offset += optimizedPath[i - 1].distance(touch)
                    }
                    appendPosition(touch.x, touch.y)
                    appendNormal(normal.x, normal.y)
                    appendPressure(touch.force * (random1.nextDouble() + 10) / 10.5 )
                    appendOffset(offset)
                }
            })

            val random2 = Random(seed + 1)
            val indexes = disposable(gl.instances(bristlesCount,
                    "i_shift" to 2,
                    "i_length" to 1,
                    "i_wave_period" to 1,
                    "i_wave_offset" to 1
            ) { (appendShift, appendLength, appendWavePeriod, appendWaveOffset) ->
                sunflower(bristlesCount).forEach { v ->
                    appendShift(v.x, v.y)
                    appendLength(
                            max(v.length + sharpness - 1, 0.0).pow(2)
                    )
                    appendWavePeriod((random2.nextDouble() * 2 + 2) * 1 * PI)
                    appendWaveOffset(random2.nextDouble() * 2 * PI)
                }
            })

            gl.settings()
                    .depthTest(false)
                    .blend(true)
                    .blendEquation(BlendEquation.ADD)
                    .blendFunction(BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA)
                    .apply {
                        gl.draw(PrimitivesType.LINE_STRIP, attributes, indexes) {
                            val bristleShift = index2("i_shift")
                            val bristleLength = index1("i_length")
                            val iWavePeriod = index1("i_wave_period")
                            val iWaveOffset = index1("i_wave_offset")
                            val aOffset = attribute1("a_offset")
                            val position = attribute2("a_position")
                            val normal = attribute2("a_normal")
                            val pressure = attribute1("a_pressure")

                            val (areaX, areaY, areaW, areaH) = area.toLtwh()
                            val uArea = uniform("u_area", areaX, areaY, areaW, areaH)
                            val uResolution = uniform("u_resolution", resolution.first.toDouble(), resolution.second.toDouble())

                            val o = opacity.pow(scaledDensity * 1.5 + 1)

                            val (cr, cg, cb) = color.toLinear()
                            val uColor = uniform("u_color", cr * o, cg * o, cb * o, o)

                            val uSize = uniform("u_size", size)

                            val vOpacity = varying1("v_opacity")

                            vertex {
                                val noise = mem(sin(aOffset / iWavePeriod * (2 * PI) + iWaveOffset))
                                val shift = (
                                        normal * noise +
                                        bristleShift * uSize
                                ) / uResolution

                                val p = mem((position - float(uArea.x, uArea.y)) / float(uArea.z, uArea.w) * 2 - 1) + shift

                                vOpacity assign smoothstep(
                                        bristleLength * 0.25,
                                        bristleLength,
                                        pressure
                                ) * sqrt(abs(noise))

                                glPosition assign float(
                                        p.x, -p.y,
                                        float(0,1)
                                )
                            }

                            fragment {
                                glFragColor assign uColor * vOpacity
                            }
                        }
                    }
        }
    }
}