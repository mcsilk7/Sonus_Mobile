package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.*

@Composable
fun HeaderBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(StudioBgPanel)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon & Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            SonusLogo(
                modifier = Modifier.size(32.dp),
                tint = StudioAmber
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Sonus",
                color = StudioText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(48.dp))

        // Search Bar
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(StudioBgCard, RoundedCornerShape(20.dp)),
            textStyle = TextStyle(color = StudioText, fontSize = 14.sp),
            cursorBrush = SolidColor(StudioAmber),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = StudioTextDim,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                "Szukaj utworów, wykonawców...",
                                color = StudioTextDim,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = StudioTextDim
                            )
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.width(48.dp))

        // User Profile with Menu
        var showMenu by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = StudioText,
                    modifier = Modifier.size(32.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(StudioBgPanel)
            ) {
                DropdownMenuItem(onClick = { 
                    showMenu = false
                    onProfileClick()
                }) {
                    Text("Mój Profil", color = StudioText)
                }
                Divider(color = StudioLine)
                DropdownMenuItem(onClick = { 
                    showMenu = false
                    onLogout()
                }) {
                    Text("Wyloguj się", color = StudioRed)
                }
            }
        }
    }
}
