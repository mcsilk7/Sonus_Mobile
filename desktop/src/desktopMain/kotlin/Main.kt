import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sonus.DependencyContainer
import com.example.sonus.DesktopDateFormatter
import com.example.sonus.DesktopNetworkMonitor
import com.example.sonus.db.getDatabaseBuilder
import data.DesktopSessionManager
import ui.DesktopDI
import ui.Screen
import ui.components.PlayerBar
import ui.components.Sidebar
import ui.screens.*
import ui.theme.SonusTheme
import ui.theme.StudioBg


fun main() {
    DesktopDI.init()
    
    application {
        Window(onCloseRequest = ::exitApplication, title = "Sonus Station Desktop") {
            SonusTheme {
                var currentScreen by remember { 
                    mutableStateOf<Screen>(if (DesktopDI.sessionManager.isLoggedIn()) Screen.Home else Screen.Login) 
                }
                var isLoggedIn by remember { mutableStateOf(DesktopDI.sessionManager.isLoggedIn()) }

                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = { 
                        isLoggedIn = true 
                        currentScreen = Screen.Home 
                    })
                } else {
                    Row(modifier = Modifier.fillMaxSize().background(StudioBg)) {
                        Sidebar(currentScreen) { currentScreen = it }
                        
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Box(modifier = Modifier.weight(1f)) {
                                when (currentScreen) {
                                    is Screen.Home -> HomeScreen()
                                    is Screen.Search -> SearchScreen()
                                    is Screen.Library -> LibraryScreen()
                                    is Screen.Settings -> SettingsScreen()
                                    else -> {}
                                }
                            }
                            PlayerBar()
                        }
                    }
                }
            }
        }
    }
}
