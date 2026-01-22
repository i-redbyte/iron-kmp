package app

import serial.MacSerialTransport

class MacLineReader(
    private val mac: MacSerialTransport
) : LineReader {

    override fun readLine(timeoutMs: Int): String? = mac.readLine(timeoutMs)

    override fun waitForLine(startsWith: String, timeoutMs: Int): String = mac.waitForLine(startsWith, timeoutMs)
}
