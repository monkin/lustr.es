package material

import core.Param
import core.map
import core.param
import core.toFixed
import draw.Color
import kotlinx.css.*
import kotlinx.css.properties.BoxShadow
import kotlinx.css.properties.BoxShadows
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.boxShadowInset
import oneact.*
import kotlin.browser.document

private fun rgb(r: Double, g: Double, b: Double) = Color.RgbColor(r / 255.0, g / 255.0, b / 255.0)

fun uiFont() = "Roboto, \"Segoe UI\", Tahoma, sans-serif"

typealias CSSColor = kotlinx.css.Color

object ThemeColor {
    /**
     * Tool bar background
     */
    val PAPER_DARK = CSSColor("var(--paper-dark-color)")
    /**
     * Default component background
     */
    val PAPER_NORMAL = CSSColor("var(--paper-normal-color)")
    /**
     * Label color
     */
    val LABEL = CSSColor("var(--label-color)")
    /**
     * Card or popup background
     */
    val PAPER_LIGHT = CSSColor("var(--paper-light-color)")
    /**
     * Text color
     */
    val PRIMARY_TEXT = CSSColor("var(--primary-text-color)")
    /**
     * Captions color
     */
    val SECONDARY_TEXT = CSSColor("var(--secondary-text-color)")
    /**
     * Disabled/hint text
     */
    val DISABLED_TEXT = CSSColor("var(--disabled-text-color)")
    /**
     * Border color
     */
    val BORDER = CSSColor("var(--border-color)")
    /**
     * Important elements color
     */
    val ACCENT = CSSColor("var(--accent-color)")
    /**
     * Danger color
     */
    val DANGER = CSSColor("var(--danger-color)")

    val TRANSPARENT = CSSColor("transparent")
}

enum class ThemeMode {
    DARK,
    LIGHT
}

@Suppress("NAME_SHADOWING")
private fun style(mode: Param<ThemeMode>, primary: Param<Color>, secondary: Param<Color>): El {
    return el("style",
        attr("type", "text/css"),
        text(map(mode, primary, secondary) { mode, primary, secondary ->
            val isLight = mode == ThemeMode.LIGHT
            val isDark = !isLight
            val (_, pa, pb) = primary.toLab()
            val (_, sa, sb) = secondary.toLab()
            val paperDark = Color.LabColor(if (isLight) 92.0 else 15.0, pa, pb).toString()
            val paperNormal = Color.LabColor(if (isLight) 96.0 else 20.0, pa, pb).toString()
            val paperLight = Color.LabColor(if (isLight) 100.0 else 27.0, pa, pb).toString()
            val labelColor = if (isDark) "rgba(0, 0, 0, .87)" else "#fff"
            val primaryText = if (isLight) "rgba(0, 0, 0, .87)" else "#fff"
            val secondaryText = if (isLight) "rgba(0, 0, 0, .54)" else "rgba(255, 255, 255, .70)"
            val disabledText = if (isLight) "rgba(0, 0, 0, .38)" else "rgba(255, 255, 255, .50)"
            val borderColor = if (isLight) "rgba(0, 0, 0, .12)" else "rgba(255, 255, 255, 0.12)"
            val accentColor = Color.LabColor(54.0, sa, sb).toString()
            val dangerColor = "#ff5252"
            """:root {
            --paper-dark-color: $paperDark;
            --paper-normal-color: $paperNormal;
            --paper-light-color: $paperLight;
            --primary-text-color: $primaryText;
            --secondary-text-color: $secondaryText;
            --disabled-text-color: $disabledText;
            --border-color: $borderColor;
            --accent-color: $accentColor;
            --label-color: $labelColor;
            --danger-color: $dangerColor;
            }
            body { font-family: ${uiFont()}; }"""
        })
    )
}

fun theme(
        mode: Param<ThemeMode> = param(ThemeMode.LIGHT),
        primary: Param<Color> = param(Color.RgbColor(1.0, 1.0, 1.0)),
        secondary: Param<Color> = param(
                Color.RgbColor(
                    0x2f.toDouble() / 255.0,
                    0xa4.toDouble() / 255.0,
                    0xe7.toDouble() / 255.0
                )
        )
): El {
    val s = style(mode, primary, secondary);
    s.node.appendTo(document.head!!)
    return beforeDispose(beforeUpdate(none()) { s.update() }) { s.remove() }
}


/**
 * Returns css 'box-shadow' string for the passed depth value'
 * @param depth Value in range [0..24]
 */
fun CSSBuilder.shadow(depth: Double) {

    if (depth < 0.5) {
        // It's necessary for shadow animations
        boxShadow(
                color = kotlinx.css.Color.transparent,
                offsetX = 0.px,
                offsetY = 1.px,
                blurRadius = 3.px
        )
        boxShadow(
                color = kotlinx.css.Color.transparent,
                offsetX = 0.px,
                offsetY = 1.px,
                blurRadius = 2.px
        )
    } else {
        val s = (depth - 1) / 23
        // first shadow parameters
        val o1 = ((0.298039 - 0.117647) * s + 0.117647).toFixed(3)
        val r11 = 18 * s + 1
        val r12 = 54 * s + 6
        // second shadow parameters
        val o2 = ((0.219608 - 0.117647) * s + 0.117647).toFixed(3)
        val r21 = 14 * s + 1
        val r22 = 16 * s + 4

        boxShadow(
                color = kotlinx.css.Color("rgba(0, 0, 0, $o1)"),
                offsetX = 0.px,
                offsetY = r11.px,
                blurRadius = r12.px,
                spreadRadius = 0.px
        )
        boxShadow(
                color = kotlinx.css.Color("rgba(0, 0, 0, $o2)"),
                offsetX = 0.px,
                offsetY = r21.px,
                blurRadius = r22.px,
                spreadRadius = 0.px
        )
    }
}

fun CSSBuilder.fillShadow(color: CSSColor) {
    boxShadowInset(
            color = color,
            spreadRadius = 10000.px
    )
}

fun fontLoader(): El {
    val link = el("link",
            attr("href", "https://fonts.googleapis.com/css?family=Roboto:300,300i,400,400i,500,500i,700,700i&amp;subset=cyrillic,greek,latin-ext,vietnamese"),
            attr("rel", "stylesheet")
    )
    link.node.appendTo(document.head!!)
    return beforeDispose(none()) { link.remove() }
}

fun CSSBuilder.borderRadius(leftTop: LinearDimension, rightTop: LinearDimension, rightBottom: LinearDimension, leftBottom: LinearDimension) {
    borderTopLeftRadius = leftTop
    borderTopRightRadius = rightTop
    borderBottomRightRadius = rightBottom
    borderBottomLeftRadius = leftBottom
}
