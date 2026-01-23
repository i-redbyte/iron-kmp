package domain.session

import domain.Protocol
import morse.MorseEstimate
import morse.MorseScript
import morse.MorseTiming

class MorseSession(
    private val io: LineIO,
    private val timing: MorseTiming = MorseTiming()
) {
    suspend fun connect() {
        io.waitFor(Protocol.READY, timeoutMs = 5000)
    }

    suspend fun ping() {
        io.writeLine(Protocol.PING)
        io.waitFor(Protocol.PONG, timeoutMs = 2000)
    }

    suspend fun sendText(text: String) {
        val script = MorseScript.fromTextRu(text)
        io.writeLine(Protocol.PLAY_PREFIX + script)
        io.waitFor(Protocol.OK, timeoutMs = 2000)

        val doneTimeout = MorseEstimate.estimateDoneTimeoutMs(
            script = script,
            timing = timing,
            safetyFactor = 1.8,
            overheadMs = 3000,
            minMs = 10000,
            maxMs = 10 * 60 * 1000
        )

        io.waitFor(Protocol.DONE, timeoutMs = doneTimeout)
    }

    suspend fun disconnect() {
        io.writeLine(Protocol.BYE)
    }

    suspend fun close() {
        io.close()
    }
}
