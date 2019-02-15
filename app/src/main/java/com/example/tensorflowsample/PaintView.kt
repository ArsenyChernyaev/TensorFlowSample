package com.example.tensorflowsample

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class PaintView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val myDefaultColor = Color.BLACK
    private val myPath = Path()
    private val myPaint = Paint()

    init {
        myPaint.isAntiAlias = true
        myPaint.isDither = true
        myPaint.color = myDefaultColor
        myPaint.style = Paint.Style.STROKE
        myPaint.strokeJoin = Paint.Join.ROUND
        myPaint.strokeCap = Paint.Cap.ROUND
        myPaint.xfermode = null
        myPaint.alpha = 0xff
        myPaint.strokeWidth = 40f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(myPath, myPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                myPath.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                myPath.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
            }
        }

        invalidate()
        return true
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)
        canvas.drawPath(myPath, myPaint)
        return bitmap
    }

    fun clear() {
        myPath.reset()
        invalidate()
    }
}