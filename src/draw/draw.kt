package draw

import core.JsArray

@GlslDsl
class DrawContext(
        private val gl: Gl,
        private val primitivesType: PrimitivesType,
        private val attributes: Attributes,
        private val instances: Attributes?,
        private val elements: Buffer? = null
) {
    private val initializers = JsArray<(program: Program) -> Unit>()
    private val vertexSource = StringBuilder()
    private val fragmentSource = StringBuilder()

    init {
        vertexSource.append("precision highp float;precision highp int;")
        fragmentSource.append("precision highp float;precision highp int;")
    }

    private inline fun <T> checkAttributeSize(name: String, size: Int, block: () -> T) = if (attributes.size(name) == size) {
        block()
    } else {
        throw Error("Attribute $name has different size ${attributes.size(name)}")
    }

    private inline fun <T> checkIndexSize(name: String, size: Int, block: () -> T) = when {
        instances == null -> throw Error("Index '$name' not found")
        instances.size(name) == size -> block()
        else -> throw Error("Index $name has different size ${attributes.size(name)}")
    }

    fun attribute1(name: String) = checkAttributeSize(name, 1) {
        vertexSource.append("attribute float $name;")
        ReadonlyFloat1(name)
    }
    fun attribute2(name: String) = checkAttributeSize(name, 2) {
        vertexSource.append("attribute vec2 $name;")
        ReadonlyFloat2(name)
    }
    fun attribute3(name: String) = checkAttributeSize(name, 3) {
        vertexSource.append("attribute vec3 $name;")
        ReadonlyFloat3(name)
    }
    fun attribute4(name: String) = checkAttributeSize(name, 4) {
        vertexSource.append("attribute vec4 $name;")
        ReadonlyFloat4(name)
    }

    fun index1(name: String) = checkIndexSize(name, 1) {
        vertexSource.append("attribute float $name;")
        ReadonlyFloat1(name)
    }
    fun index2(name: String) = checkIndexSize(name, 2) {
        vertexSource.append("attribute vec2 $name;")
        ReadonlyFloat2(name)
    }
    fun index3(name: String) = checkIndexSize(name, 3) {
        vertexSource.append("attribute vec3 $name;")
        ReadonlyFloat3(name)
    }
    fun index4(name: String) = checkIndexSize(name, 4) {
        vertexSource.append("attribute vec4 $name;")
        ReadonlyFloat4(name)
    }

    fun varying1(name: String): MutableFloat1 {
        vertexSource.append("varying float $name;")
        fragmentSource.append("varying float $name;")
        return MutableFloat1(name)
    }
    fun varying2(name: String): MutableFloat2 {
        vertexSource.append("varying vec2 $name;")
        fragmentSource.append("varying vec2 $name;")
        return MutableFloat2(name)
    }
    fun varying3(name: String): MutableFloat3 {
        vertexSource.append("varying vec3 $name;")
        fragmentSource.append("varying vec3 $name;")
        return MutableFloat3(name)
    }
    fun varying4(name: String): MutableFloat4 {
        vertexSource.append("varying vec4 $name;")
        fragmentSource.append("varying vec4 $name;")
        return MutableFloat4(name)
    }

    fun uniform(name: String, v: Double): ReadonlyFloat1 {
        vertexSource.append("uniform float $name;")
        fragmentSource.append("uniform float $name;")
        initializers.add { it.setUniform(name, v) }
        return ReadonlyFloat1(name)
    }
    fun uniform(name: String, v1: Double, v2: Double): ReadonlyFloat2 {
        vertexSource.append("uniform vec2 $name;")
        fragmentSource.append("uniform vec2 $name;")
        initializers.add { it.setUniform(name, arrayOf(v1, v2)) }
        return ReadonlyFloat2(name)
    }
    fun uniform(name: String, v1: Double, v2: Double, v3: Double): ReadonlyFloat3 {
        vertexSource.append("uniform vec3 $name;")
        fragmentSource.append("uniform vec3 $name;")
        initializers.add { it.setUniform(name, arrayOf(v1, v2, v3)) }
        return ReadonlyFloat3(name)
    }
    fun uniform(name: String, v1: Double, v2: Double, v3: Double, v4: Double): ReadonlyFloat4 {
        vertexSource.append("uniform vec4 $name;")
        fragmentSource.append("uniform vec4 $name;")
        initializers.add { it.setUniform(name, arrayOf(v1, v2, v3, v4)) }
        return ReadonlyFloat4(name)
    }

    fun uniform(name: String, v: Matrix): ReadonlyMatrix3 {
        vertexSource.append("uniform mat3 $name;")
        fragmentSource.append("uniform mat3 $name;")
        initializers.add { it.setUniform(name, v) }
        return ReadonlyMatrix3(name)
    }
    fun uniform(name: String, v: Vector) = uniform(name, v.x, v.y)

    fun texture(name: String, v: Int): Sampler {
        vertexSource.append("uniform sampler2D $name;")
        fragmentSource.append("uniform sampler2D $name;")
        initializers.add { it.setUniform(name, v) }
        return Sampler(name)
    }

    fun vertex(callback: Block.() -> Unit) {
        val block = Block(vertexSource)
        callback(block)
        vertexSource.append("void main() { $block }")
    }
    fun fragment(callback: Block.() -> Unit) {
        val block = Block(fragmentSource)
        callback(block)
        fragmentSource.append("void main() { $block }")
    }

    internal fun draw(callback: DrawContext.() -> Unit) {
        callback(this)
        gl.program(vertexSource.toString(), fragmentSource.toString()) { program ->
            initializers.forEach { v -> v(program) }
            attributes.apply(program) {
                if (instances !== null) {
                    instances.apply(program) {
                        if (elements != null) {
                            gl.settings().buffer(elements).apply {
                                gl.drawsInstancedElements(primitivesType, elements.elementsCount, instances.size)
                            }
                        } else {
                            gl.drawInstancedArrays(primitivesType, attributes.size, instances.size)
                        }
                    }
                } else {
                    if (elements != null) {
                        gl.settings().buffer(elements).apply {
                            gl.drawsElements(primitivesType, elements.elementsCount)
                        }
                    } else {
                        gl.drawArrays(primitivesType, attributes.size)
                    }
                }
            }
        }
    }
}

fun Gl.draw(primitivesType: PrimitivesType, attributes: Attributes, instances: Attributes, callback: DrawContext.() -> Unit) {
    DrawContext(this, primitivesType, attributes, instances).draw(callback)
}

fun Gl.draw(primitivesType: PrimitivesType, attributes: Attributes, callback: DrawContext.() -> Unit) {
    DrawContext(this, primitivesType, attributes, null).draw(callback)
}