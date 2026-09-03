package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import network.DesktopWireGuardManager

class SettingsViewModel(private val scope: CoroutineScope) {
    var isVpnConnected by mutableStateOf(false)
    var isVpnLoading by mutableStateOf(false)

    fun toggleVpn() {
        isVpnLoading = true
        scope.launch {
            if (isVpnConnected) {
                if (DesktopWireGuardManager.stopVpn()) {
                    isVpnConnected = false
                }
            } else {
                if (DesktopWireGuardManager.startVpn()) {
                    isVpnConnected = true
                }
            }
            isVpnLoading = false
        }
    }
}
