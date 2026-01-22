package morse

data class MorseTiming(
    val dotMs: Int = 120,
    val dashMs: Int = dotMs * 3,
    val symbolGapMs: Int = dotMs,
    val letterGapMs: Int = dotMs * 3,
    val wordGapMs: Int = dotMs * 7
)
