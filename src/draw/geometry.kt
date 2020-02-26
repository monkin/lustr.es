package draw

import core.JsArray
import core.Random
import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable
@Suppress("NOTHING_TO_INLINE")
data class Vector(val x: Double, val y: Double) {
    inline infix fun dot(vector: Vector) = x * vector.x + y * vector.y
    inline val length
        get() = sqrt(x * x + y * y)
    inline operator fun plus(vector: Vector) = Vector(x + vector.x, y + vector.y)
    inline operator fun minus(vector: Vector) = Vector(x - vector.x, y - vector.y)
    inline operator fun times(v: Double) = Vector(x * v, y * v)
    inline operator fun unaryMinus() = Vector(-x, -y)
    inline fun normal() = Vector(-y, x)
    inline fun normalize(): Vector {
        val l = length
        return Vector(x / l, y / l)
    }
    inline infix fun distance(v: Vector) = (this - v).length
}

fun Random.nextDirection(): Vector = (nextDouble() * 2 * PI).let { angle -> Vector(cos(angle), sin(angle)) }

@Serializable
@Suppress("NOTHING_TO_INLINE")
data class Touch(val point: Vector, val time: Double, val force: Double) {
    inline infix fun mix(t: Touch) = Touch(
            Vector((point.x + t.point.x) * 0.5, (point.y + t.point.y) * 0.5),
            (time + t.time) * 0.5,
            (force + t.force) * 0.5
    )
    inline fun mix(p: Touch, t: Double) = Touch(
            point * (1 - t) + p.point * t,
            time * (1 - t) + p.time * t,
            force * (1 - t) + p.force * t
    )

    inline operator fun plus(t: Touch) = Touch(point + t.point, time + t.time, force + t.force)
    inline operator fun minus(t: Touch) = Touch(point - t.point, time - t.time, force - t.force)
    inline operator fun times(v: Double) = Touch(point * v, time * v, force * v)

    inline fun normalize() = point.length.let { 1.0 / it }.let { l ->
        Touch(
                point * l,
                time * l,
                force * l
        )
    }

    val x
        get() = point.x
    val y
        get() = point.y
    infix fun distance(touch: Touch) = point distance touch.point
}

operator fun Matrix.times(touch: Touch) = touch.copy(point = this * touch.point)

typealias TouchPath = JsArray<Touch>

fun TouchPath.normals(): JsArray<Vector> = JsArray<Vector>().also { out ->
    for (i in 1..lastIndex) {
        out += (this[i].point - this[i - 1].point).normal().normalize()
    }
}

fun TouchPath.smoothNormals(): JsArray<Vector> = JsArray<Vector>().also { out ->
    val li = lastIndex
    val s = size
    for (i in 0..li) {
        val v = this[i]
        out += when (i) {
            0 -> if (s < 2) {
                Vector(0.0, 0.0)
            } else {
                (get(i + 1).point - v.point).normal()
            }
            li -> (v.point - get(i - 1).point).normal()
            else -> (get(i + 1).point - get(i - 1).point).normal()
        }.normalize()
    }
}

/**
 * Reduce segments count
 */
fun TouchPath.optimize(minSegmentLength: Double): TouchPath {
    val out = JsArray<Touch>()
    out += first()

    var i = 1
    while (i < lastIndex) {
        var j = i
        while (j < lastIndex && (get(i).point distance get(j).point) <= minSegmentLength) {
            j++
        }

        val time = (i until j).fold(0.0) { r, v ->
            val previous = get(v - 1).time
            val next = get(v + 1).time

            r + (next - previous)
        }

        var pPoint = Vector(0.0, 0.0)
        var pForce = 0.0
        var pTime = 0.0

        (i until j).forEach { v ->
            val previous = get(v - 1).time
            val next = get(v + 1).time
            val t = (next - previous) / time
            val p = get(v)

            pTime += p.time * t
            pForce += p.force * t
            pPoint += p.point * t
        }

        out += Touch(pPoint, pTime, pForce)

        i = j
    }

    out += last()
    return out.fold(JsArray<Touch>()) { r, v ->
        if (r.isEmpty() || r.last().point != v.point) {
            r += v
        }
        r
    }
}

