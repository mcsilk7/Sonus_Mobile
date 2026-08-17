package com.example.sonus.ui.player

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class RetroReelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private val accentColor = Color.parseColor("#E8A13A")
    private val metalColor = Color.parseColor("#3A3630")
    private val lineColor = Color.parseColor("#555048")
    private val tapeColor = Color.parseColor("#1A1814")

    private val structurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = tapeColor
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    private var lastDrawTime = 0L
    private var leftAngle = 0f
    private var rightAngle = 0f
    
    var isSpinning = false
        set(value) {
            if (field == value) return
            field = value
            if (value) {
                lastDrawTime = SystemClock.uptimeMillis()
                postInvalidateOnAnimation()
            }
        }

    var progress = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentTime = SystemClock.uptimeMillis()
        if (isSpinning) {
            val deltaTime = if (lastDrawTime == 0L) 0 else currentTime - lastDrawTime
            lastDrawTime = currentTime
            
            // Increment angles independently to avoid wrap-around jumps
            val baseSpeed = 0.09f // degrees per millisecond
            leftAngle = (leftAngle + baseSpeed * deltaTime) % 360f
            rightAngle = (rightAngle + baseSpeed * 1.05f * deltaTime) % 360f
        } else {
            lastDrawTime = 0L
        }

        val w = width.toFloat()
        val h = height.toFloat()
        val reelRadius = w / 4.4f
        val centerY = h / 2f
        val spacing = 25f

        val leftCenterX = w / 2f - reelRadius - spacing
        val rightCenterX = w / 2f + reelRadius + spacing

        // 1. Draw Skeleton Reels
        drawSkeletonReel(canvas, leftCenterX, centerY, reelRadius, leftAngle, (1f - progress) * 0.5f)
        drawSkeletonReel(canvas, rightCenterX, centerY, reelRadius, rightAngle, progress)

        if (isSpinning) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawSkeletonReel(canvas: Canvas, cx: Float, cy: Float, radius: Float, angle: Float, tapeLevel: Float) {
        // 1. Draw the tape first
        val maxTapeRadius = radius * 0.88f
        val minTapeRadius = radius * 0.38f
        val currentTapeRadius = minTapeRadius + (maxTapeRadius - minTapeRadius) * tapeLevel
        canvas.drawCircle(cx, cy, currentTapeRadius, tapePaint)

        // 2. Save layer for DST_OUT cutting
        val count = canvas.saveLayer(cx - radius, cy - radius, cx + radius, cy + radius, null)

        canvas.save()
        canvas.rotate(angle, cx, cy)

        // 3. Draw solid metal reel body
        fillPaint.color = metalColor
        canvas.drawCircle(cx, cy, radius, fillPaint)

        // 4. Cut 6 angular holes
        val numCutouts = 6
        val cutoutPath = Path()
        for (i in 0 until numCutouts) {
            val startA = i * (360f / numCutouts)
            val sweep = 38f
            val outerR = radius * 0.92f
            val innerR = radius * 0.42f

            val p1 = getPoint(cx, cy, innerR, startA - sweep / 2)
            val p2 = getPoint(cx, cy, outerR, startA - sweep / 2 + 5)
            val p3 = getPoint(cx, cy, outerR, startA + sweep / 2 - 5)
            val p4 = getPoint(cx, cy, innerR, startA + sweep / 2)

            cutoutPath.reset()
            cutoutPath.moveTo(p1.x, p1.y)
            cutoutPath.lineTo(p2.x, p2.y)
            cutoutPath.lineTo(p3.x, p3.y)
            cutoutPath.lineTo(p4.x, p4.y)
            cutoutPath.close()

            canvas.drawPath(cutoutPath, clearPaint)

            // Draw angular rim around the hole
            structurePaint.style = Paint.Style.STROKE
            structurePaint.strokeWidth = 2f
            structurePaint.color = lineColor
            canvas.drawPath(cutoutPath, structurePaint)
        }

        // 5. Draw Outer Rim and Hub on top
        structurePaint.strokeWidth = 4f
        structurePaint.color = lineColor
        canvas.drawCircle(cx, cy, radius, structurePaint)

        fillPaint.color = Color.parseColor("#2A2620")
        canvas.drawCircle(cx, cy, radius * 0.35f, fillPaint)
        structurePaint.color = accentColor
        structurePaint.strokeWidth = 2f
        canvas.drawCircle(cx, cy, radius * 0.35f, structurePaint)

        canvas.restore()
        canvas.restoreToCount(count)
    }

    private fun getPoint(cx: Float, cy: Float, radius: Float, angleDeg: Float): PointF {
        val rad = Math.toRadians(angleDeg.toDouble())
        return PointF(
            cx + radius * cos(rad).toFloat(),
            cy + radius * sin(rad).toFloat()
        )
    }
}
