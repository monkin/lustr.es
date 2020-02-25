package material

import core.Param
import core.param
import core.timeout
import oneact.*

private val rippleAnimation = cl("ripple-animation")
private val rippleClass = cl("ripple")
private val styled = style("""
@keyframes $rippleAnimation {
    from {
        transform: scale(0);
        opacity: 1;
    }
    to {
        transform: scale(1.5);
        opacity: 0;
    }
}
.$rippleClass {
    position: absolute;
    transform: translate(-50%, -50%);
    width: 100%;
    padding-bottom: 100%;
    pointer-events: none;
}
.$rippleClass:after {
    content: "";
    display: block;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: ${ThemeColor.BORDER};
    opacity: 0;
    animation: $rippleAnimation .5s ease-out;
    will-change: transform, opacity;
}""")

private val halfByDefault = param("50%")
private val nullByDefault = param<String?>(null)

fun ripple(x: Param<String> = halfByDefault,
           y: Param<String> = halfByDefault,
           className: Param<String?> = nullByDefault,
           onRemoveRequest: () -> Unit
): El = styled(beforeDispose(el("div",
        attr("class", classes {
            c(rippleClass)
            c(className)
        }),
        attr("style", inline("left" to x, "top" to y))
), timeout(500, onRemoveRequest)))
