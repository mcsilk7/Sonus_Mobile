package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.Screen
import ui.theme.StudioAmber
import ui.theme.StudioBgPanel
import ui.theme.StudioLine
import ui.theme.StudioText
import ui.theme.StudioTextDim

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
    ) {
        Text(
            "SONUS STATION",
            color = StudioAmber,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        SidebarItem("Home", Icons.Default.Home, currentScreen is Screen.Home) { onScreenSelected(Screen.Home) }
        SidebarItem("Search", Icons.Default.Search, currentScreen is Screen.Search) { onScreenSelected(Screen.Search) }
        SidebarItem("Library", Icons.Default.List, currentScreen is Screen.Library) { onScreenSelected(Screen.Library) }
        
        Spacer(modifier = Modifier.weight(1f))
        
        SidebarItem("Settings", Icons.Default.Settings, currentScreen is Screen.Settings) { onScreenSelected(Screen.Settings) }
    }
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
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) StudioLine else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) StudioAmber else StudioTextDim,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            label,
            color = if (isSelected) StudioText else StudioTextDim,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
