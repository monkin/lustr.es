package oneact

import core.Param
import core.param

fun <T> cache(value: Param<T>, render: (value: Param<T>) -> El): El {
    return if (value is Param.Value) {
        render(value)
    } else {
        var cached = value()
        beforeUpdate(render(param { cached })) {
            cached = value()
        }
    }
}