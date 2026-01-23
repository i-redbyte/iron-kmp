package ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConsoleLog(
    private val maxLines: Int = 400
) {
    private val _lines = MutableStateFlow<List<LogLine>>(emptyList())
    val lines: StateFlow<List<LogLine>> = _lines

    fun add(ts: String, text: String) {
        val next = (_lines.value + LogLine(ts, text)).takeLast(maxLines)
        _lines.value = next
    }

    fun clear() {
        _lines.value = emptyList()
    }
}
