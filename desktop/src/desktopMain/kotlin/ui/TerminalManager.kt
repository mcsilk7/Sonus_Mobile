package ui

import androidx.compose.runtime.mutableStateListOf

object TerminalManager {
    val logs = mutableStateListOf<String>()

    fun addLog(message: String) {
        logs.add(0, "> $message")
        if (logs.size > 100) logs.removeLast()
    }
}
