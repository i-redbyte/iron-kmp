package morse

object MorseScript {
    // Выходной формат:
    // - внутри буквы: ".-" без разделителей
    // - между буквами: пробел ' '
    // - между словами: " / "
    fun fromTextRu(input: String): String {
        val latin = RuTranslit.toLatinUpper(input)
        return fromLatin(latin)
    }

    fun fromLatin(latinUpper: String): String {
        val out = StringBuilder()
        var firstToken = true

        val words = latinUpper.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

        for (wIndex in words.indices) {
            val word = words[wIndex]
            if (wIndex > 0) {
                out.append(" / ")
                firstToken = true
            }

            for (ch in word) {
                val code = MorseAlphabet.encodeChar(ch) ?: continue
                if (!firstToken) out.append(' ')
                out.append(code)
                firstToken = false
            }
        }

        return out.toString()
    }
}
