package draw

@DslMarker
annotation class GlslDsl

sealed class TypeSize {
    object S1 : TypeSize()
    object S2 : TypeSize()
    object S3 : TypeSize()
    object S4 : TypeSize()
}

sealed class Type {
    class Float<T: TypeSize> : Type()
    class Matrix<T: TypeSize> : Type()
    object Boolean : Type()
    object Sampler : Type()
}

sealed class Storage {
    object Readonly : Storage()
    object Mutable : Storage()
}

class Expression<T: Type, S: Storage>(val value: String, val isSimple: Boolean = false) {
    override fun toString() = value
}

typealias Float1 = F<S1>
typealias Float2 = F<S2>
typealias Float3 = F<S3>
typealias Float4 = F<S4>

typealias MutableFloat1 = MF<S1>
typealias MutableFloat2 = MF<S2>
typealias MutableFloat3 = MF<S3>
typealias MutableFloat4 = MF<S4>

typealias ReadonlyFloat1 = RF<S1>
typealias ReadonlyFloat2 = RF<S2>
typealias ReadonlyFloat3 = RF<S3>
typealias ReadonlyFloat4 = RF<S4>

typealias ReadonlyMatrix3 = Expression<Type.Matrix<S3>, Storage.Readonly>
typealias ReadonlyMatrix4 = Expression<Type.Matrix<S4>, Storage.Readonly>

typealias Sampler = Expression<Type.Sampler, Storage.Readonly>

private typealias S1 = TypeSize.S1
private typealias S2 = TypeSize.S2
private typealias S3 = TypeSize.S3
private typealias S4 = TypeSize.S4

private typealias F<S> = Expression<Type.Float<S>, *>
private typealias RF<S> = Expression<Type.Float<S>, Storage.Readonly>
private typealias MF<S> = Expression<Type.Float<S>, Storage.Mutable>

private typealias B = Expression<Type.Boolean, *>
private typealias RB = Expression<Type.Boolean, Storage.Readonly>
private typealias MB = Expression<Type.Boolean, Storage.Mutable>

private fun Number.toFloatString(): String {
    val r = this.toString()
    return if (r.contains('.')) r else "$r.0"
}

@GlslDsl
class Block(private val global: StringBuilder) {

    private val local = StringBuilder()

    override fun toString() = local.toString()

    private var counter = 0
    private fun id(prefix: String) = "$prefix${counter++}"

    fun <T: Type> Expression<T, *>.seal() = Expression<T, Storage.Readonly>(value, isSimple)

    /*
     * Float constructors
     */

    fun float(v: Number) = RF<S1>(v.toFloatString(), true)
    fun float(v1: Number, v2: Number) = RF<S2>("vec2($v1, $v2)", true)
    fun float(v1: Number, v2: Number, v3: Number) = RF<S3>("vec3($v1, $v2, $v3)", true)
    fun float(v1: Number, v2: Number, v3: Number, v4: Number) = RF<S4>("vec4($v1, $v2, $v3, $v4)", true)


    fun float(v1: F<S1>, v2: F<S1>) = RF<S2>("vec2($v1, $v2)")
    fun float(v1: F<S1>, v2: F<S1>, v3: F<S1>) = RF<S3>("vec3($v1, $v2, $v3)")
    fun float(v1: F<S1>, v2: F<S1>, v3: F<S1>, v4: F<S1>) = RF<S4>("vec4($v1, $v2, $v3, $v4)")

    fun float(v1: F<S2>, v2: F<S1>) = RF<S3>("vec3($v1, $v2)")
    fun float(v1: F<S1>, v2: F<S2>) = RF<S3>("vec3($v1, $v2)")
    fun float(v1: F<S2>, v2: F<S1>, v3: F<S1>) = RF<S4>("vec4($v1, $v2, $v3)")
    fun float(v1: F<S1>, v2: F<S2>, v3: F<S1>) = RF<S4>("vec4($v1, $v2, $v3)")
    fun float(v1: F<S1>, v2: F<S1>, v3: F<S2>) = RF<S4>("vec4($v1, $v2, $v3)")
    fun float(v1: F<S2>, v2: F<S2>) = RF<S4>("vec4($v1, $v2)")

