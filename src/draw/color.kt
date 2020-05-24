package draw

import core.hash
import core.toFixed
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private fun componentToLinear(c: Double) = if (c <= 0.04045) {
    c / 12.92
} else {
    ((c + 0.055) / 1.055).pow(2.4)
}

private fun componentToSRgb(c: Double): Double {
    return if (c <= 0.0031308) {
        c * 12.92
    } else {
        1.055 * c.pow(1.0 / 2.4) - 0.055
    }
}

@Serializable
sealed class Color {
    @Serializable
    @SerialName("Rgb")
    data class RgbColor(val r: Double, val g: Double, val b: Double) : Color() {
        override fun toArray() = arrayOf(r, g, b)
        override fun toRgb() = this
        override fun toLab() = this.toLinear().toLab()
        override fun toHsv(): HsvColor {
            val v = max(max(r, g), b)
            val diff = v - min(min(r, g), b)
            val diffc = { c: Double ->  (v - c) / 6.0 / diff + 0.5 }
            var h = 0.0
            var s = 0.0

            if (diff != 0.0) {
                s = diff / v
                val rr = diffc(r)
                val gg = diffc(g)
                val bb = diffc(b)

                h = if (r == v) {
                    bb - gg
                } else if (g == v) {
                    (1.0 / 3.0) + rr - bb;
                } else if (b == v) {
                    (2.0 / 3.0) + gg - rr;
                } else {
                    0.0
                }

                if (h < 0.0) {
                    h++
                } else if (h > 1.0) {
                    h--
                }
            }
            return HsvColor(h, s, v)
        }

        override fun toLinear(): LinearColor = LinearColor(
                componentToLinear(r),
                componentToLinear(g),
                componentToLinear(b)
        )
        override fun toString() = super.toString()
    }

    @Serializable
    @SerialName("Hsv")
    data class HsvColor(val h: Double, val s: Double, val v: Double) : Color() {
        override fun toArray() = arrayOf(h, s, v)

        override fun toRgb(): RgbColor {
            val i = kotlin.math.floor(h * 6.0).toInt()
            val f = h * 6 - i
            val p = v * (1 - s)
            val q = v * (1 - f * s)
            val t = v * (1 - (1 - f) * s)
            return when (i % 6) {
                0 -> RgbColor(v, t, p)
                1 -> RgbColor(q, v, p)
                2 -> RgbColor(p, v, t)
                3 -> RgbColor(p, q, v)
                4 -> RgbColor(t, p, v)
                5 -> RgbColor(v, p, q)
                else -> RgbColor(0.0, 0.0, 0.0)
            }
        }

        override fun toHsv() = this
        override fun toLinear() = toRgb().toLinear()
        override fun toLab() = this.toLinear().toLab()
        override fun toString() = super.toString()
    }

    @Serializable
    @SerialName("LRgb")
    data class LinearColor(val r: Double, val g: Double, val b: Double) : Color() {
        override fun toArray() = arrayOf(r, g, b)

        override fun toRgb() = RgbColor(
                componentToSRgb(r),
                componentToSRgb(g),
                componentToSRgb(b)
        )

        override fun toLab(): LabColor {
            val x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047
            val y = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000
            val z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883

            val mx = if (x > 0.008856) x.pow(1.0 / 3.0) else (7.787 * x + 16.0 / 116.0)
            val my = if (y > 0.008856) y.pow(1.0 / 3.0) else (7.787 * y + 16.0 / 116.0)
            val mz = if (z > 0.008856) z.pow(1.0 / 3.0) else (7.787 * z + 16.0 / 116.0)

            return LabColor(
                    (116.0 * my) - 16.0,
                    500.0 * (mx - my),
                    200.0 * (my - mz)
            )
        }
        override fun toHsv() = toRgb().toHsv()
        override fun toLinear(): LinearColor = this

        fun mix(c: LinearColor, v: Double): LinearColor {
            val n = 1.0 - v
            return LinearColor(
                    c.r * v + r * n,
                    c.g * v + g * n,
                    c.b * v + b * n
            )
        }
        override fun toString() = super.toString()
    }

    @Serializable
    @SerialName("Lab")
    data class LabColor(val l: Double, val a: Double, val b: Double) : Color() {
        override fun toRgb() = toLinear().toRgb()
        override fun toHsv() = toLinear().toHsv();
        override fun toLinear(): LinearColor {
            val y = (l + 16.0) / 116.0
            val x = a / 500.0 + y
            val z = y - b / 200.0

            val x3 = x * x * x
            val y3 = y * y * y
            val z3 = z * z * z

            val mx = 0.95047 * (if (x3 > 0.008856) x3 else (x - 16.0 / 116.0) / 7.787)
            val my = if (y3 > 0.008856) y3 else (y - 16.0 / 116.0) / 7.787
            val mz = 1.08883 * (if (z3 > 0.008856) z3 else (z - 16.0 / 116.0) / 7.787)

            return LinearColor(
                    mx *  3.2406 + my * -1.5372 + mz * -0.4986,
                    mx * -0.9689 + my *  1.8758 + mz *  0.0415,
                    mx *  0.0557 + my * -0.2040 + mz *  1.0570
            )
        }
        override fun toLab() = this
        override fun toArray() = arrayOf(l, a, b)

        fun mix(c: LabColor, v: Double): LabColor {
            val n = 1 - v
            return LabColor(
                    c.l * v + l * n,
                    c.a * v + a * n,
                    c.b * v + b * n
            )
        }
        override fun toString() = super.toString()
    }

    abstract fun toRgb(): RgbColor
    abstract fun toHsv(): HsvColor
    abstract fun toLinear(): LinearColor
    abstract fun toLab(): LabColor
    abstract fun toArray(): Array<Double>
    override fun toString() = toString(1.0)
    fun toString(opacity: Double): String {
        val color = toRgb()
        return if (opacity <= 0.0) {
            "transparent"
        } else {
            val r = (color.r * 255.0).toFixed(0)
            val g = (color.g * 255.0).toFixed(0)
            val b = (color.b * 255.0).toFixed(0)
            if (opacity >= 1.0) {
                "rgb($r, $g, $b)"
            } else {
                "rgba($r, $g, $b, ${opacity.toFixed(4)})"
            }
        }
    }

    override fun hashCode(): Int {
        val (r, g, b) = toRgb()
        return (r * 1000).toInt() hash  (g * 1000).toInt() hash (b * 1000).toInt()
    }

    override fun equals(other: Any?) = when {
        other === null -> false
        other is Color -> {
            val (r, g, b) = toRgb()
            val (or, og, ob) = other.toRgb()
            (r * 1000).toInt() == (or * 1000).toInt()
                    && (g * 1000).toInt() == (og * 1000).toInt()
                    && (b * 1000).toInt() == (ob * 1000).toInt()
        }
        else -> false
    }
}