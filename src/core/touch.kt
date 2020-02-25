package core

import draw.Vector
import org.w3c.dom.UnionElementOrMouseEvent
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.UIEvent

// Touch event

open external class Touch {
    val identifier: Double
    val screenX: Double
    val screenY: Double
    val clientX: Double
    val clientY: Double
    val pageX: Double
    val pageY: Double
    val target: EventTarget
    val force: Double?
}

external interface TouchList {
    val length: Double
    fun item(i: Int): Touch
}

open external class TouchEvent(type: String) : UIEvent, UnionElementOrMouseEvent {
    open val altKey: Boolean
    open val ctrlKey: Boolean
    open val metaKey: Boolean
    open val shiftKey: Boolean
    open val changedTouches: TouchList
    open val targetTouchesRead: TouchList
    open val touches: TouchList
}

// Pointer event

open external class PointerEvent(type: String) : MouseEvent {
    /**
     * A unique identifier for the pointer causing the event.
     */
    open val pointerId: Double
    /**
     * The width (magnitude on the X axis), in CSS pixels, of the contact geometry of the pointer.
     */
    open val width: Double
    /**
     * The height (magnitude on the Y axis), in CSS pixels, of the contact geometry of the pointer.
     */
    open val height: Double
    /**
     * The normalized pressure of the pointer input in the range 0 to 1, where 0 and 1 represent the minimum and maximum pressure the hardware is capable of detecting, respectively.
     */
    open val pressure: Double
    /**
     * The normalized tangential pressure of the pointer input (also known as barrel pressure or cylinder stress) in the range -1 to 1, where 0 is the neutral position of the control.
     */
    open val tangentialPressure: Double
    /**
     * The plane angle (in degrees, in the range of -90 to 90) between the Y-Z plane and the plane containing both the transducer (e.g. pen stylus) axis and the Y axis.
     */
    open val tiltX: Double
    /**
     * The plane angle (in degrees, in the range of -90 to 90) between the X-Z plane and the plane containing both the transducer (e.g. pen stylus) axis and the X axis.
     */
    open val tiltY: Double
    /**
     * The clockwise rotation of the transducer (e.g. pen stylus) around its major axis in degrees, with a value in the range 0 to 359.
     */
    open val twist: Double
    /**
     * Indicates the device type that caused the event (mouse, pen, touch, etc.)
     */
    open val pointerType: String
    /**
     * Indicates if the pointer represents the primary pointer of this pointer type.
     */
    open val isPrimary: Boolean
}

val PointerEvent.isMultiTouch
    get() = this.pointerType != "mouse" && this.pointerType != "pen"
val MouseEvent.clientPoint
    get() = Vector(clientX.toDouble(), clientY.toDouble())