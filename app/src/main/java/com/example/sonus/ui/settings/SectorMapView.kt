package com.example.sonus.ui.settings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.sonus.R

class SectorMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var progress: Float = 0f // 0.0 to 1.0

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()
        
        val numSectors = 20
        val spacing = 4f
        val sectorWidth = (w - (numSectors - 1) * spacing) / numSectors
        
        val activeSectors = (progress * numSectors).toInt()
        
        val activeColor = ContextCompat.getColor(context, R.color.studio_amber)
        val emptyColor = ContextCompat.getColor(context, R.color.studio_line)
        
        for (i in 0 until numSectors) {
            val left = i * (sectorWidth + spacing)
            val right = left + sectorWidth
            
            sectorPaint.color = if (i < activeSectors) activeColor else emptyColor
            canvas.drawRect(left, 0f, right, h, sectorPaint)
        }
    }
}
