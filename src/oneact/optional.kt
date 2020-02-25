package oneact

import core.Disposable
import core.Param
import core.map

fun optional(condition: Param<Boolean>, child: () -> El) = conditional(condition, child, { none() })

fun delayedOptional(condition: Param<Boolean>, delay: Int, child: () -> El): El {
    return state(!condition()) { timeoutPassed, setTimeoutPassed ->
        var timeout: Disposable? = null
        val visible = map(condition, timeoutPassed) { c, p -> c || !p }
        val content = beforeDispose(optional(visible, child)) {
            timeout?.dispose()
            timeout = null
        }

        beforeUpdate(content) {
            val p = timeoutPassed()
            val c = condition()
            when {
                c && p -> setTimeoutPassed(false)
                c && !p -> {
                    timeout?.dispose()
                    timeout = null
                }
                !c && p -> Unit // Node was removed, it's OK
                !c && !p -> {
                    timeout = timeout ?: core.timeout(delay) {
                        timeout = null
                        setTimeoutPassed(true)
                    }
                }
            }
        }
    }
}