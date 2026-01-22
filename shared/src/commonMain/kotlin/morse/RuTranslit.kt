package morse

object RuTranslit {

    fun toLatinUpper(input: String): String {
        val sb = StringBuilder(input.length * 2)
        for (ch in input) {
            sb.append(mapChar(ch))
        }
        return sb.toString()
    }

    private fun mapChar(ch: Char): String {
        val c = ch.uppercaseChar()

        return when (c) {
            'А' -> "A"
            'Б' -> "B"
            'В' -> "V"
            'Г' -> "G"
            'Д' -> "D"
            'Е' -> "E"
            'Ё' -> "E"
            'Ж' -> "ZH"
            'З' -> "Z"
            'И' -> "I"
            'Й' -> "J"
            'К' -> "K"
            'Л' -> "L"
            'М' -> "M"
            'Н' -> "N"
            'О' -> "O"
            'П' -> "P"
            'Р' -> "R"
            'С' -> "S"
            'Т' -> "T"
            'У' -> "U"
            'Ф' -> "F"
            'Х' -> "H"
            'Ц' -> "C"
            'Ч' -> "CH"
            'Ш' -> "SH"
            'Щ' -> "SCH"
            'Ъ' -> ""
            'Ы' -> "Y"
            'Ь' -> ""
            'Э' -> "E"
            'Ю' -> "YU"
            'Я' -> "YA"
            in 'A'..'Z' -> c.toString()
            in '0'..'9' -> c.toString()
            ' ' -> " "
            '.' -> "."
            ',' -> ","
            '?' -> "?"
            '!' -> "!"
            '-' -> "-"
            ':' -> ":"
            ';' -> ";"
            '(' -> "("
            ')' -> ")"
            '\'' -> "'"
            '"' -> "\""
            '/' -> "/"
            '@' -> "@"
            else -> ""
        }
    }
}
