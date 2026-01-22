package morse

object MorseEstimate {

    /**
     * Грубая оценка длительности воспроизведения скрипта на Arduino.
     *
     * Скрипт содержит:
     * '.'  -> DOT + SYMBOL_GAP
     * '-'  -> DASH + SYMBOL_GAP
     * ' '  -> LETTER_GAP
     * '/'  -> WORD_GAP
     *
     * Важно: это оценка "как в прошивке": после каждого символа '.'/'-' делаем SYMBOL_GAP.
     */
    fun estimatePlayMs(script: String, timing: MorseTiming = MorseTiming()): Int {
        var total = 0L

        for (ch in script) {
            total += when (ch) {
                '.' -> (timing.dotMs + timing.symbolGapMs).toLong()
                '-' -> (timing.dashMs + timing.symbolGapMs).toLong()
                ' ' -> timing.letterGapMs.toLong()
                '/' -> timing.wordGapMs.toLong()
                else -> 0L
            }
        }

        return total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /**
     * Таймаут ожидания DONE:
     * - берем оценку длительности
     * - умножаем на safetyFactor (запас)
     * - добавляем фиксированный overhead (на ресет, буферы, печать, мелкие задержки)
     * - ограничиваем минимумом и максимумом
     */
    fun estimateDoneTimeoutMs(
        script: String,
        timing: MorseTiming = MorseTiming(),
        safetyFactor: Double = 1.8,
        overheadMs: Int = 3_000,
        minMs: Int = 10_000,
        maxMs: Int = 10 * 60 * 1000 // 10 минут
    ): Int {
        val play = estimatePlayMs(script, timing)
        val withReserve = (play * safetyFactor).toLong() + overheadMs
        val clamped = withReserve.coerceIn(minMs.toLong(), maxMs.toLong())
        return clamped.toInt()
    }
}
