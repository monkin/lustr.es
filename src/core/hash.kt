package core

@Suppress("NOTHING_TO_INLINE")
inline infix fun Int.hash(v: Any) = this * 31 + v.hashCode()

@Suppress("NOTHING_TO_INLINE")
inline infix fun Any.hash(v: Any) = this.hashCode() * 31 + v.hashCode()
