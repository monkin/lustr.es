package material

import core.Param
import core.map
import core.param
import kotlinx.css.*
import kotlinx.css.properties.*
import oneact.*

private val nullByDefault = param<String?>(null)

private val sectionsClass = cl("sections")
private val itemClass = cl("section-item")
private val visibleClass = cl("visible")
private val hiddenClass = cl("hidden")
private val showAnimation = cl("section-show")
private val hideAnimation = cl("section-hide")

private val keyframed = style("""
@keyframes $showAnimation {
    from {
        opacity: 0;
        transform: scaleY(0.66)
    }
    to {
        opacity: 1;
        transform: none;
    }
}
@keyframes $hideAnimation {
    from {
        opacity: 1;
        transform: none;
    }
    to {
        opacity: 0;
        transform: scaleY(0.66)
    }
}""")

private val styled = style {
    ".$sectionsClass" {
        display = Display.flex
        position = Position.relative
        height = 100.pct
    }
    ".$itemClass" {
        width = 100.pct
        height = 100.pct
        put("transformOrigin", "50% 0")
    }
    ".$hiddenClass" {
        position = Position.absolute
        left = 0.px
        top = 0.px
        zIndex = 0
        pointerEvents = PointerEvents.none
        opacity = 0
        animation(hideAnimation, 150.ms)
    }
    ".$visibleClass" {
        position = Position.static
        zIndex = 1
        transform = Transforms.none
        opacity = 1
        pointerEvents = PointerEvents.unset
        flexGrow = 1.0
        flexShrink = 1.0
        display = Display.flex
        flexDirection = FlexDirection.column
        animation(showAnimation, 150.ms)
    }
}

fun <T> sections(
        active: Param<T>,
        items: Map<T, () -> El>,
        className: Param<String?> = nullByDefault
) = keyframed(styled(el(
        attr("class", classes {
            c(sectionsClass)
            c(className)
        }),
        *items.entries.map { entry ->
            val isCurrentActive = map(active) { entry.key == it }
            delayedOptional(isCurrentActive, 150, {
                el(
                        attr("class", classes {
                            c(itemClass)
                            c(visibleClass, isCurrentActive)
                            c(hiddenClass, map(isCurrentActive) { !it })
                        }),
                        entry.value()
                )
            })
        }.toTypedArray()
)))