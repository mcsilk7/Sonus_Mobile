package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import network.DesktopWireGuardManager
import ui.TerminalManager

class SettingsViewModel(private val scope: CoroutineScope) {
    var isVpnConnected by mutableStateOf(false)
    var isVpnLoading by mutableStateOf(false)

    fun toggleVpn() {
        isVpnLoading = true
        scope.launch {
            if (isVpnConnected) {
                TerminalManager.addLog("TERMINATING_VPN_TUNNEL...")
                if (DesktopWireGuardManager.stopVpn()) {
                    isVpnConnected = false
                    TerminalManager.addLog("VPN_LINK_DISCONNECTED")
                } else {
                    TerminalManager.addLog("VPN_TERMINATION_FAILED")
                }
            } else {
                TerminalManager.addLog("ESTABLISHING_SECURE_TUNNEL...")
                if (DesktopWireGuardManager.startVpn()) {
                    isVpnConnected = true
                    TerminalManager.addLog("VPN_LINK_ESTABLISHED: ENCRYPTED")
                } else {
                    TerminalManager.addLog("VPN_CONNECTION_FAILED: CHECK_ACCESS_LEVEL")
                }
            }
            isVpnLoading = false
        }
    }
}
