package draw

import kotlin.math.PI

private fun Block.rMul(v1: Float1, v2: Float1) = ((v1 * v2) % 1677721.6781)
private fun Block.rMul(v1: Float2, v2: Float2) = ((v1 * v2) % 1677721.6781)

fun Block.mixRandom(ic: Float2): Float1 {
    val c = mem(ic)
    val m1 = float(0xf1bb)
    val m2 = float(0x3503)
    val r = mem((rMul(rMul(float(m1, m2), c), c) + rMul(float(c.y, c.x), float(m1, m2))) % 85515.4521)
    return mem(rMul(r.x, r.y))
}

fun Block.mixRandom(v1: Float1, v2: Float1) = mixRandom(float(v1, v2))
fun Block.mixRandom(v1: Float1, v2: Float) = mixRandom(v1, float(v2))

fun Block.random(seed: Float1, index: Float1) = mem(mixRandom(seed, index) % 0xFFFF.toFloat() / 0xFFFF.toFloat())
fun Block.random(seed: Float1, index: Short) = random(seed, float(index))

fun Block.normalRandom(seed: Float1, i: Float1): Float1 {
    val n1 = random(seed, i)
    val n2 = random(seed, i + 1)

    return mem(sin(n2 * (PI * 2)) * sqrt(-2.0 * log(n1 + 0.001)))
}
fun Block.randomDirection(seed: Float1, i: Float1): ReadonlyFloat2 {
    val angle = mem(random(seed, i) * (PI * 2.0))
    return float(sin(angle), cos(angle))
}
fun Block.normalRandom2D(seed: Float1, i1: Float1 = float(0), i2: Float1 = float(1)): Float2 = mem(randomDirection(seed, i1) * normalRandom(seed, i2))

fun Block.premultiply(value: Float4): Float4 {
    val v = mem(value)
    return float(float(v.x, v.y, v.z) * v.w, v.w)
}

fun Block.demultiply(value: Float4): Float4 {
    val v = mem(value)
    return cond4(
            value.w gt float(0.001),
            { float(float(v.x, v.y, v.z) / v.w, v.w) },
            { float(0, 0, 0, 0) }
    )
}

fun Block.toSRgb(value: Float1): Float1 {
    val v = mem(value)
    return cond1(
            v lt float(0.0031308),
            { v * 12.92 },
            { 1.055 * pow(v, float(1.0 / 2.4)) - 0.055 }
    )
}

fun Block.toSRgb(value: Float3): Float3 {
    val v = mem(value)
    return float(
            toSRgb(v.x),
            toSRgb(v.y),
            toSRgb(v.z)
    )
}

fun Block.toSRgb(value: Float4): Float4 {
    val v = mem(value)
    return float(toSRgb(float(v.x, v.y, v.z)), v.w)
}

fun Block.toLinear(value: Float1): Float1 {
    val v = mem(value)
    return cond1(
            v lte float(0.04045),
            { v / 12.92 },
            { pow((v + 0.055) / 1.055, float(2.4)) }
    )
}

fun Block.toLinear(value: Float3): Float3 {
    val v = mem(value)
    return float(
            toLinear(v.x),
            toLinear(v.y),
            toLinear(v.z)
    )
}

fun Block.toLinear(value: Float4): Float4 {
    val v = mem(value)
    return float(toLinear(float(v.x, v.y, v.z)), v.w)
}

/**
 * Blend two premultiplied colors
 */
fun Block.blend(color: Float4, background: Float4): Float4 {
    val c = mem(color)
    return c + background * (1 - c.w)
}
