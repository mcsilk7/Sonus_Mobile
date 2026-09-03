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
import ui.Screen
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
        SidebarItem("Szukaj", Icons.Default.Search, currentScreen is Screen.Search) { onScreenSelected(Screen.Search) }
        SidebarItem("Biblioteka", Icons.Default.LibraryMusic, currentScreen is Screen.Library) { onScreenSelected(Screen.Library) }
        SidebarItem("Ulubione", Icons.Default.Favorite, currentScreen is Screen.Favorites) { onScreenSelected(Screen.Favorites) }

        Spacer(modifier = Modifier.height(32.dp))

        // USTAWIENIA
        SidebarSectionHeader("USTAWIENIA")
        SidebarItem("Profil", Icons.Default.Person, currentScreen is Screen.Profile) { onScreenSelected(Screen.Profile) }
        SidebarItem("Ustawienia", Icons.Default.Settings, currentScreen is Screen.Settings) { onScreenSelected(Screen.Settings) }
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
