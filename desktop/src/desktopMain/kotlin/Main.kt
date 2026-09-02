import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
                var currentScreen by remember { 
                    mutableStateOf<Screen>(if (DesktopDI.sessionManager.isLoggedIn()) Screen.Home else Screen.Login) 
                }
                var isLoggedIn by remember { mutableStateOf(DesktopDI.sessionManager.isLoggedIn()) }
                var showVpnSetup by remember { mutableStateOf(false) }

                // Auto-connect VPN on startup
                LaunchedEffect(Unit) {
                    TerminalManager.addLog("SYSTEM_BOOT_SEQUENCE_INITIATED")
                    
                    val userSonusExists = DesktopWireGuardManager.checkSystemUserExists("sonus")
                    if (!userSonusExists) {
                        TerminalManager.addLog("ERROR: SYSTEM_USER 'sonus' NOT_FOUND")
                        TerminalManager.addLog("VPN_AUTO_CONFIG_UNAVAILABLE")
                    }

                    val hasAccess = DesktopWireGuardManager.hasPasswordlessAccess()
                    
                    // Jeśli użytkownik 'sonus' istnieje, ale nie mamy uprawnień i nie pytaliśmy jeszcze
                    if (userSonusExists && !hasAccess && !DesktopDI.sessionManager.isVpnConfigured()) {
                        showVpnSetup = true
                        TerminalManager.addLog("VPN_PERMISSIONS: SETUP_REQUIRED")
                    } else if (hasAccess) {
                        TerminalManager.addLog("VPN_PERMISSIONS: PASSWORDLESS_OK")
                        TerminalManager.addLog("ESTABLISHING_SECURE_TUNNEL...")
                        
                        val success = DesktopWireGuardManager.startVpn()
                        if (success) {
                            TerminalManager.addLog("VPN_LINK_ESTABLISHED: ENCRYPTED")
                        } else {
                            TerminalManager.addLog("VPN_LINK_FAILED: CHECK_WIREGUARD_STATUS")
                        }
                    } else {
                        TerminalManager.addLog("VPN_LINK_SKIPPED: NO_AUTO_LOGIN_CONFIG")
                    }
                }

                if (showVpnSetup) {
                    VpnSetupDialog(
                        onConfirm = {
                            DesktopWireGuardManager.runPermissionSetup()
                            DesktopDI.sessionManager.setVpnConfigured(true)
                            showVpnSetup = false
                        },
                        onDismiss = {
                            DesktopDI.sessionManager.setVpnConfigured(true)
                            showVpnSetup = false
                        }
                    )
                }

                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = { 
                        isLoggedIn = true 
                        currentScreen = Screen.Home 
                    })
                } else {
                    Column(modifier = Modifier.fillMaxSize().background(StudioBg)) {
                        // Header Bar
                        HeaderBar()

                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            // Left Sidebar
                            Sidebar(currentScreen) { currentScreen = it }
                            
                            // Main Content
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                when (currentScreen) {
                                    is Screen.Home -> HomeScreen()
                                    is Screen.Search -> SearchScreen()
                                    is Screen.Library -> LibraryScreen()
                                    is Screen.Settings -> SettingsScreen()
                                    else -> {}
                                }
                            }

                            // Right Panel
                            RightPanel()
                        }

                        // Footer Player Bar
                        PlayerBar()
                    }
                }
            }
        }
    }
}
