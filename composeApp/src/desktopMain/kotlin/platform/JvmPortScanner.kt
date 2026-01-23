package platform

import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JvmPortScanner : PortScanner {
    override suspend fun listPorts(): List<PortItem> = withContext(Dispatchers.IO) {
        val raw = SerialPort.getCommPorts().map { p ->
            val path = runCatching { p.systemPortPath }.getOrNull()?.takeIf { it.isNotBlank() }
            val name = p.systemPortName.takeIf { it.isNotBlank() }
            val id = (path ?: name ?: "").ifBlank { p.descriptivePortName ?: "" }

            val desc = p.descriptivePortName?.takeIf { it.isNotBlank() }
                ?: p.portDescription?.takeIf { it.isNotBlank() }
                ?: "Serial"

            PortItem(id = id, title = "$id - $desc")
        }.filter { it.id.isNotBlank() }

        val normalized = raw.map { item ->
            if (isMac() && item.id.startsWith("/dev/tty.")) {
                val cu = item.id.replaceFirst("/dev/tty.", "/dev/cu.")
                item.copy(id = cu, title = item.title.replaceFirst("/dev/tty.", "/dev/cu."))
            } else item
        }

        val dedup = normalized
            .distinctBy { it.id }
            .sortedBy { it.title.lowercase() }

        if (!isMac()) return@withContext dedup

        val ids = dedup.map { it.id }.toSet()
        dedup.filter { it.id.startsWith("/dev/cu.") || !ids.contains(it.id.replaceFirst("/dev/cu.", "/dev/tty.")) }
    }

    private fun isMac(): Boolean {
        val os = System.getProperty("os.name")?.lowercase().orEmpty()
        return os.contains("mac")
    }
}