    fun float(v1: F<S3>, v2: F<S1>) = RF<S4>("vec4($v1, $v2)")
    fun float(v1: F<S1>, v2: F<S3>) = RF<S4>("vec4($v1, $v2)")

    fun float2(v: F<S1>) = RF<S2>("vec2($v)")
    fun float3(v: F<S1>) = RF<S3>("vec3($v)")
    fun float4(v: F<S1>) = RF<S4>("vec4($v)")

    fun float2(v: Number) = float2(float(v))
    fun float3(v: Number) = float3(float(v))
    fun float4(v: Number) = float4(float(v))

    /*
     * Float components
     */

    val F<S2>.x
        get() = RF<S1>("${this}.x")
    val F<S2>.y
        get() = RF<S1>("${this}.y")

    val F<S3>.x
        get() = RF<S1>("${this}.x")
    val F<S3>.y
        get() = RF<S1>("${this}.y")
    val F<S3>.z
        get() = RF<S1>("${this}.z")

    val F<S4>.x
        get() = RF<S1>("${this}.x")
    val F<S4>.y
        get() = RF<S1>("${this}.y")
    val F<S4>.z
        get() = RF<S1>("${this}.z")
    val F<S4>.w
        get() = RF<S1>("${this}.w")

    operator fun F<S2>.component1() = x
    operator fun F<S2>.component2() = y

    operator fun F<S3>.component1() = x
    operator fun F<S3>.component2() = y
    operator fun F<S3>.component3() = z

    operator fun F<S4>.component1() = x
    operator fun F<S4>.component2() = y
    operator fun F<S4>.component3() = z
    operator fun F<S4>.component4() = w



    /**
     * Comparision
     */

    infix fun F<S1>.eq(v: F<S1>) = RB("($this == $v)")
    infix fun F<S1>.ne(v: F<S1>) = RB("($this != $v)")
    infix fun F<S1>.gt(v: F<S1>) = RB("($this > $v)")
    infix fun F<S1>.gte(v: F<S1>) = RB("($this >= $v)")
    infix fun F<S1>.lt(v: F<S1>) = RB("($this < $v)")
    infix fun F<S1>.lte(v: F<S1>) = RB("($this <= $v)")

    infix fun B.and(v: F<S1>) = RB("($this && $v)")
    infix fun B.or(v: F<S1>) = RB("($this || $v)")
    operator fun B.not() = RB("(!$this)")

    /*
     * Assignments
     */

    infix fun <T: Type> Expression<T, Storage.Mutable>.assign(right: Expression<T, *>) {
        local.append(value, " = ", right, ";");
    }

    private fun <S: TypeSize> letFloat(type: String, value: F<S>): F<S> {
        val name = id("let")
        local.append(type, " ", name, " = ", value.value, ";")
        return MF<S>(name, true)
    }

    fun let(value: F<S1>) = letFloat("float", value)
    fun let(value: F<S2>) = letFloat("vec2", value)
    fun let(value: F<S3>) = letFloat("vec3", value)
    fun let(value: F<S4>) = letFloat("vec4", value)

    fun mem(v: F<S1>): F<S1> = if (v.isSimple) v else let(v)
    fun mem(v: F<S2>): F<S2> = if (v.isSimple) v else let(v)
    fun mem(v: F<S3>): F<S3> = if (v.isSimple) v else let(v)
    fun mem(v: F<S4>): F<S4> = if (v.isSimple) v else let(v)

    fun cond(condition: B, ifTrue: Block.() -> Unit, ifFalse: Block.() -> Unit) {
        local.append("if(", condition.value, ") {")
        ifTrue()
        local.append("} else {")
        ifFalse()
        local.append("}")
    }
    fun cond(condition: B, ifTrue: Block.() -> Unit) {
        local.append("if(", condition.value, ") {")
        ifTrue()
        local.append("}")
    }

