package domain.session

interface LineIO {
    suspend fun writeLine(line: String)
    suspend fun readLine(timeoutMs: Int): String?
    suspend fun waitFor(prefix: String, timeoutMs: Int): String
    suspend fun close()
}
