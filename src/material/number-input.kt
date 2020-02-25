package material

import core.Param
import core.param
import core.toFixed
import core.map
import oneact.*
import kotlin.js.RegExp
import kotlin.math.abs

private val integerTest = RegExp("^\\s*-?\\d+\\s*$")
private val floatTest = RegExp("^\\s*-?\\d+(\\.\\d+)?\\s*$")

private fun stringify(value: Double, isInteger: Boolean): String {
    val absValue = abs(value)
    return when {
        isInteger -> value.toFixed(0)
        absValue >= 1000.0 -> value.toFixed(0)
        absValue >= 100.0 -> value.toFixed(1)
        else -> value.toFixed(2)
    }
}

private val numberInputClass = cl("number-input")
private val styled = style("""
.$numberInputClass {
    display: flex;
    flex-direction: row;
}""")


private val nullByDefault = param<String?>(null)
private val trueByDefault = param(true)

fun numberInput(
        min: Param<Double>,
        max: Param<Double>,
        value: Param<Double>,
        className: Param<String?> = nullByDefault,
        isInteger: Param<Boolean>,
        showSlider: Param<Boolean> = trueByDefault,
        showInput: Param<Boolean> = trueByDefault,
        label: Param<String?> = nullByDefault,
        note: Param<String?> = nullByDefault,
        onChange: (value: Double) -> Unit
): El {
    var stringValue = stringify(value(), isInteger())
    var numberValue = value()

    fun validate(v: String): Boolean {
        val pattern = if (isInteger()) integerTest else floatTest
        return if (pattern.test(v)) {
            val parsed = v.trim().toDouble()
            parsed >= min() && parsed <= max()
        } else {
            false
        }
    }

    return styled(state(true) { valid, setValid ->
        val input = {
            beforeUpdate(el(
                    attr("class", classes {
                        c(numberInputClass)
                        c(className)
                    }),
                    optional(showSlider) {
                        slider(
                                min = min,
                                max = max,
                                value = value,
                                isInteger = isInteger,
                                onChange = onChange
                        )
                    },
                    optional(showInput) {
                        stringInput(
                                value = param { stringValue },
                                valid = valid,
                                onChange = { v ->
                                    stringValue = v
                                    if (validate(v)) {
                                        val d = v.toDouble()
                                        numberValue = d
                                        setValid(true)
                                        onChange(d)
                                    } else {
                                        setValid(false)
                                    }
                                }
                        )
                    }
            )) {
                val currentValue = value()
                if (numberValue != currentValue) {
                    numberValue = currentValue
                    stringValue = stringify(numberValue, isInteger())
                    setValid(true)
                }
            }
        }

        conditional(map(label) { it != null }, {
            material.label(
                    text = map(label) { it ?: "" },
                    note = note,
                    valid = valid,
                    content = input()
            )
        }, input)
    })
}