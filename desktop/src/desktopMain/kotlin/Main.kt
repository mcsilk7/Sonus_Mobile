import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
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
import ui.viewmodel.SearchViewModel
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
                try {
                    val isFlatpak = java.io.File("/.flatpak-info").exists()
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
            // Natywne tło okna, aby uniknąć białych błysków
            LaunchedEffect(window) {
                val studioBgAwt = java.awt.Color(20, 18, 15) // #14120F
                window.background = studioBgAwt
                window.contentPane.background = studioBgAwt
            }

            SonusTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = StudioBg) {
                    val scope = rememberCoroutineScope()
                    val searchViewModel = remember { SearchViewModel(scope) }
                    
                    var currentScreen by remember { 
                        mutableStateOf<Screen>(if (DesktopDI.sessionManager.isLoggedIn()) Screen.Home else Screen.Login) 
                    }
                    var isLoggedIn by remember { mutableStateOf(DesktopDI.sessionManager.isLoggedIn()) }
                    var showVpnSetup by remember { mutableStateOf(!DesktopDI.sessionManager.isVpnConfigured()) }

                    LaunchedEffect(Unit) {
                        if (DesktopDI.sessionManager.isVpnConfigured()) {
                            if (DesktopWireGuardManager.hasPasswordlessAccess()) {
                                DesktopWireGuardManager.startVpn()
                            } else {
                                showVpnSetup = true
                            }
                        }
                    }

                    // Główny kontener aplikacji z użyciem Scaffold dla stabilności layoutu
                    Scaffold(
                        backgroundColor = StudioBg,
                        topBar = {
                            if (isLoggedIn) {
                                HeaderBar(
                                    query = searchViewModel.query,
                                    onQueryChange = { 
                                        searchViewModel.onQueryChange(it)
                                        if (it.isNotEmpty() && currentScreen !is Screen.Search) {
                                            currentScreen = Screen.Search
                                        }
                                    },
                                    onProfileClick = { currentScreen = Screen.Profile },
                                    onLogout = {
                                        DesktopDI.sessionManager.clearSession()
                                        isLoggedIn = false
                                        currentScreen = Screen.Login
                                    }
                                )
                            }
                        },
                        bottomBar = {
                            if (isLoggedIn) {
                                PlayerBar()
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            if (!isLoggedIn) {
                                LoginScreen(onLoginSuccess = { 
                                    isLoggedIn = true 
                                    currentScreen = Screen.Home 
                                })
                            } else {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Sidebar(currentScreen) { currentScreen = it }
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        when (currentScreen) {
                                            is Screen.Home -> HomeScreen()
                                            is Screen.Search -> SearchScreen(searchViewModel)
                                            is Screen.Library -> LibraryScreen()
                                            is Screen.Favorites -> FavoritesScreen()
                                            is Screen.Profile -> ProfileScreen()
                                            is Screen.Settings -> SettingsScreen()
                                            else -> {}
                                        }
                                    }
                                    RightPanel()
                                }
                            }

                            // VPN Setup Overlay
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
                                                val success = DesktopWireGuardManager.runPermissionSetup()
                                                if (success) {
                                                    DesktopDI.sessionManager.setVpnConfigured(true)
                                                    if (DesktopWireGuardManager.hasPasswordlessAccess()) {
                                                        DesktopWireGuardManager.startVpn()
                                                    }
                                                    showVpnSetup = false
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
    }
}
