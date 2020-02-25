@file:JsModule("fast-png")
package imports

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint8Array
import kotlin.js.*

external interface ImageData {
    val width: Int
    val height: Int
    /**
     * UInt8Array | UInt16Array
     */
    val data: ArrayBufferView
    /**
     * 8 | 16
     */
    val depth: Int
    /**
     * 1 | 2 | 3 | 4
     */
    val channels: Int
}

external fun encode(data: ImageData): Uint8Array