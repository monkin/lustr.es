package editor.state

import art.pencil
import state.Action
import state.selector

sealed class Tool {
    data class Spray(
            val size: Double = 50.0,
            val intensity: Double = 0.5,
            val granularity : Double = 1.0,
            val opacity: Double = 0.33
    ) : Tool()

    data class Pencil(
            val size: Double = 1.0,
            val hardness: Double = 1.0,
            val flow: Double = 0.5,
            val opacity: Double = 0.75
    ) : Tool()

    data class Eraser(
            val size: Double = 20.0,
            val hardness: Double = 0.5,
            val opacity: Double = 1.0
    ) : Tool()

    data class Brush(
            val size: Double = 30.0,
            val density: Double = 0.5,
            /**
             * 0 - square
             * 1 - pointed round
             */
            val sharpness: Double = 0.5,
            val opacity: Double = 0.4
    ) : Tool()

    object None : Tool()
}

sealed class SprayAction : Action<LustresState, Tool.Spray> {
    data class SetSize(val value: Double) : SprayAction() {
        override fun apply(state: Tool.Spray) = state.copy(size = value)
    }
    data class SetIntensity(val value: Double) : SprayAction() {
        override fun apply(state: Tool.Spray) = state.copy(intensity = value)
    }
    data class SetGranularity(val value: Double) : SprayAction() {
        override fun apply(state: Tool.Spray) = state.copy(granularity = value)
    }
    data class SetOpacity(val value: Double) : SprayAction() {
        override fun apply(state: Tool.Spray) = state.copy(opacity = value)
    }

    override fun read(state: LustresState) = state.spray
    override fun write(state: LustresState, local: Tool.Spray) = state.copy(spray = local)
}

sealed class PencilAction : Action<LustresState, Tool.Pencil> {
    data class SetSize(val value: Double) : PencilAction() {
        override fun apply(state: Tool.Pencil) = state.copy(size = value)
    }

    data class SetHardness(val value: Double) : PencilAction() {
        override fun apply(state: Tool.Pencil) = state.copy(hardness = value)
    }

    data class SetFlow(val value: Double) : PencilAction() {
        override fun apply(state: Tool.Pencil) = state.copy(flow = value)
    }

    data class SetOpacity(val value: Double) : PencilAction() {
        override fun apply(state: Tool.Pencil) = state.copy(opacity = value)
    }

    override fun read(state: LustresState) = state.pencil
    override fun write(state: LustresState, local: Tool.Pencil) = state.copy(pencil = local)
}


sealed class EraserAction : Action<LustresState, Tool.Eraser> {
    data class SetSize(val value: Double) : EraserAction() {
        override fun apply(state: Tool.Eraser) = state.copy(size = value)
    }
    data class SetHardness(val value: Double) : EraserAction() {
        override fun apply(state: Tool.Eraser) = state.copy(hardness = value)
    }
    data class SetOpacity(val value: Double) : EraserAction() {
        override fun apply(state: Tool.Eraser) = state.copy(opacity = value)
    }

    override fun read(state: LustresState) = state.eraser
    override fun write(state: LustresState, local: Tool.Eraser) = state.copy(eraser = local)
}

sealed class BrushAction : Action<LustresState, Tool.Brush> {
    data class SetSize(val size: Double) : BrushAction() {
        override fun apply(state: Tool.Brush) = state.copy(size = size)
    }

    data class SetDensity(val density: Double) : BrushAction() {
        override fun apply(state: Tool.Brush) = state.copy(density = density)
    }

    data class SetSharpness(val sharpness: Double) : BrushAction() {
        override fun apply(state: Tool.Brush) = state.copy(sharpness = sharpness)
    }

    data class SetOpacity(val opacity: Double) : BrushAction() {
        override fun apply(state: Tool.Brush) = state.copy(opacity = opacity)
    }

    override fun read(state: LustresState) = state.brush
    override fun write(state: LustresState, local: Tool.Brush) = state.copy(brush = local)
}

val selectSpray = selector { state: LustresState -> state.spray }
val selectSprayDispersion = selector(selectSpray) { it.size }
val selectSprayGranularity = selector(selectSpray) { it.granularity }
val selectSprayOpacity = selector(selectSpray) { it.opacity }
val selectSprayIntensity = selector(selectSpray) { it.intensity }

val selectPencil = selector { state: LustresState -> state.pencil }
val selectPencilSize = selector(selectPencil) { it.size }
val selectPencilHardness = selector(selectPencil) { it.hardness }
val selectPencilOpacity = selector(selectPencil) { it.opacity }
val selectPencilFlow = selector(selectPencil) { it.flow }

val selectEraser = selector { state: LustresState -> state.eraser }
val selectEraserSize = selector(selectEraser) { it.size }
val selectEraserHardness = selector(selectEraser) { it.hardness }
val selectEraserOpacity = selector(selectEraser) { it.opacity }

val selectBrush = selector { state: LustresState -> state.brush }
val selectBrushSize = selector(selectBrush) { it.size }
val selectBrushDensity = selector(selectBrush) { it.density }
val selectBrushOpacity = selector(selectBrush) { it.opacity }
val selectBrushSharpness = selector(selectBrush) { it.sharpness }

val selectTool = selector(selectActiveTool, selectSpray, selectEraser, selectBrush, selectPencil) { tool, spray, eraser, brush, pencil ->
    when (tool) {
        ActiveTool.SPRAY -> spray
        ActiveTool.ERASER -> eraser
        ActiveTool.BRUSH -> brush
        ActiveTool.PENCIL -> pencil
    }
}
