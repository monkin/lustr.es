package material

import core.Param
import kotlinx.css.*
import kotlinx.css.properties.*
import oneact.*
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document

private val wrapperClass = cl("dialog-wrapper")
private val overlayClass = cl("dialog-overlay")
private val dialogShowKeyframes = cl("dialog-show")
private val dialogClass = cl("dialog")
private val titleClass = cl("dialog-title")
private val bodyClass = cl("dialog-body")
private val buttonsClass = cl("dialog-buttons")
private val openClass = cl("dialog-open")

private val withKeyframes = style("""
@keyframes $dialogShowKeyframes {
    from {
        opacity: 0;
        transform: translateY(-200px);
    }
    to {
        opacity: 1;
        transform: none;
    }
}""")

private val withStyles = style {
    ".$wrapperClass" {
        position = Position.fixed
        zIndex = 1000
        left = 0.px
        top = 0.px
        width = 100.pct
        height = 100.pct
        display = Display.flex
        justifyContent = JustifyContent.center
        alignItems = Align.center
    }
    ".$overlayClass" {
        position = Position.absolute
        left = 0.px
        top = 0.px
        width = 100.pct
        height = 100.pct
        zIndex = 1
    }
    ".$dialogClass" {
        opacity = 0
        backgroundColor = Color.white
        borderRadius = 2.px
        zIndex = 2
        transition = Transitions().also { transitions ->
            transitions += Transition("opacity", 150.ms, Timing.easeIn, 0.ms)
        }
        marginTop = (-10).vh
        position = Position.relative
        shadow(24.0)
    }
    ".$openClass" {
        opacity = 1
        animation(
                name = dialogShowKeyframes,
                duration = 150.ms,
                timing = Timing.easeOut
        )
    }
    ".$bodyClass" {
        padding(10.px, 24.px, 24.px, 24.px)
    }
    ".$buttonsClass" {
        padding(6.px)
        display = Display.flex
        justifyContent = JustifyContent.flexEnd
        "> *" {
            marginLeft = 8.px
        }
    }
    ".$titleClass" {
        fontSize = 21.px
        fontWeight = FontWeight.w500
        color = ThemeColor.PRIMARY_TEXT
        padding(24.px, 24.px, 10.px, 24.px)
        userSelect = UserSelect.none
        cursor = Cursor.default
    }
}

private val styled = { el: El -> withStyles(withKeyframes(el)) }

fun dialog(
        open: Param<Boolean>,
        title: () -> El,
        body: () -> El,
        buttons: () -> El,
        onOverlayClick: () -> Unit = {}
): El {
    val dialog = delayedOptional(open, 150) {
        el(
                className {
                    c(wrapperClass)
                },
                el(
                        className(overlayClass),
                        handler<MouseEvent>("click") {
                            onOverlayClick()
                        }
                ),
                el(
                        className {
                            c(dialogClass)
                            c(openClass, open)
                        },
                        title(),
                        body(),
                        buttons()
                )
        )
    }

    dialog.node.appendTo(document.body!!)

    return styled(beforeDispose(beforeUpdate(none()) {
        dialog.update()
    }) {
        dialog.remove()
    })
}

fun dialogTitle(content: String) = el(className(titleClass), text(content))
fun dialogBody(vararg content: El) = el(className(bodyClass), children(*content))
fun dialogButtons(vararg buttons: El) = el(className(buttonsClass), children(*buttons))