    private fun <T: Type> cond(typeName: String, condition: B, ifTrue: Block.() -> Expression<T, *>, ifFalse: Block.() -> Expression<T, *>): Expression<T, Storage.Readonly> {
        val name = id("cond")
        val result = Expression<T, Storage.Mutable>(name)
        local.append("$typeName $name;")
        local.append("if(", condition.value, ") {")
        result assign ifTrue()
        local.append("} else {")
        result assign ifFalse()
        local.append("}")
        return result.seal()
    }
    fun cond1(condition: B, ifTrue: Block.() -> F<S1>, ifFalse: Block.() -> F<S1>) = cond("float", condition, ifTrue, ifFalse)
    fun cond2(condition: B, ifTrue: Block.() -> F<S2>, ifFalse: Block.() -> F<S2>) = cond("vec2", condition, ifTrue, ifFalse)
    fun cond3(condition: B, ifTrue: Block.() -> F<S3>, ifFalse: Block.() -> F<S3>) = cond("vec3", condition, ifTrue, ifFalse)
    fun cond4(condition: B, ifTrue: Block.() -> F<S4>, ifFalse: Block.() -> F<S4>) = cond("vec4", condition, ifTrue, ifFalse)

    /*
     * Float operators
     */
    operator fun <S: TypeSize> F<S>.unaryMinus() = RF<S>("(-$this)")
    operator fun <S: TypeSize> F<S>.unaryPlus() = RF<S>("(+$this)")

    operator fun F<S1>.plus(v: F<S2>) = RF<S2>("($this + $v)")
    operator fun F<S1>.plus(v: F<S3>) = RF<S3>("($this + $v)")
    operator fun F<S1>.plus(v: F<S4>) = RF<S4>("($this + $v)")
    operator fun F<S2>.plus(v: F<S1>) = RF<S2>("($this + $v)")
    operator fun F<S3>.plus(v: F<S1>) = RF<S3>("($this + $v)")
    operator fun F<S4>.plus(v: F<S1>) = RF<S4>("($this + $v)")

    operator fun F<S1>.minus(v: F<S2>) = RF<S2>("($this - $v)")
    operator fun F<S1>.minus(v: F<S3>) = RF<S3>("($this - $v)")
    operator fun F<S1>.minus(v: F<S4>) = RF<S4>("($this - $v)")
    operator fun F<S2>.minus(v: F<S1>) = RF<S2>("($this - $v)")
    operator fun F<S3>.minus(v: F<S1>) = RF<S3>("($this - $v)")
    operator fun F<S4>.minus(v: F<S1>) = RF<S4>("($this - $v)")

    operator fun F<S1>.times(v: F<S2>) = RF<S2>("($this * $v)")
    operator fun F<S1>.times(v: F<S3>) = RF<S3>("($this * $v)")
    operator fun F<S1>.times(v: F<S4>) = RF<S4>("($this * $v)")
    operator fun F<S2>.times(v: F<S1>) = RF<S2>("($this * $v)")
    operator fun F<S3>.times(v: F<S1>) = RF<S3>("($this * $v)")
    operator fun F<S4>.times(v: F<S1>) = RF<S4>("($this * $v)")

    operator fun F<S1>.div(v: F<S2>) = RF<S2>("($this / $v)")
    operator fun F<S1>.div(v: F<S3>) = RF<S3>("($this / $v)")
    operator fun F<S1>.div(v: F<S4>) = RF<S4>("($this / $v)")
    operator fun F<S2>.div(v: F<S1>) = RF<S2>("($this / $v)")
    operator fun F<S3>.div(v: F<S1>) = RF<S3>("($this / $v)")
    operator fun F<S4>.div(v: F<S1>) = RF<S4>("($this / $v)")

