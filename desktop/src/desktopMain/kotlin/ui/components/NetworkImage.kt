package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.sonus.Config
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jetbrains.skia.Image
import ui.DesktopDI
import ui.theme.StudioAmber
import ui.theme.StudioBgCard

private val imageClient = HttpClient()

@Composable
fun NetworkImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var imageBitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            imageBitmap = null
            return@LaunchedEffect
        }
        
        isLoading = true
        try {
            val token = DesktopDI.sessionManager.getToken()

            val encodedUrl = if (url.startsWith("http")) {
                url
            } else {
                URLBuilder(Config.BASE_URL).apply {
                    // Bezpieczne dołączanie ścieżki
                    appendPathSegments(url.split("/").filter { it.isNotEmpty() })
                    if (token != null) {
                        parameters.append("bearer", token)
                    }
                }.buildString()
            }
            
            println("SONUS_IMAGE_DEBUG: Loading $encodedUrl")
            
            val response = imageClient.get(encodedUrl) {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
                header("User-Agent", "SonusDesktop/2.0")
            }
            
            println("SONUS_IMAGE_DEBUG: HTTP ${response.status.value} for $url")
            
            if (response.status.value == 200) {
                val bytes = response.body<ByteArray>()
                val skiaImage = Image.makeFromEncoded(bytes)
                imageBitmap = skiaImage.toComposeImageBitmap()
            } else {
                imageBitmap = null
            }
        } catch (e: Exception) {
            println("SONUS_IMAGE_DEBUG: Error loading image $url: ${e.message}")
            imageBitmap = null
        } finally {
            isLoading = false
        }
    }

    Box(modifier = modifier.background(StudioBgCard), contentAlignment = Alignment.Center) {
        val bitmap = imageBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else if (isLoading) {
            CircularProgressIndicator(color = StudioAmber)
        }
    }
}
