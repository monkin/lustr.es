package editor.state

import draw.Matrix
import draw.Vector
import state.Action

private const val zoomStep = 1.15

data class Pinch(
        val initial: Pair<Vector, Vector>,
        val current: Pair<Vector, Vector>
) {
    fun toMatrix(): Matrix {
        val c1 = (initial.first + initial.second) * 0.5
        val c2 = (current.first + current.second) * 0.5
        val v1 = initial.second - initial.first
        val v2 = current.second - current.first
        val zoom = v2.length / v1.length
        return Matrix
                .shift(-c1)
                .scale(zoom)
                .shift(c2)
    }
}

data class Orientation(
        val matrix: Matrix = Matrix(),
        val pinch: Pinch? = null
) {
    val transformation
        get() = pinch?.let {
            matrix * it.toMatrix()
        } ?: matrix
}


sealed class OrientationAction : Action<LustresState, Orientation> {

    data class BeginPinch(val touches: Pair<Vector, Vector>) : OrientationAction() {
        override fun apply(state: Orientation) = state.copy(pinch = Pinch(touches, touches))
    }

    data class ContinuePinch(val touches: Pair<Vector, Vector>) : OrientationAction() {
        override fun apply(state: Orientation) = state.pinch?.let { pinch ->
            state.copy(pinch = pinch.copy(current = touches))
        } ?: state
    }

    object CommitPinch : OrientationAction() {
        override fun apply(state: Orientation) = Orientation(matrix = state.transformation)
    }

    object RollbackPinch: OrientationAction() {
        override fun apply(state: Orientation) = Orientation(pinch = null)
    }

    data class BeginPenZoom(val point: Vector) : OrientationAction() {
        override fun apply(state: Orientation): Orientation {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    data class ChangePenZoom(val point: Vector) : OrientationAction() {
        override fun apply(state: Orientation): Orientation {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    object CommitPenZoom : OrientationAction() {
        override fun apply(state: Orientation): Orientation {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    object RollbackPenZoom: OrientationAction() {
        override fun apply(state: Orientation): Orientation {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    data class ZoomInAt(val point: Vector) : OrientationAction() {
        override fun apply(state: Orientation) = Orientation(
                matrix = state.matrix * Matrix.shift(-point).scale(zoomStep).shift(point)
        )
    }

    data class ZoomOutAt(val point: Vector) : OrientationAction() {
        override fun apply(state: Orientation) = Orientation(
                matrix = state.matrix * Matrix.shift(-point).scale(1.0 / zoomStep).shift(point)
        )
    }

    object ZoomIn : OrientationAction() {
        override fun apply(state: Orientation): Orientation {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    object ZoomOut : OrientationAction() {
        override fun apply(state: Orientation): Orientation {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    object Reset : OrientationAction() {
        override fun apply(state: Orientation) = Orientation()
    }

    override fun read(state: LustresState) = state.orientation
    override fun write(state: LustresState, local: Orientation) = state.copy(orientation = local)
}