    operator fun F<S1>.rem(v: F<S2>) = RF<S2>("mod(vec2($this), $v)")
    operator fun F<S1>.rem(v: F<S3>) = RF<S3>("mod(vec3($this), $v)")
    operator fun F<S1>.rem(v: F<S4>) = RF<S4>("mod(vec4($this), $v)")
    operator fun F<S2>.rem(v: F<S1>) = RF<S2>("mod($this, $v)")
    operator fun F<S3>.rem(v: F<S1>) = RF<S3>("mod($this, $v)")
    operator fun F<S4>.rem(v: F<S1>) = RF<S4>("mod($this, $v)")

    operator fun <S: TypeSize> F<S>.plus(v: F<S>) = RF<S>("($this + $v)")
    operator fun <S: TypeSize> F<S>.minus(v: F<S>) = RF<S>("($this - $v)")
    operator fun <S: TypeSize> F<S>.times(v: F<S>) = RF<S>("($this * $v)")
    operator fun <S: TypeSize> F<S>.div(v: F<S>) = RF<S>("($this / $v)")
    operator fun <S: TypeSize> F<S>.rem(v: F<S>) = RF<S>("mod($this, $v)")

    operator fun F<S1>.plus(v: Number) = this + float(v)
    operator fun F<S2>.plus(v: Number) = this + float(v)
    operator fun F<S3>.plus(v: Number) = this + float(v)
    operator fun F<S4>.plus(v: Number) = this + float(v)

    operator fun F<S1>.minus(v: Number) = this - float(v)
    operator fun F<S2>.minus(v: Number) = this - float(v)
    operator fun F<S3>.minus(v: Number) = this - float(v)
    operator fun F<S4>.minus(v: Number) = this - float(v)

    operator fun F<S1>.times(v: Number) = this * float(v)
    operator fun F<S2>.times(v: Number) = this * float(v)
    operator fun F<S3>.times(v: Number) = this * float(v)
    operator fun F<S4>.times(v: Number) = this * float(v)

    operator fun F<S1>.div(v: Number) = this / float(v)
    operator fun F<S2>.div(v: Number) = this / float(v)
    operator fun F<S3>.div(v: Number) = this / float(v)
    operator fun F<S4>.div(v: Number) = this / float(v)

    operator fun F<S1>.rem(v: Number) = this % float(v)
    operator fun F<S2>.rem(v: Number) = this % float(v)
    operator fun F<S3>.rem(v: Number) = this % float(v)
    operator fun F<S4>.rem(v: Number) = this % float(v)

    operator fun Number.plus(v: F<S1>) = float(this) + v
    operator fun Number.plus(v: F<S2>) = float(this) + v
    operator fun Number.plus(v: F<S3>) = float(this) + v
    operator fun Number.plus(v: F<S4>) = float(this) + v

    operator fun Number.minus(v: F<S1>) = float(this) - v
    operator fun Number.minus(v: F<S2>) = float(this) - v
    operator fun Number.minus(v: F<S3>) = float(this) - v
    operator fun Number.minus(v: F<S4>) = float(this) - v

    operator fun Number.times(v: F<S1>) = float(this) * v
    operator fun Number.times(v: F<S2>) = float(this) * v
    operator fun Number.times(v: F<S3>) = float(this) * v
    operator fun Number.times(v: F<S4>) = float(this) * v

    operator fun Number.div(v: F<S1>) = float(this) / v
    operator fun Number.div(v: F<S2>) = float(this) / v
    operator fun Number.div(v: F<S3>) = float(this) / v
    operator fun Number.div(v: F<S4>) = float(this) / v

    operator fun Number.rem(v: F<S1>) = float(this) % v
    operator fun Number.rem(v: F<S2>) = float(this) % v
    operator fun Number.rem(v: F<S3>) = float(this) % v
    operator fun Number.rem(v: F<S4>) = float(this) % v


    operator fun MF<S3>.times(v: F<S2>) = RF<S3>("($this * vec3($v, 1))")
    operator fun MF<S3>.times(v: F<S3>) = RF<S3>("($this * $v)")
    operator fun F<S2>.times(v: MF<S3>) = RF<S3>("(vec3($v, 1) * $this)")
    operator fun F<S3>.times(v: MF<S3>) = RF<S3>("($v * $this)")

