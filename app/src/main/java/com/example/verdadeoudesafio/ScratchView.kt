package com.example.verdadeoudesafio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ScratchView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var overlayBitmap: Bitmap? = null
    private var overlayCanvas: Canvas? = null

    private val path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 350f
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var overlayColor = Color.GRAY

    fun setOverlayColor(color: Int) {
        overlayColor = color
        if (width > 0 && height > 0) {
            overlayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            overlayCanvas = Canvas(overlayBitmap!!)
            overlayCanvas?.drawColor(overlayColor)
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setOverlayColor(overlayColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        overlayBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(event.x, event.y)
            MotionEvent.ACTION_MOVE -> path.lineTo(event.x, event.y)
            MotionEvent.ACTION_UP -> {}
        }
        overlayCanvas?.drawPath(path, paint)
        invalidate()
        return true
    }

    fun getScratchedPercentage(): Float {
        overlayBitmap?.let { bitmap ->
            var count = 0
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            for (p in pixels) {
                if (p == 0) count++ // pixel apagado
            }
            return (count.toFloat() / pixels.size) * 100
        }
        return 0f
    }
}
