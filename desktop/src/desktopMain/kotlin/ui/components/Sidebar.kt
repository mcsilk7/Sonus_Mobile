package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ui.Screen
import ui.DesktopDI
import ui.theme.*

@Composable
fun Sidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(StudioBgPanel)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // MENU GŁÓWNE
        SidebarSectionHeader("MENU GŁÓWNE")
        SidebarItem("Odkrywaj", Icons.Default.Explore, currentScreen is Screen.Home) { onScreenSelected(Screen.Home) }
        SidebarItem("Biblioteka", Icons.Default.LibraryMusic, currentScreen is Screen.Library) { onScreenSelected(Screen.Library) }
        SidebarItem("Ulubione", Icons.Default.Favorite, false) { }
        SidebarItem("Radia", Icons.Default.Radio, false) { }

        Spacer(modifier = Modifier.height(32.dp))

        // PLAYLISTY
        SidebarSectionHeader("PLAYLISTY")
        SidebarItem("+ Stwórz playlistę", Icons.Default.Add, false) { }
        SidebarItem("Do auta", Icons.Default.MusicNote, false) { }
        SidebarItem("Wieczór", Icons.Default.MusicNote, false) { }
        SidebarItem("Praca / Focus", Icons.Default.MusicNote, false) { }
        SidebarItem("Chillout", Icons.Default.MusicNote, false) { }

        Spacer(modifier = Modifier.height(32.dp))

        // USTAWIENIA
        SidebarSectionHeader("USTAWIENIA")
        SidebarItem("Korektor (EQ)", Icons.Default.GraphicEq, false) { }
        SidebarItem("Konto", Icons.Default.Person, false) { }
        SidebarItem("Jakość audio", Icons.Default.HighQuality, currentScreen is Screen.Settings) { onScreenSelected(Screen.Settings) }
        
        // TEST ONLY: HARD RESET
        SidebarItem("DEBUG: RESET VPN", Icons.Default.Refresh, false) {
            DesktopDI.sessionManager.setVpnConfigured(false)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Terminal feed as small footer
        Text("SYSTEM_LOGS", color = StudioTextFaint, fontSize = 10.sp, fontWeight = FontWeight.Black)
        
        // Setup VPN button if needed
        androidx.compose.runtime.rememberCoroutineScope().let { scope ->
            val hasAccess = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                hasAccess.value = network.DesktopWireGuardManager.hasPasswordlessAccess()
            }
            
            if (!hasAccess.value) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material.TextButton(
                    onClick = {
                        scope.launch {
                            if (network.DesktopWireGuardManager.runPermissionSetup()) {
                                hasAccess.value = network.DesktopWireGuardManager.hasPasswordlessAccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(30.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("SETUP VPN AUTO-LOGIN", color = StudioAmber, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun SidebarSectionHeader(title: String) {
    Text(
        title,
        color = StudioTextFaint,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp, start = 12.dp)
    )
}

@Composable
private fun SidebarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) StudioLine else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) StudioAmber else StudioTextDim,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            color = if (isSelected) StudioText else StudioTextDim,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
