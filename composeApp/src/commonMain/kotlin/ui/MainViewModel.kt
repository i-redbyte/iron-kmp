package ui

import domain.session.MorseSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.PortItem
import platform.PortScanner
import platform.SerialConnector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val portScanner: PortScanner,
    private val serialConnector: SerialConnector,
    private val log: ConsoleLog,
    initialLocale: AppLocale
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val timeFmt = SimpleDateFormat("HH:mm:ss")

    private val _state = MutableStateFlow(
        UiState(
            locale = initialLocale,
            ports = emptyList(),
            selectedPort = null,
            autoRefresh = false,
            connectionState = ConnectionState.DISCONNECTED,
            inputText = ""
        )
    )
    val state: StateFlow<UiState> = _state

    private var autoJob: Job? = null
    private var session: MorseSession? = null

    private fun strings(locale: AppLocale): Strings = when (locale) {
        AppLocale.RU -> StringsRu
        AppLocale.EN -> StringsEn
    }

    private fun ts(): String = timeFmt.format(Date())

    fun setLocale(locale: AppLocale) {
        _state.update { it.copy(locale = locale) }
    }

    fun setInput(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun selectPort(port: PortItem?) {
        _state.update { it.copy(selectedPort = port) }
    }

    fun refreshPorts() {
        scope.launch {
            val s = strings(_state.value.locale)
            runCatching { portScanner.listPorts() }
                .onSuccess { list ->
                    _state.update { st ->
                        val selected = st.selectedPort
                        val stillExists = selected?.let { sel -> list.any { it.id == sel.id } } ?: true
                        st.copy(
                            ports = list,
                            selectedPort = if (stillExists) selected else null
                        )
                    }
                    log.add(ts(), s.logPortsUpdated(list.size))
                }
                .onFailure { e ->
                    log.add(ts(), s.logConnectFailed(e.message ?: e::class.simpleName.orEmpty()))
                }
        }
    }

    fun setAutoRefresh(enabled: Boolean, periodMs: Long = 2500L) {
        val s = strings(_state.value.locale)
        _state.update { it.copy(autoRefresh = enabled) }

        scope.launch {
            autoJob?.cancel()
            autoJob = null
            if (enabled) {
                log.add(ts(), s.logAutoRefreshOn(periodMs))
                autoJob = launch {
                    while (isActive) {
                        refreshPorts()
                        delay(periodMs)
                    }
                }
            } else {
                log.add(ts(), s.logAutoRefreshOff())
            }
        }
    }

    fun connect() {
        val st = _state.value
        val s = strings(st.locale)
        val port = st.selectedPort ?: return
        if (st.connectionState != ConnectionState.DISCONNECTED) return

        _state.update { it.copy(connectionState = ConnectionState.CONNECTING) }
        log.add(ts(), s.logConnecting(port.title))

        scope.launch {
            var createdSession: MorseSession? = null
            runCatching {
                val io = serialConnector.connect(port)
                val sess = MorseSession(io)
                createdSession = sess
                sess.connect()
                session = sess
            }.onSuccess {
                _state.update { it.copy(connectionState = ConnectionState.CONNECTED) }
                log.add(ts(), s.logConnected(port.title))
            }.onFailure { e ->
                _state.update { it.copy(connectionState = ConnectionState.DISCONNECTED) }
                runCatching { createdSession?.close() }
                session = null
                log.add(ts(), s.logConnectFailed(e.message ?: e::class.simpleName.orEmpty()))
            }
        }
    }

    fun disconnect() {
        val st = _state.value
        val s = strings(st.locale)
        if (st.connectionState == ConnectionState.DISCONNECTED) return

        _state.update { it.copy(connectionState = ConnectionState.DISCONNECTED) }

        scope.launch {
            runCatching {
                val sess = session
                session = null
                if (sess != null) {
                    runCatching { sess.disconnect() }
                    sess.close()
                }
            }
            log.add(ts(), s.logDisconnected())
        }
    }

    fun send() {
        val st = _state.value
        val s = strings(st.locale)
        val text = st.inputText.trim()
        if (text.isEmpty()) return

        val sess = session
        if (sess == null || st.connectionState != ConnectionState.CONNECTED) {
            log.add(ts(), s.logNotConnected())
            return
        }

        _state.update { it.copy(inputText = "") }
        log.add(ts(), s.logSend(text))

        scope.launch {
            runCatching { sess.sendText(text) }
                .onSuccess { log.add(ts(), s.logDone()) }
                .onFailure { e -> log.add(ts(), s.logSendFailed(e.message ?: e::class.simpleName.orEmpty())) }
        }
    }

    suspend fun shutdown() {
        autoJob?.let {
            it.cancel()
            runCatching { it.cancelAndJoin() }
        }
        autoJob = null
        runCatching { session?.disconnect() }
        runCatching { session?.close() }
        session = null
        scope.coroutineContext[Job]?.cancel()
    }

    companion object {
        fun defaultLocale(): AppLocale {
            val lang = Locale.getDefault().language.lowercase()
            return if (lang.startsWith("ru")) AppLocale.RU else AppLocale.EN
        }
    }
}