private fun intersection(p1: Vector, p2: Vector, p3: Vector, p4: Vector): Vector?  {
    val d = (p1.x - p2.x) * (p4.y - p3.y) - (p1.y - p2.y) * (p4.x - p3.x)
    val da = (p1.x - p3.x) * (p4.y - p3.y) - (p1.y - p3.y) * (p4.x - p3.x)
    val db = (p1.x - p2.x) * (p1.y - p3.y) - (p1.y - p2.y) * (p1.x - p3.x)

    val ta = da / d
    val tb = db / d

    return if (ta.isFinite() && tb.isFinite() && ta >= -1 && ta <= 1 && tb >= -1 && tb <= 1) {
        Vector(
                p1.x + ta * (p2.x - p1.x),
                p1.y + ta * (p2.y - p1.y)
        )
    } else {
        null
    }
}

private fun smooth(level: Int, p1: Touch, p2: Touch, p3: Touch, p4: Touch, minAngleCos: Double, out: JsArray<Touch>) {
    if (level > 0 && (p2.point - p1.point).normalize() dot (p4.point - p3.point).normalize() < minAngleCos) {
        val p12 = p1 mix p2
        val p23 = p2 mix p3
        val p34 = p3 mix p4
        val p1223 = p12 mix p23
        val p2334 = p23 mix p34
        val p = p1223 mix p2334

        smooth(level - 1, p1, p12, p1223, p, minAngleCos, out)
        smooth(level - 1, p, p2334, p34, p4, minAngleCos, out)
    } else {
        out += p4
    }
}

fun TouchPath.smooth(maxAngle: Double): TouchPath {
    val minAngleCos = cos(maxAngle)
    val out = JsArray<Touch>()

    out += first()

    (2 until lastIndex).forEach { i ->
        val p1 = get(i - 2)
        val p2 = get(i - 1)
        val p3 = get(i)
        val p4 = get(i + 1)

        val v1 = (p1.point - p3.point).normalize()
        val v2 = (p2.point - p4.point).normalize()
        val angleCos = v1 dot v2

        if (angleCos < minAngleCos) {
            val d = p2 distance p3

            val s1 = p2 + (p2 - p1).normalize() * d * 0.2
            val s2 = p3 + (p3 - p4).normalize() * d * 0.2

            smooth(3, p2, s1, s2, p3, angleCos, out)
        } else {
            out += p3
        }
    }

    out += last()

    return out
}

private fun split(p1: Touch, p2: Touch, maxLength: Double, out: JsArray<Touch>) {
    if (p1.point distance  p2.point > maxLength) {
        val m = p1.mix(p2)
        split(p1, m, maxLength, out)
        split(m, p2, maxLength, out)
    } else {
        out += p2
    }
}

fun TouchPath.split(maxLength: Double): TouchPath = JsArray<Touch>().also { out ->
    out += first()
    (1..lastIndex).forEach { i ->
        split(get(i - 1), get(i), maxLength, out)
    }
}

val TouchPath.hasForceInfo
    get() = some { abs(it.force - 0.5) > 0.01 }

fun TouchPath.smoothForce(level: Int) = map { i, v ->
    val i1 = max(0, i - level)
    val i2 = min(lastIndex, i + level)
    var acc = 0.0
    for (j in i1..i2) {
        acc += get(j).force
    }
    v.copy(force = acc / (i2 - i1 + 1))
}

fun TouchPath.offsets(): JsArray<Double> = JsArray<Double>().also { out ->
    var l = 0.0
    forEach { i, v ->
        out += l
        if (i != 0) {
            l += v.point distance get(i - 1).point
        }
    }
}


data class Bounds(val x1: Double, val y1: Double, val x2: Double, val y2: Double) {
    val width
            get() = x2 - x1
    val height
            get() = y2 - y1
    val isEmpty: Boolean
            get() = width == 0.0 || height == 0.0
    constructor(width: Double, height: Double) : this(0.0, 0.0, width, height)
    fun toLtrb() = arrayOf(x1, y1, x2, y2)
    fun toLtwh() = arrayOf(x1, y1, width, height)
}

data class Matrix(val data: Array<Double>) {
    constructor() : this(arrayOf(
        1.0, 0.0, 0.0,
        0.0, 1.0, 0.0,
        0.0, 0.0, 1.0
    ))

