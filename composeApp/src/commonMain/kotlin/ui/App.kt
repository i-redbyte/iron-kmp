package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun App(vm: MainViewModel, log: ConsoleLog) {
    val state = vm.state.collectAsState().value
    val strings = when (state.locale) {
        AppLocale.RU -> StringsRu
        AppLocale.EN -> StringsEn
    }

    AppTheme {
        MainScreen(
            state = state,
            strings = strings,
            log = log,
            onRefresh = vm::refreshPorts,
            onToggleAuto = vm::setAutoRefresh,
            onSelectPort = vm::selectPort,
            onConnect = vm::connect,
            onDisconnect = vm::disconnect,
            onInputChange = vm::setInput,
            onSend = vm::send,
            onLocaleChange = vm::setLocale
        )
    }
}
