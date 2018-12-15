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
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.view.SurfaceHolder
import android.view.WindowInsets

import java.lang.ref.WeakReference
import android.os.BatteryManager
import java.text.DateFormat
import java.util.*

class MyWatchFace : CanvasWatchFaceService() {

    companion object {
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

        private lateinit var mHour: ScreenElementModel
        private lateinit var mMinute: ScreenElementModel
        private lateinit var mBattery: ScreenElementModel
        private lateinit var mDate: ScreenElementModel

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false
        private var mAmbient: Boolean = false

        private val mUpdateTimeHandler: Handler = EngineHandler(this)

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            mAmbient = inAmbientMode

            if (mLowBitAmbient) {
                mHour.paint.isAntiAlias = !inAmbientMode
                mMinute.paint.isAntiAlias = !inAmbientMode
                mBattery.paint.isAntiAlias = !inAmbientMode
                mDate.paint.isAntiAlias = !inAmbientMode
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }


        override fun onDraw(canvas: Canvas, bounds: Rect) {
            // Draw the background.
            canvas.drawColor(Color.BLACK)

            drawTime(canvas, bounds)

            if (!mAmbient) {
                drawBatteryLevel(canvas, bounds)
                drawDate(canvas, bounds)
            }

        }

        private fun drawTime(canvas: Canvas, bounds: Rect) {
            val now = System.currentTimeMillis()
            mCalendar.timeInMillis = now

            var hour =  mCalendar.get(Calendar.HOUR_OF_DAY)
            if (hour > 12) {
                hour -= 12
            }
            mHour.draw(hour.toText(applicationContext), canvas, bounds)

            val minutes = mCalendar.get(Calendar.MINUTE).toText(applicationContext)

            mMinute.draw(minutes, canvas, bounds, mMinute.getHeight() + 5)
        }

        private fun drawBatteryLevel(canvas: Canvas, bounds: Rect) {
            val batteryLevel = getBatteryLevel()

            mBattery.draw("$batteryLevel%", canvas, bounds)
        }

        private fun drawDate(canvas: Canvas, bounds: Rect) {
            val dateString = DateFormat.getDateInstance(DateFormat.LONG).format(mCalendar.time)

            mDate.draw(dateString, canvas, bounds)
        }

        fun getBatteryLevel(): Int {
            val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 0

            return (level.toFloat() / scale.toFloat() * 100.0f).toInt()
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            mHour = ScreenElementModel(0.9f,
                0.5f,
                resources.getDimension(R.dimen.digital_hour_text_size),
                applicationContext)

            mMinute = ScreenElementModel(0.9f,
                0.5f,
                resources.getDimension(R.dimen.digital_minutes_text_size),
                applicationContext)

            mDate = ScreenElementModel(0.5f,
                0.8f,
                resources.getDimension(R.dimen.digital_date_size),
                applicationContext)

            if (insets.isRound) {
                mBattery = ScreenElementModel(0.4f,
                    0.2f,
                    resources.getDimension(R.dimen.digital_battery_size),
                    applicationContext)
            } else {
                mBattery = ScreenElementModel(0.25f,
                    0.2f,
                    resources.getDimension(R.dimen.digital_battery_size),
                    applicationContext)
            }


            mHour.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            mHour.paint.textAlign = Paint.Align.RIGHT
            mMinute.paint.textAlign = Paint.Align.RIGHT
            mBattery.paint.textAlign = Paint.Align.CENTER
            mDate.paint.textAlign = Paint.Align.CENTER
        }

        //region default methods
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
        //endregion
    }
}
