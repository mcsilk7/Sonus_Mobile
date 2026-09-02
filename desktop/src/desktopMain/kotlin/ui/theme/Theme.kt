package ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val StudioColorPalette = darkColors(
    primary = StudioAmber,
    primaryVariant = StudioAmberDim,
    secondary = StudioAmber,
    background = StudioBg,
    surface = StudioBgPanel,
    onPrimary = StudioBg,
    onSecondary = StudioBg,
    onBackground = StudioText,
    onSurface = StudioTextDim,
    error = StudioRed
)

@Composable
fun SonusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = StudioColorPalette,
        content = content
    )
}
