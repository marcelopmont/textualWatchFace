package com.ghostapps.simpletextwf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.support.v4.content.ContextCompat

class ScreenElementModel (var xRelativePosition: Float, var yRelativePosition: Float, var textSize: Float, val context: Context) {

    private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

    var paint = Paint().apply {
        typeface = NORMAL_TYPEFACE
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.digital_text)
    }

    init {
        paint.textSize = textSize
    }

    fun getHeight(): Int {
        val sampleString = "sample string"
        val bounds = Rect()
        paint.getTextBounds(sampleString, 0, sampleString.length, bounds)

        return bounds.height()
    }

    fun draw(text: String, canvas: Canvas, bounds: Rect) {
        canvas.drawText(text, xRelativePosition * bounds.height().toFloat(), yRelativePosition * bounds.height().toFloat(), paint)
    }

    fun draw(text: String, canvas: Canvas, bounds: Rect, deltaY: Int) {
        canvas.drawText(text, xRelativePosition * bounds.height().toFloat(), yRelativePosition * bounds.height().toFloat() + deltaY, paint)
    }

}