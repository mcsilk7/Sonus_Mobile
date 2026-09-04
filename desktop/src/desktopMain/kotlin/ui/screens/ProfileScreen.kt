package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.DesktopDI
import ui.viewmodel.LibraryViewModel
import ui.theme.*

@Composable
fun ProfileScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { LibraryViewModel(scope) }
    val username = DesktopDI.sessionManager.getUsername() ?: "OPERATOR"
    val role = DesktopDI.sessionManager.getRole() ?: "USER"
    
    val totalFavorites = viewModel.favorites.size
    val totalPlaylists = viewModel.playlists.size
    val totalSeconds = viewModel.favorites.sumOf { it.duration ?: 0 }
    val totalHours = totalSeconds / 3600

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Technical Header
        Text("SYSTEM_USER_INTERFACE", color = StudioTextFaint, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(
            "OPERATOR_PROFILE",
            color = StudioText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Initial)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(StudioAmberDim.copy(alpha = 0.3f))
                    .background(StudioBgPanel),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    color = StudioAmber,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text(username.uppercase(), color = StudioText, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("LEVEL: $role", color = StudioAmber, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { /* Edit logic */ }, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Edit, null, tint = StudioAmber, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("MODIFY_IDENTIFIER", color = StudioAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Stats Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("UNITS", totalPlaylists.toString(), "PL_DATA", Modifier.weight(1f))
            StatCard("RUNTIME", "${totalHours}H", "LISTENING", Modifier.weight(1f))
            StatCard("SIGNALS", totalFavorites.toString(), "FAV_DATA", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Actions
        SettingsSection("ACCOUNT_SECURITY") {
            ActionRow("TERMINATE_SESSION", Icons.AutoMirrored.Filled.Logout, StudioRed) {
                DesktopDI.sessionManager.clearSession()
                // Redirect logic handled in Main.kt
            }
            ActionRow("WIPE_LOCAL_BUFFER", Icons.Default.DeleteSweep, StudioText) {
                // Wipe logic
            }
            ActionRow("DELETE_ACCOUNT_DATA", Icons.Default.Delete, StudioRed) {
                // Delete logic
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "BUILD: SONUS_V2.5.1_DESKTOP_STABLE",
            color = StudioTextFaint,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, subLabel: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioBgPanel)
            .padding(16.dp)
    ) {
        Text(label, color = StudioTextFaint, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        Text(value, color = StudioAmber, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(subLabel, color = StudioTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(title, color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        Divider(color = StudioLine, modifier = Modifier.padding(vertical = 12.dp))
        content()
    }
}

@Composable
private fun ActionRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = StudioTextFaint, modifier = Modifier.size(18.dp))
    }
}
