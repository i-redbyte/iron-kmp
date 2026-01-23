package platform

interface PortScanner {
    suspend fun listPorts(): List<PortItem>
}
