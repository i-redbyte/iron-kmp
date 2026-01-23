package morse

import kotlin.text.iterator

object RuTranslit {
    private val map: Map<Char, String> = mapOf(
        'А' to "A", 'Б' to "B", 'В' to "V", 'Г' to "G", 'Д' to "D", 'Е' to "E",
        'Ё' to "E", 'Ж' to "ZH", 'З' to "Z", 'И' to "I", 'Й' to "J", 'К' to "K",
        'Л' to "L", 'М' to "M", 'Н' to "N", 'О' to "O", 'П' to "P", 'Р' to "R",
        'С' to "S", 'Т' to "T", 'У' to "U", 'Ф' to "F", 'Х' to "H", 'Ц' to "C",
        'Ч' to "CH", 'Ш' to "SH", 'Щ' to "SHCH", 'Ъ' to "", 'Ы' to "Y", 'Ь' to "",
        'Э' to "E", 'Ю' to "YU", 'Я' to "YA"
    )

    fun toLatinUpper(input: String): String {
        val out = StringBuilder()
        for (ch in input) {
            val up = ch.uppercaseChar()
            val repl = map[up]
            if (repl != null) {
                out.append(repl)
            } else {
                out.append(ch)
            }
        }
        return out.toString().uppercase()
    }
}
