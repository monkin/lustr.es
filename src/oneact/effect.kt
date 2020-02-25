package oneact

import core.Disposable
import core.Param

private val noop = {}

enum class EffectStage {
    BeforeUpdate,
    AfterUpdate
}

fun <T> El.effect(
        param: Param<T>,
        stage: EffectStage,
        apply: El.() -> (() -> Unit)
): El {
    var dispose = noop
    var value: T? = null
    val effect = {
        val newValue = param()
        if (value === null || newValue !== value) {
            dispose()
            value = newValue
            dispose = this.apply()
        }
    }
    return when (stage) {
        EffectStage.BeforeUpdate -> beforeUpdate(effect)
        EffectStage.AfterUpdate -> afterUpdate(effect)
    }.beforeDispose { dispose() }
}

fun <T> El.effectBeforeUpdate(
        param: Param<T>,
        apply: El.() -> (() -> Unit)
) = effect(param, EffectStage.BeforeUpdate, apply)

fun <T> El.effectAfterUpdate(
        param: Param<T>,
        apply: El.() -> (() -> Unit)
) = effect(param, EffectStage.AfterUpdate, apply)



