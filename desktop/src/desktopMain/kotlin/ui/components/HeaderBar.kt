package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.*

@Composable
fun HeaderBar(onLogout: () -> Unit = {}) {
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
        var searchText by remember { mutableStateOf("") }
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp)),
            placeholder = { Text("Szukaj utworów, wykonawców...", color = StudioTextDim, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StudioTextDim) },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = StudioBgCard,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = StudioAmber,
                textColor = StudioText
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(48.dp))

        // Window Controls (Mockup)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Remove, "Minimize", tint = StudioTextDim, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.CropSquare, "Maximize", tint = StudioTextDim, modifier = Modifier.size(18.dp))
            }
            
            // Temporary Logout Button
            TextButton(
                onClick = onLogout,
                colors = ButtonDefaults.textButtonColors(contentColor = StudioRed)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("LOGOUT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
