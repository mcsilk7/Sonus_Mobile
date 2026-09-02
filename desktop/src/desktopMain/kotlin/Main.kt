import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.sonus.DependencyContainer
import com.example.sonus.DesktopDateFormatter
import com.example.sonus.DesktopNetworkMonitor
import com.example.sonus.db.getDatabaseBuilder
import data.DesktopSessionManager
import audio.DesktopPlayer
import kotlinx.coroutines.launch
import network.DesktopWireGuardManager
import ui.DesktopDI
import ui.Screen
import ui.TerminalManager
import ui.components.*
import ui.screens.*
import ui.theme.SonusTheme
import ui.theme.StudioBg


fun main() {
    DesktopDI.init()
    
    application {
        val icon = SonusLogoPainter()
        val windowState = rememberWindowState(
            width = 1280.dp,
            height = 800.dp
        )

        Window(
            onCloseRequest = {
                // Próbujemy wyłączyć VPN przy zamykaniu - tylko jeśli mamy uprawnienia (bez hasła)
                try {
                    val isFlatpak = java.io.File("/.flatpak-info").exists()
                    // Używamy "sudo -n" (non-interactive), aby uniknąć okna z hasłem przy zamykaniu
                    val command = if (isFlatpak) {
                        arrayOf("flatpak-spawn", "--host", "sudo", "-n", "wg-quick", "down", "/tmp/sonus_vpn.conf")
                    } else {
                        arrayOf("sudo", "-n", "wg-quick", "down", "/tmp/sonus_vpn.conf")
                    }
                    Runtime.getRuntime().exec(command).waitFor()
                } catch (e: Exception) {}
                exitApplication()
            },
            title = "Sonus",
            icon = icon,
            state = windowState
        ) {
            
            SonusTheme {
                val scope = rememberCoroutineScope()
                var currentScreen by remember { 
                    mutableStateOf<Screen>(if (DesktopDI.sessionManager.isLoggedIn()) Screen.Home else Screen.Login) 
                }
                var isLoggedIn by remember { mutableStateOf(DesktopDI.sessionManager.isLoggedIn()) }
                var showVpnSetup by remember { mutableStateOf(!DesktopDI.sessionManager.isVpnConfigured()) }

                // Auto-connect VPN on startup
                LaunchedEffect(Unit) {
                    TerminalManager.addLog("SYSTEM_BOOT_SEQUENCE_INITIATED")
                    
                    if (DesktopDI.sessionManager.isVpnConfigured()) {
                        if (DesktopWireGuardManager.hasPasswordlessAccess()) {
                            TerminalManager.addLog("VPN_PERMISSIONS: PASSWORDLESS_OK")
                            TerminalManager.addLog("ESTABLISHING_SECURE_TUNNEL...")
                            
                            val success = DesktopWireGuardManager.startVpn()
                            if (success) {
                                TerminalManager.addLog("VPN_LINK_ESTABLISHED: ENCRYPTED")
                            } else {
                                TerminalManager.addLog("VPN_LINK_FAILED: CHECK_WIREGUARD_STATUS")
                            }
                        } else {
                            TerminalManager.addLog("VPN_PERMISSIONS: MISSING (Setup required again?)")
                            showVpnSetup = true
                        }
                    } else {
                        TerminalManager.addLog("VPN_PERMISSIONS: FIRST_RUN_SETUP_REQUIRED")
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!isLoggedIn) {
                        LoginScreen(onLoginSuccess = { 
                            isLoggedIn = true 
                            currentScreen = Screen.Home 
                        })
                    } else {
                        Column(modifier = Modifier.fillMaxSize().background(StudioBg)) {
                            HeaderBar(onLogout = {
                                DesktopDI.sessionManager.clearSession()
                                isLoggedIn = false
                                currentScreen = Screen.Login
                            })
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                Sidebar(currentScreen) { currentScreen = it }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    when (currentScreen) {
                                        is Screen.Home -> HomeScreen()
                                        is Screen.Search -> SearchScreen()
                                        is Screen.Library -> LibraryScreen()
                                        is Screen.Settings -> SettingsScreen()
                                        else -> {}
                                    }
                                }
                                RightPanel()
                            }
                            PlayerBar()
                        }
                    }

                    // VPN Setup Overlay (Brama wejściowa - "Tylko jedna droga")
                    if (showVpnSetup) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            VpnSetupDialog(
                                onConfirm = {
                                    scope.launch {
                                        TerminalManager.addLog("RUNNING_PERM_SETUP...")
                                        val success = DesktopWireGuardManager.runPermissionSetup()
                                        if (success) {
                                            TerminalManager.addLog("KONFIGURACJA_UKONCZONA: SUKCES")
                                            DesktopDI.sessionManager.setVpnConfigured(true)
                                            
                                            // Po sukcesie, od razu odpalamy VPN
                                            if (DesktopWireGuardManager.hasPasswordlessAccess()) {
                                                TerminalManager.addLog("ESTABLISHING_SECURE_TUNNEL...")
                                                DesktopWireGuardManager.startVpn()
                                            }
                                            showVpnSetup = false
                                        } else {
                                            TerminalManager.addLog("ERROR: SCRIPT_FAILED_OR_NOT_FOUND")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
