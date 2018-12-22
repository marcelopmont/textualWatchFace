package com.ghostapps.simpletextwf.models

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.ghostapps.simpletextwf.R
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat


class BatteryImageModel(private var xRelativePosition: Float, private var yRelativePosition: Float, private val context: Context) {

    companion object {
        const val ICON_SIZE_MULTIPLIER = 0.6
        const val ICON_VERTICAL_POSITION_OFFSET = 4 // transposition to make icon vertical centralized
    }

    private val battery20: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_20)!!.toBitmap() }
    private val battery30: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_30)!!.toBitmap() }
    private val battery50: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_50)!!.toBitmap() }
    private val battery60: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_60)!!.toBitmap() }
    private val battery80: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_80)!!.toBitmap() }
    private val battery90: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_90)!!.toBitmap() }
    private val batteryAlert: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_alert)!!.toBitmap() }
    private val batteryFull: Bitmap by lazy { ContextCompat.getDrawable(context, R.drawable.battery_full)!!.toBitmap() }

    private var paint = Paint()

    fun drawBatteryIcon(batteryLevel: Int, canvas: Canvas, bounds: Rect, xDelta: Int) {

        val icon = getBatteryIconBitmap(batteryLevel)

        val xPosition = xRelativePosition * bounds.height().toFloat() - (icon.width * ICON_SIZE_MULTIPLIER).toInt() - xDelta
        val yPosition = yRelativePosition * bounds.height().toFloat() - (icon.height * ICON_SIZE_MULTIPLIER).toInt() + ICON_VERTICAL_POSITION_OFFSET

        canvas.drawBitmap(icon,
            xPosition,
            yPosition,
            paint)

    }

    fun getWidth(batteryLevel: Int): Int {
        val icon = getBatteryIconBitmap(batteryLevel)
        return (icon.width * ICON_SIZE_MULTIPLIER).toInt()
    }

    private fun getBatteryIconBitmap(batteryLevel: Int): Bitmap {
        return when {
            batteryLevel > 95 -> batteryFull
            batteryLevel > 90 -> battery90
            batteryLevel > 80 -> battery80
            batteryLevel > 60 -> battery60
            batteryLevel > 50 -> battery50
            batteryLevel > 30 -> battery30
            batteryLevel > 20 -> battery20
            else -> batteryAlert
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return this.bitmap

        val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, (canvas.width * ICON_SIZE_MULTIPLIER).toInt(), (canvas.height * ICON_SIZE_MULTIPLIER).toInt())
        this.draw(canvas)

        return bitmap
    }
}