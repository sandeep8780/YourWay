package com.example.yourway.fetchpost

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import kotlin.math.sqrt

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    private var scale = 1f
    private val matrix = Matrix()
    private var lastTouch = PointF()
    private var startTouch = PointF()
    private var mode = Mode.NONE
    private var maxScale = 3f // Maximum zoom scale
    private var initialDistance = 0f

    private enum class Mode {
        NONE,
        DRAG,
        ZOOM
    }

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastTouch.set(event.x, event.y)
                startTouch.set(lastTouch)
                mode = Mode.DRAG
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.DRAG) {
                    val dx = event.x - lastTouch.x
                    val dy = event.y - lastTouch.y
                    matrix.postTranslate(dx, dy)
                    lastTouch.set(event.x, event.y)
                } else if (mode == Mode.ZOOM) {
                    if (event.pointerCount == 2) {
                        scaleImage(event)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                initialDistance = spacing(event)
                if (initialDistance > 10f) { // Prevents zooming with very small distances
                    mode = Mode.ZOOM
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mode = Mode.NONE
            }
            MotionEvent.ACTION_UP -> {
                mode = Mode.NONE
            }
        }

        // Apply the current matrix to the ImageView
        imageMatrix = matrix
        invalidate()
        return true
    }

    private fun scaleImage(event: MotionEvent) {
        if (event.pointerCount == 2) {
            val newDistance = spacing(event)
            if (newDistance > 10f) { // Ensure there is a significant distance
                val scaleFactor = newDistance / initialDistance
                scale *= scaleFactor

                // Limit the scale
                if (scale > maxScale) {
                    scale = maxScale
                } else if (scale < 1f) {
                    scale = 1f
                }

                // Apply the scaling to the matrix
                matrix.setScale(scale, scale, event.getX(0), event.getY(0))
            }
        }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }
}
