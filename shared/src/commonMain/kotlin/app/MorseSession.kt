package app

import domain.Protocol.BYE
import domain.Protocol.DONE
import domain.Protocol.PING
import domain.Protocol.PLAY_PREFIX
import domain.Protocol.PONG
import domain.Protocol.READY
import morse.MorseEstimate
import morse.MorseScript
import morse.MorseTiming
import serial.SerialTransport

interface LineReader {
    fun readLine(timeoutMs: Int): String?
    fun waitForLine(startsWith: String, timeoutMs: Int): String
}

class MorseSession(
    private val transport: SerialTransport,
    private val reader: LineReader,
    private val timing: MorseTiming = MorseTiming()
) {
    fun connect(portPath: String, baud: Int = 115200) {
        transport.open(portPath, baud)
        reader.waitForLine(READY, timeoutMs = 5000)
    }

    fun sendTextAsMorse(text: String) {
        val script = MorseScript.fromTextRu(text)
        transport.writeLine("$PLAY_PREFIX$script")

        reader.waitForLine("OK", timeoutMs = 2000)

        val doneTimeout = MorseEstimate.estimateDoneTimeoutMs(
            script = script,
            timing = timing,
            safetyFactor = 1.8,
            overheadMs = 3_000,
            minMs = 10_000,
            maxMs = 10 * 60 * 1000
        )

        reader.waitForLine(DONE, timeoutMs = doneTimeout)
    }

    fun ping() {
        transport.writeLine(PING)
        reader.waitForLine(PONG, timeoutMs = 2000)
    }

    fun disconnect() {
        transport.writeLine(BYE)
        reader.readLine(500)
        transport.close()
    }
}
