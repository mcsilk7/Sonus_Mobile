package com.example.sonus.ui.player

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.sonus.R
import java.util.Random

class NoiseOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val noisePaint = Paint().apply {
        alpha = 15 // Very subtle
    }
    
    private val scanlinePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.studio_black)
        alpha = 10
        strokeWidth = 1f
    }
    
    private val random = Random()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw random noise points
        for (i in 0..100) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            canvas.drawPoint(x, y, noisePaint)
        }

        // Draw horizontal scanlines
        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, scanlinePaint)
            y += 6f // Spacing between scanlines
        }

        // Keep animating for noise flicker
        postInvalidateDelayed(50)
    }
}
