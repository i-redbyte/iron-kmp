package ui

import platform.PortItem

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED
}

data class UiState(
    val locale: AppLocale,
    val ports: List<PortItem>,
    val selectedPort: PortItem?,
    val autoRefresh: Boolean,
    val connectionState: ConnectionState,
    val inputText: String
)
