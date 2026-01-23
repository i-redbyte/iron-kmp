package platform

import domain.session.LineIO

interface SerialConnector {
    suspend fun connect(port: PortItem): LineIO
}
