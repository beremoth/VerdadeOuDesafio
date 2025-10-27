package com.example.verdadeoudesafio

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ScratchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var overlayBitmap: Bitmap? = null
    private var overlayCanvas: Canvas? = null
    private val path = Path()
    private var overlayColor = Color.DKGRAY

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 200f
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var onScratchListener: ((Float) -> Unit)? = null

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
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                overlayCanvas?.drawPath(path, paint)
                invalidate()
                onScratchListener?.invoke(getScratchedPercentage())
            }
            MotionEvent.ACTION_UP -> {
                onScratchListener?.invoke(getScratchedPercentage())
            }
        }
        return true
    }

    fun setOnScratchListener(listener: (Float) -> Unit) {
        this.onScratchListener = listener
    }

    fun reset() {
        path.reset()
        setOverlayColor(overlayColor)
    }

    fun getScratchedPercentage(): Float {
        overlayBitmap?.let { bitmap ->
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val scratched = pixels.count { it == 0 }
            return (scratched.toFloat() / pixels.size) * 100
        }
        return 0f
    }
}