    /*
     * Functions
     */

    fun <S: TypeSize> sqrt(v: F<S>) = RF<S>("sqrt($v)")
    fun <S: TypeSize> abs(v: F<S>) = RF<S>("abs($v)")
    fun <S: TypeSize> sign(v: F<S>) = RF<S>("sign($v)")
    fun <S: TypeSize> floor(v: F<S>) = RF<S>("floor($v)")
    fun <S: TypeSize> ceil(v: F<S>) = RF<S>("ceil($v)")
    fun <S: TypeSize> fract(v: F<S>) = RF<S>("fract($v)")
    fun dot(v1: F<S2>, v2: F<S2>) = RF<S1>("dot($v1, $v2)")
    fun dot(v1: F<S3>, v2: F<S3>) = RF<S1>("dot($v1, $v2)")
    fun dot(v1: F<S4>, v2: F<S4>) = RF<S1>("dot($v1, $v2)")



    fun min(v1: F<S1>, v2: F<S1>) = RF<S1>("min($v1, $v2)")
    fun min(v1: F<S2>, v2: F<S1>) = RF<S2>("min($v1, $v2)")
    fun min(v1: F<S3>, v2: F<S1>) = RF<S3>("min($v1, $v2)")
    fun min(v1: F<S4>, v2: F<S1>) = RF<S4>("min($v1, $v2)")
    fun min(v1: F<S2>, v2: F<S2>) = RF<S2>("min($v1, $v2)")
    fun min(v1: F<S3>, v2: F<S3>) = RF<S3>("min($v1, $v2)")
    fun min(v1: F<S4>, v2: F<S4>) = RF<S4>("min($v1, $v2)")

    fun max(v1: F<S1>, v2: F<S1>) = RF<S1>("max($v1, $v2)")
    fun max(v1: F<S2>, v2: F<S1>) = RF<S2>("max($v1, $v2)")
    fun max(v1: F<S3>, v2: F<S1>) = RF<S3>("max($v1, $v2)")
    fun max(v1: F<S4>, v2: F<S1>) = RF<S4>("max($v1, $v2)")
    fun max(v1: F<S2>, v2: F<S2>) = RF<S2>("max($v1, $v2)")
    fun max(v1: F<S3>, v2: F<S3>) = RF<S3>("max($v1, $v2)")
    fun max(v1: F<S4>, v2: F<S4>) = RF<S4>("max($v1, $v2)")

    fun <S: TypeSize> log(v: F<S>) = RF<S>("log($v)")
    fun <S: TypeSize> pow(x: F<S>, y: F<S>) = RF<S>("pow($x, $y)")

    fun <S: TypeSize> sin(v: F<S>) = RF<S>("sin($v)")
    fun <S: TypeSize> cos(v: F<S>) = RF<S>("cos($v)")

    fun <S: TypeSize> length(v: F<S>) = RF<S1>("length($v)")
    fun <S: TypeSize> distance(v1: F<S>, v2: F<S>) = RF<S1>("distance($v1, $v2)")

    fun <S: TypeSize> smoothstep(edge1: F<S1>, edge2: F<S1>, v: F<S>) = RF<S>("smoothstep($edge1, $edge2, $v)")

    fun texture2D(sampler: Sampler, point: F<S2>) = RF<S4>("texture2D($sampler, $point)")

    /*
     * Predefined variables
     */

    val glPosition
        get() = MF<S4>("gl_Position")
    val glPointSize
        get() = MF<S1>("gl_PointSize")

    /**
     * Fragment position within window coordinates
     */
    val glFragCoord
        get() = RF<S4>("gl_FragCoord")
    /**
     * Fragment belongs to a front-facing primitive
     */
    val glFrontFacing
        get() = RF<S1>("gl_FrontFacing")
    /**
     * Fragment position within a 0.0 to 1.0 point (point rasterization only) for each component
     */
    val glPointCoord
        get() = RF<S2>("gl_PointCoord")

    val glFragColor
        get() = MF<S4>("gl_FragColor")
}
