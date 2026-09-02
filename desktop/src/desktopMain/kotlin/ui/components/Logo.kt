package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import ui.theme.StudioAmber
import ui.theme.StudioBg

@Composable
fun SonusLogo(modifier: Modifier = Modifier, tint: Color = StudioAmber) {
    Canvas(modifier = modifier) {
        drawSonusLogo(tint)
    }
}

class SonusLogoPainter(private val showBackground: Boolean = true) : Painter() {
    override val intrinsicSize: Size = Size(108f, 108f)

    override fun DrawScope.onDraw() {
        if (showBackground) {
            drawCircle(
                color = StudioBg,
                radius = size.minDimension / 2f
            )
        }
        drawSonusLogo(StudioAmber, isIcon = true)
    }
}

fun DrawScope.drawSonusLogo(tint: Color, isIcon: Boolean = false) {
    // Viewport is 108x108
    val scaleFactor = size.width / 108f
    val foregroundScale = if (isIcon) 0.72f else 0.58f
    val offset = if (isIcon) 15f else 22f
    
    scale(scaleFactor, pivot = androidx.compose.ui.geometry.Offset.Zero) {
        translate(offset, offset) {
            scale(foregroundScale, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                // Small Single Note (Left)
                translate(0f, 40f) {
                    rotate(-25f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                        drawPath(
                            path = PathParser().parsePathString("M16,0v24c0,0 -1.5,-1 -3.5,-1c-3.6,0 -6.5,2.2 -6.5,5s2.9,5 6.5,5s6.5,-2.2 6.5,-5V4c3,0 5,2 5,6v-2c0,-5 -4,-8 -8,-8z").toPath(),
                            color = tint
                        )
                    }
                }

                // BEAMED PAIR (Center)
                translate(38f, 28f) {
                    rotate(-10f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                        // Beam
                        drawPath(
                            path = PathParser().parsePathString("M0,8 L32,0 L32,8 L0,16 Z").toPath(),
                            color = tint
                        )
                        // Left Note
                        drawPath(
                            path = PathParser().parsePathString("M0,8 v32c0,0 -1.5,-1 -3.5,-1c-3.6,0 -6.5,2.2 -6.5,5s2.9,5 6.5,5s6.5,-2.2 6.5,-5V8z").toPath(),
                            color = tint
                        )
                        // Right Note
                        drawPath(
                            path = PathParser().parsePathString("M32,0 v32c0,0 -1.5,-1 -3.5,-1c-3.6,0 -6.5,2.2 -6.5,5s2.9,5 6.5,5s6.5,-2.2 6.5,-5V0z").toPath(),
                            color = tint
                        )
                    }
                }

                // Small Single Note (Right)
                translate(75f, 52f) {
                    rotate(10f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                        drawPath(
                            path = PathParser().parsePathString("M12,0v24c0,0 -1.5,-1 -3.5,-1c-3.6,0 -6.5,2.2 -6.5,5s2.9,5 6.5,5s6.5,-2.2 6.5,-5V4c3,0 5,2 5,6v-2c0,-5 -4,-8 -8,-8z").toPath(),
                            color = tint
                        )
                    }
                }
            }
        }
    }
}
