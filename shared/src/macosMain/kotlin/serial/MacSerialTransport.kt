package serial

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
class MacSerialTransport : SerialTransport {
    private var fd: Int = -1

    override fun open(path: String, baud: Int) {
        check(fd < 0) { "Port already open" }

        fd = platform.posix.open(path, O_RDWR or O_NOCTTY or O_NONBLOCK)
        require(fd >= 0) { "Cannot open port: $path, errno=$errno" }

        memScoped {
            val tio = alloc<termios>()

            if (tcgetattr(fd, tio.ptr) != 0) {
                val e = errno
                close(fd)
                fd = -1
                error("tcgetattr failed errno=$e")
            }

            cfmakeraw(tio.ptr)

            val speed = when (baud) {
                9600 -> B9600
                19200 -> B19200
                38400 -> B38400
                57600 -> B57600
                115200 -> B115200
                else -> {
                    close(fd)
                    fd = -1
                    error("Unsupported baud: $baud")
                }
            }

            cfsetispeed(tio.ptr, speed.toULong())
            cfsetospeed(tio.ptr, speed.toULong())

            // 8N1
            tio.c_cflag = tio.c_cflag or CLOCAL.toULong() or CREAD.toULong()
            tio.c_cflag = tio.c_cflag and PARENB.toULong().inv()
            tio.c_cflag = tio.c_cflag and CSTOPB.toULong().inv()
            tio.c_cflag = tio.c_cflag and CSIZE.toULong().inv()
            tio.c_cflag = tio.c_cflag or CS8.toULong()

            tio.c_cc[VMIN] = 0u
            tio.c_cc[VTIME] = 1u // 0.1s

            if (tcsetattr(fd, TCSANOW, tio.ptr) != 0) {
                val e = errno
                close(fd)
                fd = -1
                error("tcsetattr failed errno=$e")
            }
        }

        val flags = fcntl(fd, F_GETFL, 0)
        fcntl(fd, F_SETFL, flags and O_NONBLOCK.inv())
    }

    override fun writeLine(line: String) {
        check(fd >= 0) { "Port not open" }
        val data = (line + "\n").encodeToByteArray()

        data.usePinned { pinned ->
            var offset = 0
            while (offset < data.size) {
                val n = write(fd, pinned.addressOf(offset), (data.size - offset).toULong()).toInt()
                if (n < 0) error("write failed errno=${errno}")
                offset += n
            }
        }
    }

    fun readLine(timeoutMs: Int): String? {
        check(fd >= 0) { "Port not open" }

        val deadline = nowMs() + timeoutMs
        val sb = StringBuilder()
        val one = ByteArray(1)

        while (nowMs() < deadline) {
            val n = one.usePinned { pinned ->
                read(fd, pinned.addressOf(0), 1u).toInt()
            }

            if (n > 0) {
                val ch = one[0].toInt().toChar()
                if (ch == '\r') continue
                if (ch == '\n') return sb.toString()
                sb.append(ch)
            } else {
                usleep(2_000u)
            }
        }
        return null
    }

    fun waitForLine(startsWith: String, timeoutMs: Int): String {
        val deadline = nowMs() + timeoutMs
        while (nowMs() < deadline) {
            val left = (deadline - nowMs()).toInt().coerceAtLeast(1)
            val raw = readLine(left) ?: continue
            val line = raw.trim()
            if (line.startsWith(startsWith)) return line
        }
        error("Timeout waiting for '$startsWith'")
    }

    override fun close() {
        if (fd >= 0) {
            close(fd)
            fd = -1
        }
    }

    private fun nowMs(): Long = memScoped {
        val tv = alloc<timeval>()
        gettimeofday(tv.ptr, null)
        tv.tv_sec * 1000L + tv.tv_usec.toLong() / 1000L
    }
}
