import app.MacLineReader
import app.MorseSession
import serial.MacSerialTransport

fun main(args: Array<String>) {
    val port = args.getOrNull(0) ?: "/dev/cu.usbserial-A50285BI"

    val transport = MacSerialTransport()
    val reader = MacLineReader(transport)
    val session = MorseSession(transport, reader)

    session.connect(port, 115200)
    println("Connected to $port. Type text to play Morse.")
    println("Commands: :q exit, :ping, :help")

    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break

        when (line.trim()) {
            ":q", ":quit", ":exit" -> break
            ":help" -> {
                println("Enter any text - it will be encoded to Morse and sent as PLAY:<script>")
                println("Commands: :q exit, :ping, :help")
            }
            ":ping" -> {
                session.ping()
                println("PONG")
            }
            "" -> {}
            else -> {
                session.sendTextAsMorse(line)
                println("DONE")
            }
        }
    }

    session.disconnect()
    println("Disconnected.")
}
