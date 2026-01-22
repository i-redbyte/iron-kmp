package serial

interface SerialTransport {
    fun open(path: String, baud: Int = 115200)
    fun writeLine(line: String)
    fun close()
}