    operator fun times(t: Matrix): Matrix {
        val sm = data
        val tm = t.data
        return Matrix(arrayOf(
                sm[0] * tm[0] + sm[1] * tm[3] + sm[2] * tm[6], sm[0] * tm[1] + sm[1] * tm[4] + sm[2] * tm[7], sm[0] * tm[2] + sm[1] * tm[5] + sm[2] * tm[8],
                sm[3] * tm[0] + sm[4] * tm[3] + sm[5] * tm[6], sm[3] * tm[1] + sm[4] * tm[4] + sm[5] * tm[7], sm[3] * tm[2] + sm[4] * tm[5] + sm[5] * tm[8],
                sm[6] * tm[0] + sm[7] * tm[3] + sm[8] * tm[6], sm[6] * tm[1] + sm[7] * tm[4] + sm[8] * tm[7], sm[6] * tm[2] + sm[7] * tm[5] + sm[8] * tm[8]
        ))
    }
    fun shift(dx: Double, dy: Double) = Matrix.shift(dx, dy) * this
    fun shift(v: Vector) = Matrix.shift(v) * this
    fun rotate(alpha: Double) = Matrix.rotate(alpha) * this
    fun scale(sx: Double, sy: Double = sx) = Matrix.scale(sx, sy) * this
    fun inverse(): Matrix {
        val a = data[0]
        val b = data[1]
        val c = data[2]
        val d = data[3]
        val e = data[4]
        val f = data[5]
        val g = data[6]
        val h = data[7]
        val i = data[8]
        val id = 1.0 / det()
        return Matrix(arrayOf(
                (e * i - f * h) * id, (c * h - b * i) * id, (b * f - c * e) * id,
                (f * g - d * i) * id, (a * i - c * g) * id, (c * d - a * f) * id,
                (d * h - e * g) * id, (b * g - a * h) * id, (a * e - b * d) * id
        ))
    }

    fun zoom(): Double {
        val zoomX = sqrt(data[0] * data[0] + data[1] * data[1])
        val zoomY = sqrt(data[3] * data[3] + data[4] * data[4])
        return sqrt(zoomX * zoomY)
    }

    fun det(): Double {
        val m = data
        return (m[0] * m[4] * m[8]
            + m[1] * m[5] * m[6]
            + m[2] * m[3] * m[7]
            - m[2] * m[4] * m[6]
            - m[1] * m[3] * m[8]
            - m[0] * m[5] * m[7])
    }

    operator fun times(vector: Vector): Vector {
        val (x, y) = vector
        val m = data
        return Vector(
                x * m[0] + y * m[1] + m[2],
                x * m[3] + y * m[4] + m[5]
        )
    }

    operator fun times(bounds: Bounds): Bounds {
        val (x1, y1) = this * Vector(bounds.x1, bounds.y1)
        val (x2, y2) = this * Vector(bounds.x2, bounds.y2)
        return Bounds(x1, y1, x2, y2)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as Matrix

        return data contentEquals other.data
    }

    override fun hashCode() = data.contentHashCode()

    // matrix(scaleX(),skewY(),skewX(),scaleY(),translateX(),translateY())
    fun toCss() = "matrix(${data[0]}, ${data[1]}, ${data[3]}, ${data[4]}, ${data[2]}, ${data[5]})"

    companion object {
        fun shift(dx: Double, dy: Double) = Matrix(arrayOf(
                1.0, 0.0, dx,
                0.0, 1.0, dy,
                0.0, 0.0, 1.0
        ))
        fun shift(v: Vector) = Matrix(arrayOf(
                1.0, 0.0, v.x,
                0.0, 1.0, v.y,
                0.0, 0.0, 1.0
        ))
        fun rotate(angle: Double): Matrix {
            val c = cos(angle)
            val s = sin(angle)
            return Matrix(arrayOf(
                c, s, 0.0,
                -s, c, 0.0,
                0.0, 0.0, 1.0
            ))
        }
        fun scale(sx: Double, sy: Double = sx): Matrix {
            return Matrix(arrayOf(
                sx, 0.0, 0.0,
                0.0, sy, 0.0,
                0.0, 0.0, 1.0
            ))
        }

    }
}