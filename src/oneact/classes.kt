package oneact

import core.Param
import core.param
import core.map


interface ClassesBuilder {
    fun c(name: Param<String?>): Unit
    fun c(name: String?) = c(param(name))
    fun c(name: String?, condition: Param<Boolean>) {
        c(map(condition) { if (it) name else null })
    }
    fun c(name: String?, condition: () -> Boolean) {
        c(name, param(condition))
    }
}

fun classes(callback: ClassesBuilder.() -> Unit): Param<String?> {
    val strings = ArrayList<Param<String?>>()
    callback(object : ClassesBuilder {
        override fun c(name: Param<String?>) {
            strings.add(name)
        }
    })
    return when {
        strings.isEmpty() -> param<String?>(null)
        strings.all { it is Param.Value } -> param(strings.map { it() }.filter { it != null }.joinToString(" "))
        else -> param { strings.map { it() }.filter { it != null }.joinToString(" ") }
    }
}