package com.ghostapps.simpletextwf.models

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import com.ghostapps.simpletextwf.R

class ScreenElementModel (var xRelativePosition: Float, var yRelativePosition: Float, var textSize: Float, val context: Context) {

    private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

    var paint = Paint().apply {
        typeface = NORMAL_TYPEFACE
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.digital_text)
        textAlign = Paint.Align.RIGHT
    }

    var borderPaint = Paint().apply {
        typeface = NORMAL_TYPEFACE
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.digital_text)
        strokeWidth = 1f
        style = Paint.Style.STROKE
        textAlign = Paint.Align.RIGHT
    }

    init {
        paint.textSize = textSize
        borderPaint.textSize = textSize
    }

    fun getHeight(): Int {
        val sampleString = "sample string"
        val bounds = Rect()
        paint.getTextBounds(sampleString, 0, sampleString.length, bounds)

        return bounds.height()
    }

    fun getWidth(text: String): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        return bounds.width()
    }

    fun draw(text: String, canvas: Canvas, bounds: Rect) {
        canvas.drawText(text, xRelativePosition * bounds.height().toFloat(), yRelativePosition * bounds.height().toFloat(), paint)
    }

    fun draw(text: String, canvas: Canvas, bounds: Rect, burnInProtection: Boolean) {
        if (burnInProtection) {
            canvas.drawText(text, xRelativePosition * bounds.height().toFloat(), yRelativePosition * bounds.height().toFloat(), borderPaint)
//            paint.color = ContextCompat.getColor(context, R.color.background)
//            canvas.drawText(text, xRelativePosition * bounds.height().toFloat(), yRelativePosition * bounds.height().toFloat(), paint)
//            paint.color = ContextCompat.getColor(context, R.color.digital_text)

        } else {
            canvas.drawText(text, xRelativePosition * bounds.height().toFloat(), yRelativePosition * bounds.height().toFloat(), paint)
        }

    }

    fun drawWithDeltaY(text: String, canvas: Canvas, bounds: Rect, deltaY: Int) {
        canvas.drawText(text, xRelativePosition * bounds.height().toFloat(), yRelativePosition * bounds.height().toFloat() + deltaY, paint)
    }

    fun drawWithDeltaX(text: String, canvas: Canvas, bounds: Rect, deltaX: Int) {
        canvas.drawText(text, xRelativePosition * bounds.height().toFloat() - deltaX, yRelativePosition * bounds.height().toFloat(), paint)
    }

}