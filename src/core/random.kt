package core

@Suppress("NOTHING_TO_INLINE")
class Random(var seed: Int = 0) {
    companion object {
        const val max = 0x7fffffff
    }

    inline fun nextInt(): Int {
        seed = (seed * 1103515245 + 12345) and max
        return seed
    }

    inline fun nextDouble() = nextInt().toDouble() / max.toDouble()
}