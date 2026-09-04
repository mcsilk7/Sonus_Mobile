package ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import ui.theme.StudioAmber
import ui.theme.StudioLine

@Composable
fun RetroReels(
    isPlaying: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
    reelSize: androidx.compose.ui.unit.Dp = 80.dp
) {
    val transition = rememberInfiniteTransition()
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val animatedRotation = if (isPlaying) rotation else 0f

    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Reel(animatedRotation, progress, reelSize)
        Spacer(modifier = Modifier.width(20.dp))
        Reel(animatedRotation, 1f - progress, reelSize)
    }
}

@Composable
private fun Reel(rotation: Float, tapeLevel: Float, size: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.width / 2
        
        // Outer rim
        drawCircle(
            color = StudioLine,
            radius = radius,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Tape
        drawCircle(
            color = StudioAmber.copy(alpha = 0.3f),
            radius = radius * 0.2f + (radius * 0.7f * tapeLevel)
        )
        
        // Rotating spikes
        rotate(rotation, center) {
            for (i in 0 until 3) {
                rotate(i * 120f, center) {
                    drawLine(
                        color = StudioAmber,
                        start = center.copy(y = center.y - radius * 0.2f),
                        end = center.copy(y = center.y - radius * 0.9f),
                        strokeWidth = 4.dp.toPx()
                    )
                }
            }
        }
        
        // Center hub
        drawCircle(
            color = StudioAmber,
            radius = radius * 0.15f
        )
    }
}
