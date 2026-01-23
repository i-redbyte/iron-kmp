package platform

import com.fazecast.jSerialComm.SerialPort
import domain.session.LineIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

class JvmSerialConnector : SerialConnector {
    override suspend fun connect(port: PortItem): LineIO = withContext(Dispatchers.IO) {
        val descriptor = normalizeDescriptor(port.id)
        val p = SerialPort.getCommPort(descriptor)

        p.baudRate = 115200
        p.numDataBits = 8
        p.numStopBits = SerialPort.ONE_STOP_BIT
        p.parity = SerialPort.NO_PARITY
        p.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 200, 0)

        if (!p.openPort()) error("Cannot open port: $descriptor")

        runCatching { p.flushIOBuffers() }

        val closed = AtomicBoolean(false)
        val lines = Channel<String>(capacity = Channel.BUFFERED)

        val readerThread = Thread {
            val sb = StringBuilder()
            val buf = ByteArray(256)
            try {
                while (!closed.get()) {
                    val n = p.readBytes(buf, buf.size)
                    if (n <= 0) continue

                    for (i in 0 until n) {
                        val ch = (buf[i].toInt() and 0xFF).toChar()
                        if (ch == '\r') continue
                        if (ch == '\n') {
                            val line = sb.toString()
                            sb.setLength(0)
                            if (!closed.get()) lines.trySend(line)
                        } else {
                            sb.append(ch)
                            if (sb.length > 4096) sb.setLength(0)
                        }
                    }
                }
            } catch (_: Throwable) {
            } finally {
                runCatching { lines.close() }
            }
        }.apply {
            isDaemon = true
            name = "serial-reader-$descriptor"
            start()
        }

        object : LineIO {
            override suspend fun writeLine(line: String) = withContext(Dispatchers.IO) {
                ensureOpen(closed)
                val data = (line + "\n").toByteArray(Charsets.UTF_8)
                val written = p.writeBytes(data, data.size)
                if (written <= 0) error("Write failed")
            }

            override suspend fun readLine(timeoutMs: Int): String? {
                ensureOpen(closed)
                val res = withTimeoutOrNull(timeoutMs.toLong()) { lines.receiveCatching().getOrNull() }
                if (res == null && lines.isClosedForReceive) error("Serial read stream closed")
                return res
            }

            override suspend fun waitFor(prefix: String, timeoutMs: Int): String {
                ensureOpen(closed)
                val endAt = System.currentTimeMillis() + timeoutMs
                while (System.currentTimeMillis() < endAt) {
                    val left = (endAt - System.currentTimeMillis()).toInt().coerceAtLeast(1)
                    val line = readLine(left) ?: continue
                    val t = line.trim()
                    if (t.startsWith(prefix)) return t
                }
                error("Timeout waiting for '$prefix'")
            }

            override suspend fun close() = withContext(Dispatchers.IO) {
                if (closed.compareAndSet(false, true)) {
                    runCatching { lines.close() }
                    runCatching { p.closePort() }
                    runCatching { readerThread.join(300) }
                }
            }
        }
    }

    private fun ensureOpen(closed: AtomicBoolean) {
        if (closed.get()) error("Port is closed")
    }

    private fun normalizeDescriptor(id: String): String {
        if (isMac() && id.startsWith("/dev/tty.")) return id.replaceFirst("/dev/tty.", "/dev/cu.")
        return id
    }

    private fun isMac(): Boolean {
        val os = System.getProperty("os.name")?.lowercase().orEmpty()
        return os.contains("mac")
    }
}
