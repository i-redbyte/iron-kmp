package app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import platform.JvmPortScanner
import platform.JvmSerialConnector
import ui.App
import ui.ConsoleLog
import ui.MainViewModel

fun main() = application {
    val log = ConsoleLog()
    val vm = MainViewModel(
        portScanner = JvmPortScanner(),
        serialConnector = JvmSerialConnector(),
        log = log,
        initialLocale = MainViewModel.defaultLocale()
    )

    Window(
        onCloseRequest = {
            runBlocking { vm.shutdown() }
            exitApplication()
        },
        title = "[RB]Iron Morse KMP[RB]"
    ) {
        vm.refreshPorts()
        App(vm, log)
    }
}
