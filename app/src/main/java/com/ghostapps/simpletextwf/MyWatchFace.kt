package com.ghostapps.simpletextwf

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.view.SurfaceHolder
import android.view.WindowInsets


import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.TimeZone
import android.os.BatteryManager



/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 *
 *
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
class MyWatchFace : CanvasWatchFaceService() {

    companion object {
        private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        private const val INTERACTIVE_UPDATE_RATE_MS = 5000

        /**
         * Handler message id for updating the time periodically in interactive mode.
         */
        private const val MSG_UPDATE_TIME = 0
    }

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    private class EngineHandler(reference: MyWatchFace.Engine) : Handler() {
        private val mWeakReference: WeakReference<MyWatchFace.Engine> = WeakReference(reference)

        override fun handleMessage(msg: Message) {
            val engine = mWeakReference.get()
            if (engine != null) {
                when (msg.what) {
                    MSG_UPDATE_TIME -> engine.handleUpdateTimeMessage()
                }
            }
        }
    }

    inner class Engine : CanvasWatchFaceService.Engine() {

        private lateinit var mCalendar: Calendar

        private var mRegisteredTimeZoneReceiver = false

        private var mXOffset: Float = 0F
        private var mYOffset: Float = 0F

        private lateinit var mHourPaint: Paint
        private lateinit var mMinutesPaint: Paint
        private lateinit var mBatteryPaint: Paint

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false
        private var mAmbient: Boolean = false

        private val mUpdateTimeHandler: Handler = EngineHandler(this)

        private val mTimeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@MyWatchFace)
                    .build()
            )

            mCalendar = Calendar.getInstance()

            val resources = this@MyWatchFace.resources
            mYOffset = resources.getDimension(R.dimen.digital_y_offset)

            // Initializes Watch Face.
            mHourPaint = Paint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                color = ContextCompat.getColor(applicationContext, R.color.digital_text)
            }
            mMinutesPaint = Paint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                color = ContextCompat.getColor(applicationContext, R.color.digital_text)
            }
            mBatteryPaint = Paint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                color = ContextCompat.getColor(applicationContext, R.color.digital_text)
            }
        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            mLowBitAmbient = properties.getBoolean(
                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
            )
            mBurnInProtection = properties.getBoolean(
                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
            )
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            mAmbient = inAmbientMode

            if (mLowBitAmbient) {
                mHourPaint.isAntiAlias = !inAmbientMode
                mMinutesPaint.isAntiAlias = !inAmbientMode
                mBatteryPaint.isAntiAlias = !inAmbientMode
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }


        override fun onDraw(canvas: Canvas, bounds: Rect) {
            // Draw the background.
            canvas.drawColor(Color.BLACK)

            if (!mAmbient) {
                drawBatteryLevel(canvas, bounds)
            }

            drawTime(canvas, bounds)

        }

        private fun drawTime(canvas: Canvas, bounds: Rect) {
            val now = System.currentTimeMillis()
            mCalendar.timeInMillis = now

            var hour =  mCalendar.get(Calendar.HOUR_OF_DAY)
            if (hour > 12) {
                hour -= 12
            }
            val minutes = mCalendar.get(Calendar.MINUTE).toText(applicationContext)

            val xPosition = bounds.width().toFloat() * 0.9f
            val yPosition = bounds.height().toFloat() / 2


            val minutesBounds = Rect()
            mMinutesPaint.getTextBounds(minutes, 0, minutes.length, minutesBounds)

            canvas.drawText(hour.toText(applicationContext), xPosition, yPosition, mHourPaint)
            canvas.drawText(minutes, xPosition, yPosition + minutesBounds.height() + 5, mMinutesPaint)
        }

        private fun drawBatteryLevel(canvas: Canvas, bounds: Rect) {
            val batteryLevel = getBatteryLevel()

            val xPosition = bounds.width().toFloat() / 2
            val yPosition = bounds.height().toFloat() * 0.2f

            canvas.drawText("$batteryLevel%", xPosition, yPosition, mBatteryPaint)
        }

        fun getBatteryLevel(): Int {
            val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 0

            return (level.toFloat() / scale.toFloat() * 100.0f).toInt()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()

                // Update time zone in case it changed while we weren't visible.
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            } else {
                unregisterReceiver()
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }

        private fun registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@MyWatchFace.registerReceiver(mTimeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = false
            this@MyWatchFace.unregisterReceiver(mTimeZoneReceiver)
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            // Load resources that have alternate values for round watches.
            val resources = this@MyWatchFace.resources
            val isRound = insets.isRound
            mXOffset = resources.getDimension(
                if (isRound)
                    R.dimen.digital_x_offset_round
                else
                    R.dimen.digital_x_offset
            )

            if (isRound) {
                mHourPaint.textSize = resources.getDimension(R.dimen.digital_hour_text_size_round)
                mMinutesPaint.textSize = resources.getDimension(R.dimen.digital_minutes_text_size_round)
                mBatteryPaint.textSize = resources.getDimension(R.dimen.digital_battery_size_round)
            } else {
                mHourPaint.textSize = resources.getDimension(R.dimen.digital_hour_text_size)
                mMinutesPaint.textSize = resources.getDimension(R.dimen.digital_minutes_text_size)
                mBatteryPaint.textSize = resources.getDimension(R.dimen.digital_battery_size)
            }


            mHourPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            mHourPaint.textAlign = Paint.Align.RIGHT
            mMinutesPaint.textAlign = Paint.Align.RIGHT
            mBatteryPaint.textAlign = Paint.Align.CENTER
        }

        /**
         * Starts the [.mUpdateTimeHandler] timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private fun updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        /**
         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        fun handleUpdateTimeMessage() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val timeMs = System.currentTimeMillis()
                val delayMs = INTERACTIVE_UPDATE_RATE_MS - timeMs % INTERACTIVE_UPDATE_RATE_MS
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
            }
        }
    }
}
