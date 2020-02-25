package art

import core.Random
import core.context
import draw.*
import kotlin.math.*

private fun TouchPath.integrate() = TouchPath().also { out ->
    for (i in 1..lastIndex) {
        val p1 = get(i - 1)
        val p2 = get(i)
        out += (p1 mix p2).copy(time = p1 distance p2)
    }
}

private fun draw(
        gl: Gl,
        area: Bounds,
        path: TouchPath,
        dispersion: Double,
        intensity: Double,
        granularity: Double,
        color: Color,
        opacity: Double,
        seed: Int
) {
    val dotSize = granularity * dispersion / 30.0
    val dotsPerPixel = intensity * dispersion / (dotSize * dotSize) * 75.0
    val maxDistance = min(8.0, max(1.0, dispersion * 0.1))
    val instancesCount = ceil(dotsPerPixel * maxDistance + 1.0).toInt()
    val points = path.optimize(2.0).split(maxDistance).integrate()
    context {
        if (points.isNotEmpty()) {
            val aRandom = Random(seed)
            val iRandom = Random(seed + 1)

            val attributes = disposable(gl.attributes(points.size,
                    "a_point" to 2,
                    "a_dispersion" to 1,
                    "a_count" to 1,
                    "a_seed1" to 1,
                    "a_seed2" to 1
            ) { (setPoint, setDispersion, setCount, setSeed1, setSeed2) ->
                for (point in points.toArray()) {
                    setPoint(point.x, point.y)
                    setDispersion(dispersion * (1.5 - point.force))
                    setCount(point.time * dotsPerPixel)
                    setSeed1((aRandom.nextInt() and 0x0FFFF).toDouble())
                    setSeed2((aRandom.nextInt() and 0x0FFFF).toDouble())
                }
            })

            val instances = disposable(gl.instances(instancesCount, "i_index" to 1, "i_seed" to 1) { (setIndex, setSeed) ->
                for (index in 0 until instancesCount) {
                    setIndex(index.toDouble())
                    setSeed((iRandom.nextInt() and 0x0FFFFF).toDouble())
                }
            })

            gl.draw(PrimitivesType.POINTS, attributes, instances) {
                val uGranularity = uniform("u_granularity", dotSize)
                val (areaX, areaY, areaW, areaH) = area.toLtwh()
                val uArea = uniform("u_area", areaX, areaY, areaW, areaH)
                val uColor = opacity.pow(intensity + 1).let { o ->
                    val (cr, cg, cb) = color.toLinear()
                    uniform("u_color", cr * o, cg * o, cb * o, o)
                }

                val aPoint = attribute2("a_point")
                val aDispersion = attribute1("a_dispersion")
                val aCount = attribute1("a_count")
                val aSeed1 = attribute1("a_seed1")
                val aSeed2 = attribute1("a_seed2")

                val iSeed = index1("i_seed")
                val iIndex = index1("i_index")

                val vEdge = varying1("v_edge")

                vertex {
                    cond(iIndex lt aCount, {
                        val direction =  randomDirection(iSeed, aSeed1)
                        val distance = sqrt(normalRandom(iSeed, aSeed2))
                        val g = mem(
                                min(
                                        uGranularity * 2,
                                        max(
                                                float(1),
                                                uGranularity * pow(float(E), normalRandom(mixRandom(iSeed, aSeed1), aSeed2) / 2)
                                        )
                                )
                        )
                        val shift = direction * distance * aDispersion * uGranularity / g
                        val p = mem((aPoint - float(uArea.x, uArea.y) + shift) / float(uArea.z, uArea.w) * 2 - 1)
                        vEdge assign float(1.0) - float(2.0) / g
                        glPointSize assign g
                        glPosition assign float(
                                p.x, -p.y,
                                float(0, 1)
                        )
                    }, {
                        glPointSize assign float(-1)
                        glPosition assign float4(0)
                    })
                }

                fragment {
                    glFragColor assign uColor * smoothstep(
                            float(1),
                            vEdge,
                            length(glPointCoord * 2 - 1)
                    )
                }
            }
        }
    }
}

internal fun spray(
        gl: Gl,
        area: Bounds,
        path: TouchPath,
        size: Double,
        intensity: Double,
        granularity: Double,
        color: Color,
        opacity: Double,
        seed: Int = 0
) {
    context {
        gl.settings()
                .depthTest(false)
                .blend(true)
                .blendEquation(BlendEquation.ADD)
                .blendFunction(BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA)
                .apply {
                    draw(
                            gl,
                            area,
                            path,
                            size / 2,
                            intensity,
                            granularity,
                            color,
                            opacity,
                            seed
                    )
                }
    }
}