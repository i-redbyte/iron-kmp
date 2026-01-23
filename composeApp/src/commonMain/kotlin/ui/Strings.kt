package ui

interface Strings {
    val appTitle: String
    val device: String
    val connect: String
    val disconnect: String
    val refresh: String
    val autoRefresh: String
    val send: String
    val console: String
    val language: String
    val inputHint: String
    val portsEmpty: String

    fun logPortsUpdated(count: Int): String
    fun logAutoRefreshOn(periodMs: Long): String
    fun logAutoRefreshOff(): String
    fun logConnecting(target: String): String
    fun logConnected(target: String): String
    fun logDisconnected(): String
    fun logConnectFailed(message: String): String
    fun logSend(text: String): String
    fun logDone(): String
    fun logSendFailed(message: String): String
    fun logNotConnected(): String
}

object StringsRu : Strings {
    override val appTitle = "Iron Morse"
    override val device = "Устройство"
    override val connect = "Подключиться"
    override val disconnect = "Отключиться"
    override val refresh = "Обновить"
    override val autoRefresh = "Автообновление"
    override val send = "Отправить"
    override val console = "Консоль"
    override val language = "Язык"
    override val inputHint = "Введите текст"
    override val portsEmpty = "Устройств не найдено"

    override fun logPortsUpdated(count: Int) = "Список устройств обновлен - найдено $count"
    override fun logAutoRefreshOn(periodMs: Long) = "Автообновление включено - каждые ${periodMs}мс"
    override fun logAutoRefreshOff() = "Автообновление выключено"
    override fun logConnecting(target: String) = "Подключение к $target..."
    override fun logConnected(target: String) = "Подключено - $target"
    override fun logDisconnected() = "Отключено"
    override fun logConnectFailed(message: String) = "Ошибка подключения - $message"
    override fun logSend(text: String) = "Отправка: $text"
    override fun logDone() = "Готово - DONE"
    override fun logSendFailed(message: String) = "Ошибка отправки - $message"
    override fun logNotConnected() = "Не подключено - отправка невозможна"
}

object StringsEn : Strings {
    override val appTitle = "Iron Morse"
    override val device = "Device"
    override val connect = "Connect"
    override val disconnect = "Disconnect"
    override val refresh = "Refresh"
    override val autoRefresh = "Auto refresh"
    override val send = "Send"
    override val console = "Console"
    override val language = "Language"
    override val inputHint = "Enter text"
    override val portsEmpty = "No devices found"

    override fun logPortsUpdated(count: Int) = "Ports updated - found $count"
    override fun logAutoRefreshOn(periodMs: Long) = "Auto refresh enabled - every ${periodMs}ms"
    override fun logAutoRefreshOff() = "Auto refresh disabled"
    override fun logConnecting(target: String) = "Connecting to $target..."
    override fun logConnected(target: String) = "Connected - $target"
    override fun logDisconnected() = "Disconnected"
    override fun logConnectFailed(message: String) = "Connection error - $message"
    override fun logSend(text: String) = "Sending: $text"
    override fun logDone() = "Done - DONE"
    override fun logSendFailed(message: String) = "Send error - $message"
    override fun logNotConnected() = "Not connected - cannot send"
}
